package com.example.woldeokmoneyverse.data.model

/**
 * Standard UI State wrapper required by docs/mobile-app-ui-ux-spec.ko.md
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    object Offline : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(
        val message: String,
        val isRecoverable: Boolean = true,
        val isReauthRequired: Boolean = false,
        val isSessionExpired: Boolean = false
    ) : UiState<Nothing>()
}
