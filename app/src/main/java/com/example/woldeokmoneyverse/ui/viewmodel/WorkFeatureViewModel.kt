package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.data.remote.apiProblem
import com.example.woldeokmoneyverse.data.remote.koreanApiProblem
import com.example.woldeokmoneyverse.util.formatMoneyAmount
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
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
        get() = if (dailyLimit <= 0) Int.MAX_VALUE else (dailyLimit - takenToday).coerceAtLeast(0)

    val quotaReached: Boolean
        get() = dailyLimit > 0 && takenToday >= dailyLimit
}

data class WorkAssignmentUi(
    val assignmentId: String,
    val taskId: String,
    val jobType: String,
    val name: String,
    val status: String, // "assigned" or "submitted"
    val assignedAt: Long,
    val expiresAt: Long,
    val minimumDurationSeconds: Int
) {
    fun secondsRemaining(now: Long = System.currentTimeMillis()): Int {
        if (assignedAt <= 0L) return 0
        val elapsedSeconds = ((now - assignedAt) / 1000L).toInt()
        return (minimumDurationSeconds - elapsedSeconds).coerceAtLeast(0)
    }

    fun isSubmittable(now: Long = System.currentTimeMillis()): Boolean = secondsRemaining(now) <= 0
    fun isSubmitted(): Boolean = status.equals("submitted", ignoreCase = true)
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = expiresAt in 1..now
}

data class WorkReceiptUi(
    val receiptId: String,
    val taskId: String,
    val taskName: String,
    val rewardAmount: String,
    val experienceAmount: String,
    val completedAt: Long,
    val idempotencyKey: String
)

data class JobProfileUi(
    val jobType: String,
    val title: String,
    val level: Int,
    val experience: Long,
    val nextLevelExp: Long,
    val tasksCompleted: Int
) {
    val progressPercent: Float
        get() = if (nextLevelExp <= 0L) 1.0f else (experience.toFloat() / nextLevelExp.toFloat()).coerceIn(0f, 1f)
}

data class CareerUi(val code: String, val label: String)

class WorkFeatureViewModel : ViewModel() {
    // 백엔드 enum과 100% 일치하는 8대 전문 직업군
    val careers = listOf(
        CareerUi("developer", "기술자·소프트웨어"),
        CareerUi("trader", "트레이더·금융"),
        CareerUi("entertainer", "크리에이터·엔터"),
        CareerUi("detective", "탐정·수사관"),
        CareerUi("miner", "자원 광부"),
        CareerUi("farmer", "스마트 농부"),
        CareerUi("artisan", "공방 장인"),
        CareerUi("civil_servant", "행정 공무원")
    )

    private val _selectedJob = MutableStateFlow<String?>(null)
    val selectedJob: StateFlow<String?> = _selectedJob.asStateFlow()

    private val _tasks = MutableStateFlow<List<WorkTaskUi>>(emptyList())
    val tasks: StateFlow<List<WorkTaskUi>> = _tasks.asStateFlow()

    private val _activeAssignments = MutableStateFlow<List<WorkAssignmentUi>>(emptyList())
    val activeAssignments: StateFlow<List<WorkAssignmentUi>> = _activeAssignments.asStateFlow()

    private val _receipts = MutableStateFlow<List<WorkReceiptUi>>(emptyList())
    val receipts: StateFlow<List<WorkReceiptUi>> = _receipts.asStateFlow()

    private val _jobProfile = MutableStateFlow<JobProfileUi?>(null)
    val jobProfile: StateFlow<JobProfileUi?> = _jobProfile.asStateFlow()

    private val _featureState = MutableStateFlow("enabled")
    val featureState: StateFlow<String> = _featureState.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _rewardQuotaReached = MutableStateFlow(false)
    val rewardQuotaReached: StateFlow<Boolean> = _rewardQuotaReached.asStateFlow()

    private val _rewardQuotaSummary = MutableStateFlow<String?>(null)
    val rewardQuotaSummary: StateFlow<String?> = _rewardQuotaSummary.asStateFlow()

