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
    val recommended: Boolean,
    val dailyLimit: Int,
    val takenToday: Int
) {
    val remainingToday: Int
        get() = if (dailyLimit > 0) (dailyLimit - takenToday).coerceAtLeast(0) else Int.MAX_VALUE

    val dailyQuotaReached: Boolean
        get() = dailyLimit > 0 && takenToday >= dailyLimit
}

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
    private val _featureState = MutableStateFlow("enabled")
    val featureState: StateFlow<String> = _featureState.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun load() = viewModelScope.launch {
        val profileResponse = runCatching { ApiClient.api.contractGet("app-api/v1/work/profile") }.getOrNull()
        if (profileResponse?.isSuccessful == true) {
            val profile = profileResponse.body()?.takeIf { it.isJsonObject }?.asJsonObject
            val activeJob = profile?.get("active_job")?.takeIf { it.isJsonObject }?.asJsonObject
            _selectedJob.value = string(activeJob, "job_type", "jobType")
        } else if (profileResponse != null) {
            _message.value = profileResponse.code().toString()
        }

        runCatching { ApiClient.api.contractGet("app-api/v1/work/tasks") }
            .onSuccess { response ->
                if (!response.isSuccessful) {
                    _message.value = response.code().toString()
                    return@onSuccess
                }
                val root = response.body()?.asJsonObject
                _featureState.value = string(root, "featureState", "feature_state") ?: "disabled"
                val allTasks = root?.getAsJsonArray("tasks")?.mapNotNull(::parseTask).orEmpty()
                val activeJob = _selectedJob.value
                _tasks.value = if (activeJob.isNullOrBlank()) allTasks else allTasks.filter { it.jobType == activeJob }
            }
            .onFailure { _message.value = "네트워크 오류" }
    }

    fun selectCareer(code: String) = viewModelScope.launch {
        if (_featureState.value != "enabled") {
            _message.value = "직업 기능이 관리자에 의해 제한되어 있습니다."
            return@launch
        }
        _busy.value = true
        val body = JsonObject().apply { addProperty("jobType", code) }
        runCatching { ApiClient.api.contractPost("app-api/v1/work/active-job", body) }
            .onSuccess { response ->
                if (response.isSuccessful) {
                    _selectedJob.value = code
                    _message.value = "직업이 ${careers.firstOrNull { it.code == code }?.label ?: code}(으)로 변경되었습니다."
                    load()
                } else {
                    _message.value = response.code().toString()
                }
            }
            .onFailure { _message.value = "네트워크 오류" }
        _busy.value = false
    }

    fun completeTask(task: WorkTaskUi) = viewModelScope.launch {
        if (_featureState.value != "enabled") {
            _message.value = "직업 기능이 관리자에 의해 제한되어 있습니다."
            return@launch
        }
        val activeJob = _selectedJob.value
        if (!activeJob.isNullOrBlank() && task.jobType != activeJob) {
            _message.value = "현재 직업에서 수행할 수 없는 작업입니다."
            return@launch
        }
        if (task.dailyQuotaReached) {
            _message.value = "오늘 이 작업의 수행 한도를 모두 사용했습니다."
            return@launch
        }
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
                    _message.value = response.code().toString()
                    load()
                }
            }
            .onFailure { _message.value = "네트워크 오류" }
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
            recommended = bool(obj, "recommended"),
            dailyLimit = int(obj, "dailyLimit", "daily_limit"),
            takenToday = int(obj, "takenToday", "taken_today")
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
