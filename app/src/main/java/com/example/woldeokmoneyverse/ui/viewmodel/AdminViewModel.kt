package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val loading: Boolean = true,
    val consoleOpen: Boolean = false,
    val memberCount: Int? = null,
    val workSummary: String = "확인 중",
    val threads: List<SupportThreadDto> = emptyList(),
    val selectedThreadId: String? = null,
    val messages: List<SupportMessageDto> = emptyList(),
    val activityLogs: List<AdminActivityLogDto> = emptyList(),
    val error: String? = null
)

class AdminViewModel : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    fun openAndLoad() = viewModelScope.launch {
        _state.value = AdminUiState(loading = true)
        runCatching { ApiClient.api.contractPost("app-api/v1/admin/security/sessions", JsonObject()) }
            .onFailure { _state.value = _state.value.copy(loading = false, error = it.message ?: "관리자 콘솔 연결 실패"); return@launch }
            .onSuccess { response ->
                if (!response.isSuccessful) {
                    _state.value = _state.value.copy(loading = false, error = "관리자 콘솔 연결 실패 (${response.code()})")
                    return@launch
                }
            }
        _state.value = _state.value.copy(consoleOpen = true)
        loadDashboard()
    }

    private suspend fun loadDashboard() {
        val users = runCatching { ApiClient.api.contractGet("app-api/v1/admin/users") }.getOrNull()
        val memberCount = users?.body()?.takeIf { users.isSuccessful && it.isJsonObject }?.asJsonObject
            ?.getAsJsonArray("users")?.size()
        val work = runCatching { ApiClient.api.contractGet("app-api/v1/admin/work") }.getOrNull()
        val workSummary = if (work?.isSuccessful == true) {
            val root = work.body()?.takeIf { it.isJsonObject }?.asJsonObject
            val catalogue = root?.getAsJsonArray("catalogue")?.size() ?: 0
            val levels = root?.getAsJsonArray("jobLevels")?.size() ?: 0
            "직업 ${catalogue}개 · 레벨 정책 ${levels}개"
        } else "직업 운영 API 확인 필요"
        val support = runCatching { ApiClient.api.getAdminSupportThreads() }.getOrNull()
        val threads = if (support?.isSuccessful == true) support.body()?.threads.orEmpty() else emptyList()
        val logsResponse = runCatching { ApiClient.api.getAdminActivityLogs(limit = 50) }.getOrNull()
        val activityLogs = if (logsResponse?.isSuccessful == true) logsResponse.body().orEmpty() else emptyList()
        val selected = _state.value.selectedThreadId?.takeIf { id -> threads.any { it.threadId == id } }
            ?: threads.firstOrNull()?.threadId
        _state.value = _state.value.copy(loading = false, memberCount = memberCount, workSummary = workSummary, threads = threads, selectedThreadId = selected, activityLogs = activityLogs, error = null)
        if (selected != null) loadMessages(selected)
    }

    fun refresh() = viewModelScope.launch { if (_state.value.consoleOpen) loadDashboard() else openAndLoad() }

    fun selectThread(id: String) {
        _state.value = _state.value.copy(selectedThreadId = id, messages = emptyList())
        loadMessages(id)
    }

    private fun loadMessages(id: String) = viewModelScope.launch {
        runCatching { ApiClient.api.getAdminSupportMessages(id) }
            .onSuccess { r -> if (r.isSuccessful && r.body() != null) _state.value = _state.value.copy(messages = r.body()!!.messages) else _state.value = _state.value.copy(error = "문의 대화 조회 실패 (${r.code()})") }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "문의 대화 조회 실패") }
    }

    fun reply(body: String) = viewModelScope.launch {
        val id = _state.value.selectedThreadId ?: return@launch
        if (body.isBlank()) return@launch
        runCatching { ApiClient.api.sendAdminSupportMessage(id, CreateSupportMessageRequest(body.trim().take(2000))) }
            .onSuccess { r -> if (r.isSuccessful) { loadMessages(id); loadDashboard() } else _state.value = _state.value.copy(error = "관리자 답장 실패 (${r.code()})") }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "관리자 답장 실패") }
    }

    fun setStatus(status: String) = viewModelScope.launch {
        val id = _state.value.selectedThreadId ?: return@launch
        if (status !in setOf("open", "waiting_user", "resolved")) return@launch
        runCatching { ApiClient.api.setAdminSupportStatus(id, SupportStatusRequest(status)) }
            .onSuccess { r -> if (r.isSuccessful) loadDashboard() else _state.value = _state.value.copy(error = "문의 상태 변경 실패 (${r.code()})") }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "문의 상태 변경 실패") }
    }
}
