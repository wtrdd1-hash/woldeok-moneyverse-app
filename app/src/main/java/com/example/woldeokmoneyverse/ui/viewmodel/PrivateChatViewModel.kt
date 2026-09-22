package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PrivateChatState(
    val loading: Boolean = false,
    val conversations: List<ChatConversationDto> = emptyList(),
    val selectedConversationId: String? = null,
    val messages: List<ChatMessageDto> = emptyList(),
    val totalUnread: Int = 0,
    val error: String? = null,
    val notice: String? = null
)

class PrivateChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(PrivateChatState())
    val state: StateFlow<PrivateChatState> = _state.asStateFlow()

    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { ApiClient.api.getPrivateChats() }
            .onSuccess { response ->
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val selected = _state.value.selectedConversationId
                        ?.takeIf { id -> body.conversations.any { it.conversationId == id } }
                        ?: body.conversations.firstOrNull()?.conversationId
                    _state.value = _state.value.copy(loading = false, conversations = body.conversations, selectedConversationId = selected, totalUnread = body.totalUnread, error = null)
                    if (selected != null) loadMessages(selected)
                } else _state.value = _state.value.copy(loading = false, error = "쪽지 목록 조회 실패 (${response.code()})")
            }
            .onFailure { _state.value = _state.value.copy(loading = false, error = it.message ?: "쪽지 목록 조회 실패") }
    }

    fun select(conversationId: String) {
        _state.value = _state.value.copy(selectedConversationId = conversationId, messages = emptyList(), error = null)
        loadMessages(conversationId)
    }

    fun open(peerUserId: String) = viewModelScope.launch {
        if (peerUserId.isBlank()) return@launch
        runCatching { ApiClient.api.openPrivateChat(OpenChatRequest(peerUserId.trim())) }
            .onSuccess { response ->
                if (response.isSuccessful && response.body() != null) {
                    _state.value = _state.value.copy(selectedConversationId = response.body()!!.conversationId, notice = "대화방을 열었습니다.")
                    load()
                } else _state.value = _state.value.copy(error = "대화방 생성 실패 (${response.code()})")
            }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "대화방 생성 실패") }
    }

    private fun loadMessages(conversationId: String) = viewModelScope.launch {
        runCatching { ApiClient.api.getPrivateChatMessages(conversationId) }
            .onSuccess { response ->
                if (response.isSuccessful && response.body() != null) {
                    val messages = response.body()!!.messages
                    _state.value = _state.value.copy(messages = messages, error = null)
                    messages.lastOrNull()?.sequence?.toIntOrNull()?.let { sequence ->
                        runCatching { ApiClient.api.markPrivateChatRead(conversationId, MarkChatReadRequest(sequence)) }
                    }
                } else _state.value = _state.value.copy(error = "쪽지 조회 실패 (${response.code()})")
            }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "쪽지 조회 실패") }
    }

    fun send(body: String) = viewModelScope.launch {
        val id = _state.value.selectedConversationId ?: return@launch
        val text = body.trim().take(2000)
        if (text.isBlank()) return@launch
        runCatching { ApiClient.api.sendPrivateChatMessage(id, SendChatMessageRequest(text)) }
            .onSuccess { response ->
                if (response.isSuccessful) {
                    _state.value = _state.value.copy(notice = "쪽지를 보냈습니다.")
                    loadMessages(id); load()
                } else _state.value = _state.value.copy(error = "쪽지 전송 실패 (${response.code()})")
            }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "쪽지 전송 실패") }
    }

    fun toggleMute() = viewModelScope.launch {
        val current = currentConversation() ?: return@launch
        val next = !current.muted
        val r = runCatching { ApiClient.api.mutePrivateChat(current.conversationId, ChatToggleRequest(muted = next)) }
        if (r.getOrNull()?.isSuccessful == true) { _state.value = _state.value.copy(notice = if (next) "알림을 음소거했습니다." else "알림을 다시 켰습니다."); load() }
        else _state.value = _state.value.copy(error = "음소거 설정 변경 실패")
    }

    fun toggleBlock() = viewModelScope.launch {
        val current = currentConversation() ?: return@launch
        val result = if (current.isPeerBlocked) runCatching { ApiClient.api.unblockPrivateChatUser(current.peerUserId) } else runCatching { ApiClient.api.blockPrivateChatUser(current.peerUserId) }
        if (result.getOrNull()?.isSuccessful == true) { _state.value = _state.value.copy(notice = if (current.isPeerBlocked) "차단을 해제했습니다." else "회원을 차단했습니다."); load() }
        else _state.value = _state.value.copy(error = "차단 설정 변경 실패")
    }

    fun archive() = viewModelScope.launch {
        val current = currentConversation() ?: return@launch
        val r = runCatching { ApiClient.api.archivePrivateChat(current.conversationId, ChatToggleRequest(archived = true)) }
        if (r.getOrNull()?.isSuccessful == true) { _state.value = _state.value.copy(selectedConversationId = null, messages = emptyList(), notice = "대화방을 보관했습니다."); load() }
        else _state.value = _state.value.copy(error = "대화방 보관 실패")
    }

    fun report(details: String) = viewModelScope.launch {
        val current = currentConversation() ?: return@launch
        val text = details.trim().take(2000)
        if (text.length < 2) return@launch
        runCatching { ApiClient.api.reportPrivateChat(current.conversationId, ChatReportRequest("other", text)) }
            .onSuccess { response -> if (response.isSuccessful) _state.value = _state.value.copy(notice = "신고가 접수되었습니다.") else _state.value = _state.value.copy(error = "신고 접수 실패 (${response.code()})") }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "신고 접수 실패") }
    }

    fun clearNotice() { _state.value = _state.value.copy(notice = null) }

    private fun currentConversation(): ChatConversationDto? {
        val id = _state.value.selectedConversationId ?: return null
        return _state.value.conversations.firstOrNull { it.conversationId == id }
    }
}