    fun load() = viewModelScope.launch {
        // 1. 근무 쿼터 및 대시보드
        val dashboardResponse = runCatching { ApiClient.api.contractGet("app-api/v1/work") }.getOrNull()
        if (dashboardResponse?.isSuccessful == true) {
            val root = dashboardResponse.body()?.takeIf { it.isJsonObject }?.asJsonObject
            val dailyPaid = string(root, "dailyPaid", "daily_paid") ?: "0"
            val dailyCap = string(root, "dailyCap", "daily_cap") ?: "0"
            val weeklyPaid = string(root, "weeklyPaid", "weekly_paid") ?: "0"
            val weeklyCap = string(root, "weeklyCap", "weekly_cap") ?: "0"
            val dailyReached = quotaReached(dailyPaid, dailyCap)
            val weeklyReached = quotaReached(weeklyPaid, weeklyCap)
            _rewardQuotaReached.value = dailyReached || weeklyReached
            _rewardQuotaSummary.value = "오늘 ${formatMoneyAmount(dailyPaid)} / ${formatMoneyAmount(dailyCap)} WLD · 주간 ${formatMoneyAmount(weeklyPaid)} / ${formatMoneyAmount(weeklyCap)} WLD"
        }

        // 2. 현재 활성 직업 프로필
        val profileResponse = runCatching { ApiClient.api.contractGet("app-api/v1/work/profile") }.getOrNull()
        if (profileResponse?.isSuccessful == true) {
            val profile = profileResponse.body()?.takeIf { it.isJsonObject }?.asJsonObject
            val activeJob = profile?.get("active_job")?.takeIf { it.isJsonObject }?.asJsonObject
            val jobType = string(activeJob, "job_type", "jobType")
            _selectedJob.value = jobType

            val level = int(profile ?: JsonObject(), "job_level", "jobLevel", "level").coerceAtLeast(1)
            val exp = long(profile, "job_experience", "jobExperience", "experience")
            val nextExp = long(profile, "next_level_exp", "nextLevelExp").takeIf { it > 0L } ?: (level * 500L)
            val completed = int(profile ?: JsonObject(), "tasks_completed", "tasksCompleted", "totalTasksCompleted")
            val label = careers.firstOrNull { it.code == jobType }?.label ?: (jobType ?: "견습")
            val title = when {
                level >= 10 -> "마스터 $label"
                level >= 7 -> "수석 $label"
                level >= 4 -> "시니어 $label"
                level >= 2 -> "주니어 $label"
                else -> "수습 $label"
            }
            _jobProfile.value = JobProfileUi(
                jobType = jobType ?: "",
                title = title,
                level = level,
                experience = exp,
                nextLevelExp = nextExp,
                tasksCompleted = completed
            )
        }

        // 3. 작업 목록 조회
        val tasksResponse = runCatching { ApiClient.api.contractGet("app-api/v1/work/tasks") }.getOrNull()
        if (tasksResponse?.isSuccessful == true) {
            val root = tasksResponse.body()?.asJsonObject
            _featureState.value = string(root, "featureState", "feature_state") ?: "enabled"
            val allTasks = root?.getAsJsonArray("tasks")?.mapNotNull(::parseTask).orEmpty()
            val activeJob = _selectedJob.value
            _tasks.value = if (activeJob.isNullOrBlank()) allTasks else allTasks.filter { it.jobType == activeJob }
        }

        // 4. 진행 중인 과제 목록 조회 (assignments)
        val assignmentsRes = runCatching { ApiClient.api.contractGet("app-api/v1/work/assignments") }.getOrNull()
        if (assignmentsRes?.isSuccessful == true) {
            val root = assignmentsRes.body()?.takeIf { it.isJsonObject }?.asJsonObject
            val arr = root?.getAsJsonArray("assignments")
            val parsed = arr?.mapNotNull(::parseAssignment).orEmpty()
            _activeAssignments.value = parsed.filter { it.status.equals("assigned", ignoreCase = true) || it.status.equals("submitted", ignoreCase = true) }
        }

        // 5. 최근 정산 영수증 이력 조회 (receipts)
        val receiptsRes = runCatching { ApiClient.api.contractGet("app-api/v1/work/receipts") }.getOrNull()
        if (receiptsRes?.isSuccessful == true) {
            val root = receiptsRes.body()?.takeIf { it.isJsonObject }?.asJsonObject
            val arr = root?.getAsJsonArray("receipts")
            _receipts.value = arr?.mapNotNull(::parseReceipt).orEmpty()
        }
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
                    _message.value = koreanApiProblem(apiProblem(response), "직업 변경")
                }
            }
            .onFailure { _message.value = "네트워크 오류: 직업 변경에 실패했습니다." }
        _busy.value = false
    }

    /**
     * 과제 수주 -> 제출 -> 검증 3단계 스마트 오케스트레이션
     */
    fun completeTask(task: WorkTaskUi) = viewModelScope.launch {
        if (_featureState.value != "enabled") {
            _message.value = "직업 기능이 관리자에 의해 제한되어 있습니다."
            return@launch
        }
        if (_rewardQuotaReached.value) {
            _message.value = "오늘 또는 이번 주 근무 보상 한도에 도달했습니다. ${_rewardQuotaSummary.value.orEmpty()}".trim()
            return@launch
        }
        if (task.quotaReached) {
            _message.value = "오늘 이 작업의 수행 한도를 모두 사용했습니다."
            return@launch
        }
        val activeJob = _selectedJob.value
        if (!activeJob.isNullOrBlank() && task.jobType != activeJob) {
            _message.value = "현재 활성 직업에서 수행할 수 없는 작업입니다."
            return@launch
        }

        _busy.value = true

        // 1단계 확인: 이미 수주되어 진행 중인 동일 task assignment가 있는지 검사
        val existing = _activeAssignments.value.firstOrNull { it.taskId == task.id }
        if (existing != null) {
            if (existing.isSubmitted()) {
                // 이미 제출되었으면 바로 보상 수령(verify)
                claimRewardInternal(existing.assignmentId, task.name)
            } else if (existing.isSubmittable()) {
                // 제출 가능 상태면 바로 제출(submit) -> 보상 수령(verify)
                submitAndClaimInternal(existing.assignmentId, task.name)
            } else {
                val remaining = existing.secondsRemaining()
                _message.value = "아직 최소 수행 시간($remaining 초)이 지나지 않았습니다. 잠시 후 완료 버튼을 눌러주세요."
            }
            _busy.value = false
            return@launch
        }

        // 2단계: 신규 과제 수주 (POST /app-api/v1/work/assignments)
        val assignBody = JsonObject().apply {
            addProperty("taskId", task.id)
            addProperty("idempotencyKey", UUID.randomUUID().toString())
        }
        val assignRes = runCatching { ApiClient.api.contractPost("app-api/v1/work/assignments", assignBody) }.getOrNull()
        if (assignRes == null || !assignRes.isSuccessful) {
            _message.value = if (assignRes != null) koreanApiProblem(apiProblem(assignRes), "업무 수주") else "네트워크 오류: 업무 수주에 실패했습니다."
            _busy.value = false
            load()
            return@launch
        }

        val assignPayload = assignRes.body()?.takeIf { it.isJsonObject }?.asJsonObject
        val assignmentId = string(assignPayload, "assignmentId", "assignment_id")

        if (assignmentId.isNullOrBlank()) {
            _message.value = "업무를 수주했습니다. 과제 목록을 갱신합니다."
            load()
            _busy.value = false
            return@launch
        }

        // 최소 수행 시간이 0초이거나 즉시 제출 가능한 경우 바로 제출 및 보상 수령까지 완결
        if (task.minimumDurationSeconds <= 0) {
            submitAndClaimInternal(assignmentId, task.name)
        } else {
            _message.value = "'${task.name}' 업무를 수주했습니다. 최소 ${task.minimumDurationSeconds}초 후 제출 가능합니다."
            load()
        }

        _busy.value = false
    }

    /**
     * 진행 중인 assignment를 제출하고 즉시 보상 정산
     */
    fun submitAssignment(assignment: WorkAssignmentUi) = viewModelScope.launch {
        if (!assignment.isSubmittable()) {
            _message.value = "아직 최소 수행 시간(${assignment.secondsRemaining()}초)이 남았습니다."
            return@launch
        }
        _busy.value = true
        if (assignment.isSubmitted()) {
            claimRewardInternal(assignment.assignmentId, assignment.name)
        } else {
            submitAndClaimInternal(assignment.assignmentId, assignment.name)
        }
        _busy.value = false
    }

    /**
     * 제출(submit) -> 검증(verify) 연속 처리
     */
    private suspend fun submitAndClaimInternal(assignmentId: String, taskName: String) {
        // 1) Submit
        val submitBody = JsonObject().apply {
            addProperty("idempotencyKey", UUID.randomUUID().toString())
        }
        val submitRes = runCatching {
            ApiClient.api.contractPost("app-api/v1/work/assignments/$assignmentId/completions", submitBody)
        }.getOrNull()

        if (submitRes == null || !submitRes.isSuccessful) {
            _message.value = if (submitRes != null) koreanApiProblem(apiProblem(submitRes), "업무 제출") else "네트워크 오류: 업무 제출에 실패했습니다."
            load()
            return
        }

        // 2) Verify & Claim
        claimRewardInternal(assignmentId, taskName)
    }

    /**
     * 검증(verify) 호출하여 WLD/EXP 확정 정산
     */
    private suspend fun claimRewardInternal(assignmentId: String, taskName: String) {
        val verifyBody = JsonObject().apply {
            addProperty("idempotencyKey", UUID.randomUUID().toString())
        }
        val verifyRes = runCatching {
            ApiClient.api.contractPost("app-api/v1/work/assignments/$assignmentId/verify", verifyBody)
        }.getOrNull()

        if (verifyRes != null && verifyRes.isSuccessful) {
            val payload = verifyRes.body()?.takeIf { it.isJsonObject }?.asJsonObject
            val reward = string(payload, "rewardAmount", "reward_amount") ?: "0"
            val exp = string(payload, "experienceAmount", "experience_amount") ?: "0"
            _message.value = "✓ '$taskName' 근무 완료! +${formatMoneyAmount(reward)} WLD / +$exp EXP 수령"
            load()
        } else {
            _message.value = if (verifyRes != null) koreanApiProblem(apiProblem(verifyRes), "보상 정산") else "네트워크 오류: 보상 정산에 실패했습니다."
            load()
        }
    }

    private fun quotaReached(paid: String, cap: String): Boolean {
        val paidValue = paid.toBigIntegerOrNull() ?: BigInteger.ZERO
        val capValue = cap.toBigIntegerOrNull() ?: BigInteger.ZERO
        return capValue.signum() > 0 && paidValue >= capValue
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

    private fun parseAssignment(element: JsonElement): WorkAssignmentUi? {
        val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return null
        val assignmentId = string(obj, "assignmentId", "assignment_id") ?: return null
        val taskId = string(obj, "taskId", "task_id") ?: ""
        val jobType = string(obj, "jobType", "job_type") ?: ""
        val name = string(obj, "name") ?: "진행 중인 업무"
        val status = string(obj, "status") ?: "assigned"
        val assignedAtStr = string(obj, "assignedAt", "assigned_at")
        val expiresAtStr = string(obj, "expiresAt", "expires_at")
        val minDuration = int(obj, "minimumDurationSeconds", "minimum_duration_seconds")

        return WorkAssignmentUi(
            assignmentId = assignmentId,
            taskId = taskId,
            jobType = jobType,
            name = name,
            status = status,
            assignedAt = parseIsoToMillis(assignedAtStr),
            expiresAt = parseIsoToMillis(expiresAtStr),
            minimumDurationSeconds = minDuration
        )
    }

    private fun parseReceipt(element: JsonElement): WorkReceiptUi? {
        val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return null
        val receiptId = string(obj, "receiptId", "receipt_id", "id") ?: return null
        val taskId = string(obj, "taskId", "task_id") ?: ""
        val taskName = string(obj, "taskName", "task_name", "name") ?: "근무 과제"
        val reward = string(obj, "rewardAmount", "reward_amount", "reward") ?: "0"
        val exp = string(obj, "experienceAmount", "experience_amount", "experience") ?: "0"
        val completedAtStr = string(obj, "completedAt", "completed_at", "createdAt", "created_at")
        val idempotencyKey = string(obj, "idempotencyKey", "idempotency_key") ?: ""

        return WorkReceiptUi(
            receiptId = receiptId,
            taskId = taskId,
            taskName = taskName,
            rewardAmount = reward,
            experienceAmount = exp,
            completedAt = parseIsoToMillis(completedAtStr),
            idempotencyKey = idempotencyKey
        )
    }

    private fun parseIsoToMillis(iso: String?): Long {
        if (iso.isNullOrBlank()) return 0L
        return runCatching {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(iso.take(19))?.time ?: 0L
        }.getOrDefault(0L)
    }

    private fun string(obj: JsonObject?, vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        obj?.get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asString }.getOrNull() }
    }

    private fun int(obj: JsonObject, vararg names: String): Int = names.firstNotNullOfOrNull { name ->
        obj.get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asInt }.getOrNull() }
    } ?: 0

    private fun long(obj: JsonObject?, vararg names: String): Long = names.firstNotNullOfOrNull { name ->
        obj?.get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asLong }.getOrNull() }
    } ?: 0L

    private fun bool(obj: JsonObject, vararg names: String): Boolean = names.firstNotNullOfOrNull { name ->
        obj.get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asBoolean }.getOrNull() }
    } ?: false
}
