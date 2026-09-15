package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.woldeokmoneyverse.data.remote.LobbyMessageDto
import com.example.woldeokmoneyverse.data.remote.RealtimeLobbyClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LobbyChatState(
    val connected: Boolean = false,
    val canChat: Boolean = false,
    val online: Int = 0,
    val messages: List<LobbyMessageDto> = emptyList(),
    val error: String? = null
)

class LobbyChatViewModel : ViewModel() {
    private val client = RealtimeLobbyClient()
    private val _state = MutableStateFlow(LobbyChatState())
    val state: StateFlow<LobbyChatState> = _state.asStateFlow()
    private var started = false

    fun connect() {
        if (started) return
        started = true
        client.connect(
            onConnection = { value -> _state.value = _state.value.copy(connected = value) },
            onPermission = { value -> _state.value = _state.value.copy(canChat = value) },
            onOnline = { value -> _state.value = _state.value.copy(online = value.coerceAtLeast(0)) },
            onMessage = { msg -> _state.value = _state.value.copy(messages = (_state.value.messages + msg).takeLast(100), error = null) },
            onError = { message -> _state.value = _state.value.copy(error = message) }
        )
    }

    fun send(text: String) {
        if (_state.value.canChat && _state.value.connected) client.send(text)
    }

    override fun onCleared() {
        client.close()
        super.onCleared()
    }
}
