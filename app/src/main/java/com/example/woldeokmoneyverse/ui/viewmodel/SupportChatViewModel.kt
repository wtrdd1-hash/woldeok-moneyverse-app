package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SupportChatState(
    val loading: Boolean = false,
    val threads: List<SupportThreadDto> = emptyList(),
    val selectedThreadId: String? = null,
    val messages: List<SupportMessageDto> = emptyList(),
    val error: String? = null
)

class SupportChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(SupportChatState())
    val state: StateFlow<SupportChatState> = _state.asStateFlow()

    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { ApiClient.api.getSupportThreads() }
            .onSuccess { response ->
                if (response.isSuccessful && response.body() != null) {
                    val threads = response.body()!!.threads
                    val selected = _state.value.selectedThreadId?.takeIf { id -> threads.any { it.threadId == id } }
                        ?: threads.firstOrNull()?.threadId
                    _state.value = _state.value.copy(loading = false, threads = threads, selectedThreadId = selected)
                    if (selected != null) loadMessages(selected)
                } else _state.value = _state.value.copy(loading = false, error = "문의 목록 조회 실패 (${response.code()})")
            }
            .onFailure { _state.value = _state.value.copy(loading = false, error = it.message ?: "문의 목록 조회 실패") }
    }

    fun select(threadId: String) {
        _state.value = _state.value.copy(selectedThreadId = threadId, messages = emptyList(), error = null)
        loadMessages(threadId)
    }

    private fun loadMessages(threadId: String) = viewModelScope.launch {
        runCatching { ApiClient.api.getSupportMessages(threadId) }
            .onSuccess { response ->
                if (response.isSuccessful && response.body() != null) {
                    _state.value = _state.value.copy(messages = response.body()!!.messages, error = null)
                } else _state.value = _state.value.copy(error = "문의 메시지 조회 실패 (${response.code()})")
            }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "문의 메시지 조회 실패") }
    }

    fun create(subject: String, body: String) = viewModelScope.launch {
        if (subject.isBlank() || body.isBlank()) return@launch
        runCatching { ApiClient.api.createSupportThread(CreateSupportThreadRequest(subject.trim().take(120), body.trim().take(2000))) }
            .onSuccess { response ->
                if (response.isSuccessful && response.body() != null) {
                    _state.value = _state.value.copy(selectedThreadId = response.body()!!.thread.threadId)
                    load()
                } else _state.value = _state.value.copy(error = "문의 생성 실패 (${response.code()})")
            }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "문의 생성 실패") }
    }

    fun send(body: String) = viewModelScope.launch {
        val id = _state.value.selectedThreadId ?: return@launch
        if (body.isBlank()) return@launch
        runCatching { ApiClient.api.sendSupportMessage(id, CreateSupportMessageRequest(body.trim().take(2000))) }
            .onSuccess { response -> if (response.isSuccessful) { loadMessages(id); load() } else _state.value = _state.value.copy(error = "답장 실패 (${response.code()})") }
            .onFailure { _state.value = _state.value.copy(error = it.message ?: "답장 실패") }
    }
}
