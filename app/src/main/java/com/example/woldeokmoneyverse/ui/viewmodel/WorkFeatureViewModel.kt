package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class WorkTaskUi(
    val id: String,
    val name: String,
    val description: String,
    val jobType: String,
    val reward: String,
    val experience: String,
    val minimumDurationSeconds: Int,
    val recommended: Boolean
)

data class CareerUi(val code: String, val label: String)

class WorkFeatureViewModel : ViewModel() {
    val careers = listOf(
        CareerUi("developer", "개발자"), CareerUi("trader", "트레이더"),
        CareerUi("entertainer", "엔터테이너"), CareerUi("detective", "탐정"),
        CareerUi("miner", "광부"), CareerUi("farmer", "농부"),
        CareerUi("artisan", "장인"), CareerUi("civil_servant", "공무원")
    )

    private val _selectedJob = MutableStateFlow<String?>(null)
    val selectedJob: StateFlow<String?> = _selectedJob.asStateFlow()
    private val _tasks = MutableStateFlow<List<WorkTaskUi>>(emptyList())
    val tasks: StateFlow<List<WorkTaskUi>> = _tasks.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun load() = viewModelScope.launch {
        runCatching { ApiClient.api.contractGet("app-api/v1/work/tasks") }
            .onSuccess { response ->
                if (!response.isSuccessful) {
                    _message.value = "작업 목록 조회 실패 (HTTP ${response.code()})"
                    return@onSuccess
                }
                val root = response.body()?.asJsonObject
                _tasks.value = root?.getAsJsonArray("tasks")?.mapNotNull(::parseTask).orEmpty()
            }
            .onFailure { _message.value = "작업 목록 조회 실패: ${it.message}" }
    }

    fun selectCareer(code: String) = viewModelScope.launch {
        _busy.value = true
        val body = JsonObject().apply { addProperty("jobType", code) }
        runCatching { ApiClient.api.contractPost("app-api/v1/work/active-job", body) }
            .onSuccess { response ->
                if (response.isSuccessful) {
                    _selectedJob.value = code
                    _message.value = "직업이 ${careers.firstOrNull { it.code == code }?.label ?: code}(으)로 변경되었습니다."
                    load()
                } else {
                    _message.value = "직업 변경 실패 (HTTP ${response.code()})"
                }
            }
            .onFailure { _message.value = "직업 변경 실패: ${it.message}" }
        _busy.value = false
    }

    fun completeTask(task: WorkTaskUi) = viewModelScope.launch {
        _busy.value = true
        val body = JsonObject().apply { addProperty("idempotencyKey", UUID.randomUUID().toString()) }
        runCatching { ApiClient.api.contractPost("app-api/v1/work/tasks/${task.id}/complete", body) }
            .onSuccess { response ->
                if (response.isSuccessful) {
                    val payload = response.body()?.asJsonObject
                    val reward = string(payload, "rewardAmount", "reward_amount") ?: task.reward
                    val exp = string(payload, "experienceGained", "experience_gained") ?: task.experience
                    _message.value = "근무 완료: +$reward WLD / +$exp EXP"
                    load()
                } else {
                    _message.value = "근무 완료 실패 (HTTP ${response.code()}): ${response.errorBody()?.string().orEmpty()}"
                }
            }
            .onFailure { _message.value = "근무 완료 실패: ${it.message}" }
        _busy.value = false
    }

    fun clearMessage() { _message.value = null }

    private fun parseTask(element: JsonElement): WorkTaskUi? {
        val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return null
        val id = string(obj, "taskId", "task_id") ?: return null
        return WorkTaskUi(
            id = id,
            name = string(obj, "name") ?: "근무 과제",
            description = string(obj, "description") ?: "",
            jobType = string(obj, "jobType", "job_type") ?: "",
            reward = string(obj, "rewardPreview", "reward_preview", "baseReward", "base_reward") ?: "0",
            experience = string(obj, "experiencePreview", "experience_preview", "baseExperience", "base_experience") ?: "0",
            minimumDurationSeconds = int(obj, "minimumDurationSeconds", "minimum_duration_seconds"),
            recommended = bool(obj, "recommended")
        )
    }

    private fun string(obj: JsonObject?, vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        obj?.get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asString }.getOrNull() }
    }
    private fun int(obj: JsonObject, vararg names: String): Int = names.firstNotNullOfOrNull { name ->
        obj.get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asInt }.getOrNull() }
    } ?: 0
    private fun bool(obj: JsonObject, vararg names: String): Boolean = names.firstNotNullOfOrNull { name ->
        obj.get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asBoolean }.getOrNull() }
    } ?: false
}
