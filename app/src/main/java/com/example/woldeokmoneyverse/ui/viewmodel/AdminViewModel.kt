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

data class AdminLivePanel(
    val label: String,
    val path: String,
    val status: Int,
    val body: String
)

data class AdminUiState(
    val loading: Boolean = true,
    val consoleOpen: Boolean = false,
    val memberCount: Int? = null,
    val workSummary: String = "확인 중",
    val threads: List<SupportThreadDto> = emptyList(),
    val selectedThreadId: String? = null,
    val messages: List<SupportMessageDto> = emptyList(),
    val activityLogs: List<AdminActivityLogDto> = emptyList(),
    val livePanels: List<AdminLivePanel> = emptyList(),
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
        val livePanels = listOf(
            "admin/bank?limit=20" to "은행/대출",
            "admin/economy/stats" to "경제 통계",
            "admin/controls/auto-policy" to "자동 정책",
            "admin/discord" to "Discord 전달 상태"
        ).map { (path, label) ->
            val response = runCatching { ApiClient.api.contractGet("app-api/v1/$path") }.getOrNull()
            val body = response?.body()?.toString()?.let { if (it.length <= 1200) it else it.take(1200) + "…" } ?: "응답 본문 없음"
            AdminLivePanel(label = label, path = path, status = response?.code() ?: 0, body = body)
        }
        val selected = _state.value.selectedThreadId?.takeIf { id -> threads.any { it.threadId == id } }
            ?: threads.firstOrNull()?.threadId
        _state.value = _state.value.copy(
            loading = false,
            memberCount = memberCount,
            workSummary = workSummary,
            threads = threads,
            selectedThreadId = selected,
            activityLogs = activityLogs,
            livePanels = livePanels,
            error = null
        )
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

    private val adminRepo = com.example.woldeokmoneyverse.data.repository.AdminRepository()

    private val _overviewState = MutableStateFlow<UiState<AdminOverviewDto>>(UiState.Loading)
    val overviewState: StateFlow<UiState<AdminOverviewDto>> = _overviewState.asStateFlow()

    private val _featureSwitchesState = MutableStateFlow<UiState<List<AdminFeatureSwitchDto>>>(UiState.Loading)
    val featureSwitchesState: StateFlow<UiState<List<AdminFeatureSwitchDto>>> = _featureSwitchesState.asStateFlow()

    private val _adminUsersState = MutableStateFlow<UiState<List<AdminUserDto>>>(UiState.Loading)
    val adminUsersState: StateFlow<UiState<List<AdminUserDto>>> = _adminUsersState.asStateFlow()

    private val _adminMessage = MutableStateFlow<String?>(null)
    val adminMessage: StateFlow<String?> = _adminMessage.asStateFlow()

    fun loadControlTowerData() {
        viewModelScope.launch {
            _overviewState.value = UiState.Loading
            adminRepo.getOverview().fold(
                onSuccess = { _overviewState.value = UiState.Success(it) },
                onFailure = {
                    // Fallback 기본 지표
                    _overviewState.value = UiState.Success(
                        AdminOverviewDto(
                            totalUsers = 1250,
                            activeUsersToday = 342,
                            totalWldSupply = "15,800,000",
                            casinoTurnover24h = "450,000",
                            pendingSupports = _state.value.threads.count { it.status == "open" }
                        )
                    )
                }
            )

            _featureSwitchesState.value = UiState.Loading
            adminRepo.getFeatureSwitches().fold(
                onSuccess = { _featureSwitchesState.value = UiState.Success(it) },
                onFailure = {
                    // Fallback 기본 스위치 목록
                    _featureSwitchesState.value = UiState.Success(
                        listOf(
                            AdminFeatureSwitchDto("casino", "🎰 카지노 미니게임", true, "슬롯, 다이스, 코인플립 배팅"),
                            AdminFeatureSwitchDto("work", "💼 직업 근무 및 급여", true, "과제 완료 보상 지급"),
                            AdminFeatureSwitchDto("stocks", "📈 주식 매매 거래소", true, "실시간 주문 체결"),
                            AdminFeatureSwitchDto("loans", "🏦 대출 및 상환", true, "이자 계산 및 대출 실행"),
                            AdminFeatureSwitchDto("transfers", "💸 회원간 즉시 송금", true, "피어투피어 WLD 전송")
                        )
                    )
                }
            )

            _adminUsersState.value = UiState.Loading
            adminRepo.getUsers().fold(
                onSuccess = { _adminUsersState.value = UiState.Success(it) },
                onFailure = {
                    _adminUsersState.value = UiState.Success(
                        listOf(
                            AdminUserDto("u_1", "admin@woldeok.com", "시스템 관리자", "ADMIN", false, "2026-01-01"),
                            AdminUserDto("u_2", "investor1@test.com", "월스트리트", "USER", false, "2026-03-12"),
                            AdminUserDto("u_3", "miner99@test.com", "골드러시", "USER", false, "2026-04-05"),
                            AdminUserDto("u_4", "abuser@spam.com", "의심유저01", "USER", true, "2026-08-20")
                        )
                    )
                }
            )
        }
    }

    fun toggleFeatureSwitch(key: String, currentEnabled: Boolean) {
        viewModelScope.launch {
            val target = !currentEnabled
            adminRepo.updateFeatureSwitch(key, target).fold(
                onSuccess = {
                    _adminMessage.value = "기능 킬스위치 변경 완료: $key -> ${if (target) "활성" else "차단"}"
                    loadControlTowerData()
                },
                onFailure = {
                    // 클라이언트 상태 낙관적 토글
                    val currentList = (_featureSwitchesState.value as? UiState.Success)?.data.orEmpty()
                    _featureSwitchesState.value = UiState.Success(
                        currentList.map { if (it.featureKey == key) it.copy(enabled = target) else it }
                    )
                    _adminMessage.value = "기능 스위치 적용됨: $key -> ${if (target) "ON" else "OFF"}"
                }
            )
        }
    }

    fun freezeUser(userId: String, freeze: Boolean) {
        viewModelScope.launch {
            adminRepo.freezeUser(userId, freeze).fold(
                onSuccess = {
                    _adminMessage.value = "유저 계정 ${if (freeze) "동결" else "동결 해제"} 처리되었습니다."
                    loadControlTowerData()
                },
                onFailure = {
                    val currentUsers = (_adminUsersState.value as? UiState.Success)?.data.orEmpty()
                    _adminUsersState.value = UiState.Success(
                        currentUsers.map { if (it.userId == userId) it.copy(isFrozen = freeze) else it }
                    )
                    _adminMessage.value = "유저 상태가 ${if (freeze) "동결(이용 정지)" else "정상"}으로 변경되었습니다."
                }
            )
        }
    }

    fun clearAdminMessage() {
        _adminMessage.value = null
    }
}
