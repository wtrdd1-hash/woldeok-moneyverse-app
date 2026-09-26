package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val accountRepo: AccountRepository = AccountRepository()
) : ViewModel() {

    private val _sessionsState = MutableStateFlow<UiState<List<AccountSessionDto>>>(UiState.Loading)
    val sessionsState: StateFlow<UiState<List<AccountSessionDto>>> = _sessionsState.asStateFlow()

    private val _securityLogsState = MutableStateFlow<UiState<List<SecurityLogDto>>>(UiState.Loading)
    val securityLogsState: StateFlow<UiState<List<SecurityLogDto>>> = _securityLogsState.asStateFlow()

    private val _twoFactorSetupState = MutableStateFlow<UiState<TwoFactorSetupResponse>>(UiState.Empty)
    val twoFactorSetupState: StateFlow<UiState<TwoFactorSetupResponse>> = _twoFactorSetupState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun loadSessions() {
        viewModelScope.launch {
            _sessionsState.value = UiState.Loading
            accountRepo.getSessions().fold(
                onSuccess = { _sessionsState.value = UiState.Success(it) },
                onFailure = { _sessionsState.value = UiState.Error(it.message ?: "로그인 세션 목록 조회 실패") }
            )
        }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            _busy.value = true
            accountRepo.revokeSession(sessionId).fold(
                onSuccess = {
                    _message.value = it.message ?: "기기 세션을 원격 로그아웃했습니다."
                    loadSessions()
                },
                onFailure = { _message.value = "세션 원격 로그아웃 실패: ${it.message}" }
            )
            _busy.value = false
        }
    }

    fun revokeOtherSessions() {
        viewModelScope.launch {
            _busy.value = true
            accountRepo.revokeOtherSessions().fold(
                onSuccess = {
                    _message.value = it.message ?: "현재 기기를 제외한 모든 타 기기에서 로그아웃했습니다."
                    loadSessions()
                },
                onFailure = { _message.value = "타 기기 일괄 로그아웃 실패: ${it.message}" }
            )
            _busy.value = false
        }
    }

    fun loadSecurityLogs() {
        viewModelScope.launch {
            _securityLogsState.value = UiState.Loading
            accountRepo.getSecurityLogs().fold(
                onSuccess = { _securityLogsState.value = UiState.Success(it) },
                onFailure = { _securityLogsState.value = UiState.Error(it.message ?: "보안 로그 조회 실패") }
            )
        }
    }

    fun changePassword(current: String, new: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            accountRepo.changePassword(current, new).fold(
                onSuccess = {
                    _message.value = "비밀번호가 성공적으로 변경되었습니다."
                    onDone()
                    loadSecurityLogs()
                },
                onFailure = { _message.value = "비밀번호 변경 실패: ${it.message}" }
            )
            _busy.value = false
        }
    }

    fun startTwoFactorSetup() {
        viewModelScope.launch {
            _twoFactorSetupState.value = UiState.Loading
            accountRepo.setupTwoFactor().fold(
                onSuccess = { _twoFactorSetupState.value = UiState.Success(it) },
                onFailure = { _twoFactorSetupState.value = UiState.Error(it.message ?: "2FA 키 발급 실패") }
            )
        }
    }

    fun verifyTwoFactor(code: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            accountRepo.verifyTwoFactor(code).fold(
                onSuccess = {
                    _message.value = "2FA 2단계 인증이 활성화되었습니다."
                    _twoFactorSetupState.value = UiState.Empty
                    onDone()
                },
                onFailure = { _message.value = "2FA 인증 실패: ${it.message}" }
            )
            _busy.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    // --- v8: Notifications Governance State & Actions ---
    private val _notificationsState = MutableStateFlow<UiState<List<NotificationDto>>>(UiState.Loading)
    val notificationsState: StateFlow<UiState<List<NotificationDto>>> = _notificationsState.asStateFlow()

    private val _notificationPreferencesState = MutableStateFlow<UiState<NotificationPreferencesDto>>(UiState.Loading)
    val notificationPreferencesState: StateFlow<UiState<NotificationPreferencesDto>> = _notificationPreferencesState.asStateFlow()

    private val _safetyState = MutableStateFlow<UiState<AccountSafetyDto>>(UiState.Loading)
    val safetyState: StateFlow<UiState<AccountSafetyDto>> = _safetyState.asStateFlow()

    private val _takedownStatusState = MutableStateFlow<UiState<TakedownStatusResponse>>(UiState.Empty)
    val takedownStatusState: StateFlow<UiState<TakedownStatusResponse>> = _takedownStatusState.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationsState.value = UiState.Loading
            accountRepo.getNotifications().fold(
                onSuccess = { _notificationsState.value = UiState.Success(it) },
                onFailure = { _notificationsState.value = UiState.Error(it.message ?: "알림 목록 조회 실패") }
            )
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            _busy.value = true
            accountRepo.markAllNotificationsRead().fold(
                onSuccess = {
                    _message.value = "모든 알림을 읽음 처리했습니다."
                    loadNotifications()
                },
                onFailure = { _message.value = "알림 읽음 처리 실패: ${it.message}" }
            )
            _busy.value = false
        }
    }

    fun loadNotificationPreferences() {
        viewModelScope.launch {
            _notificationPreferencesState.value = UiState.Loading
            accountRepo.getNotificationPreferences().fold(
                onSuccess = { _notificationPreferencesState.value = UiState.Success(it) },
                onFailure = { _notificationPreferencesState.value = UiState.Error(it.message ?: "알림 수신 설정 조회 실패") }
            )
        }
    }

    fun updateNotificationPreferences(marketing: Boolean, activity: Boolean, quest: Boolean, maintenance: Boolean) {
        viewModelScope.launch {
            _busy.value = true
            accountRepo.updateNotificationPreferences(UpdateNotificationPreferencesRequest(marketing, activity, quest, maintenance)).fold(
                onSuccess = {
                    _notificationPreferencesState.value = UiState.Success(it)
                    _message.value = "알림 수신 설정이 저장되었습니다."
                },
                onFailure = { _message.value = "알림 설정 저장 실패: ${it.message}" }
            )
            _busy.value = false
        }
    }

    fun loadAccountSafety() {
        viewModelScope.launch {
            _safetyState.value = UiState.Loading
            accountRepo.getAccountSafety().fold(
                onSuccess = { _safetyState.value = UiState.Success(it) },
                onFailure = { _safetyState.value = UiState.Error(it.message ?: "계정 안전 상태 조회 실패") }
            )
        }
    }

    fun submitTakedown(targetUrl: String, reason: String, email: String, passwordHash: String, onDone: (String) -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            accountRepo.submitTakedown(TakedownRequest(targetUrl, reason, email, passwordHash)).fold(
                onSuccess = {
                    _message.value = "긴급 삭제 접수 완료! 접수번호: ${it.trackingId}"
                    onDone(it.trackingId)
                },
                onFailure = { _message.value = "긴급 삭제 접수 실패: ${it.message}" }
            )
            _busy.value = false
        }
    }

    fun checkTakedownStatus(trackingId: String, password: String) {
        viewModelScope.launch {
            _takedownStatusState.value = UiState.Loading
            accountRepo.getTakedownStatus(trackingId, password).fold(
                onSuccess = { _takedownStatusState.value = UiState.Success(it) },
                onFailure = { _takedownStatusState.value = UiState.Error(it.message ?: "긴급 삭제 상태 조회 실패") }
            )
        }
    }
}

