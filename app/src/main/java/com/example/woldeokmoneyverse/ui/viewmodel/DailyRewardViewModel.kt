package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class DailyRewardUiState(
    val available: Boolean,
    val nextEligibleAt: String? = null,
    val lastClaimedAmount: String? = null
)

class DailyRewardViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<DailyRewardUiState>>(UiState.Loading)
    val state: StateFlow<UiState<DailyRewardUiState>> = _state.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            runCatching {
                val response = ApiClient.api.contractGet("app-api/v1/rewards/availability")
                if (!response.isSuccessful || response.body() == null) {
                    error("출석 가능 여부 조회 실패 (HTTP ${response.code()})")
                }
                val root = response.body()!!.asJsonObject
                DailyRewardUiState(
                    available = root.boolean("dailyAvailable", "daily_available") ?: false,
                    nextEligibleAt = root.text("dailyNextEligibleAt", "daily_next_eligible_at")
                )
            }.fold(
                onSuccess = { _state.value = UiState.Success(it) },
                onFailure = { _state.value = UiState.Error(it.message ?: "출석 상태 조회 실패") }
            )
        }
    }

    fun claim() {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                val payload = JsonObject().apply {
                    addProperty("idempotencyKey", UUID.randomUUID().toString())
                }
                val response = ApiClient.api.contractPost("app-api/v1/rewards/daily/claims", payload)
                if (response.isSuccessful && response.body() != null) {
                    val root = response.body()!!.asJsonObject
                    val amount = root.text("amount") ?: "0"
                    _message.value = "일일 출석 보상 ${amount} WLD를 받았습니다."
                    load()
                } else {
                    if (response.code() == 409) {
                        _message.value = "오늘 출석 보상은 이미 받았거나 아직 수령 시간이 아닙니다."
                        load()
                    } else {
                        _message.value = "출석 보상 수령 실패 (HTTP ${response.code()})"
                    }
                }
            } catch (error: Throwable) {
                _message.value = "출석 보상 수령 실패: ${error.message ?: "알 수 없는 오류"}"
            } finally {
                _busy.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun JsonObject.text(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asString }.getOrNull() }
    }

    private fun JsonObject.boolean(vararg names: String): Boolean? = names.firstNotNullOfOrNull { name ->
        get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asBoolean }.getOrNull() }
    }
}
