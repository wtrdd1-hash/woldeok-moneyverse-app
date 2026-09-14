package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class BoardComposerViewModel : ViewModel() {
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun createPost(
        title: String,
        content: String,
        imageBytes: ByteArray?,
        imageMimeType: String?,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        if (title.isBlank() || content.isBlank()) {
            _message.value = "제목과 내용을 입력해 주세요."
            return@launch
        }
        _busy.value = true
        try {
            var storageKey: String? = null
            if (imageBytes != null) {
                val mediaType = imageMimeType?.toMediaTypeOrNull() ?: "application/octet-stream".toMediaTypeOrNull()
                val upload = ApiClient.api.contractPostRaw(
                    "app-api/v1/board/images/uploads",
                    imageBytes.toRequestBody(mediaType)
                )
                if (!upload.isSuccessful || upload.body() == null) {
                    _message.value = "게시판 이미지 업로드 실패 (HTTP ${upload.code()})"
                    return@launch
                }
                val obj = upload.body()!!.asJsonObject
                storageKey = obj.get("storageKey")?.asString
                    ?: obj.get("storage_key")?.asString
                if (storageKey.isNullOrBlank()) {
                    _message.value = "이미지 업로드 응답에 저장 키가 없습니다."
                    return@launch
                }
            }

            val body = JsonObject().apply {
                addProperty("title", title.trim())
                addProperty("body", content.trim())
                addProperty("idempotencyKey", UUID.randomUUID().toString())
                storageKey?.let {
                    addProperty("imageStorageKey", it)
                    addProperty("imageAltText", title.trim().take(300))
                }
            }
            val created = ApiClient.api.contractPost("app-api/v1/board/posts", body)
            if (!created.isSuccessful) {
                _message.value = "게시글 등록 실패 (HTTP ${created.code()}): ${created.errorBody()?.string().orEmpty()}"
                return@launch
            }
            _message.value = if (storageKey == null) "게시글이 등록되었습니다." else "이미지와 게시글이 등록되었습니다."
            onSuccess()
        } catch (error: Exception) {
            _message.value = "게시글 등록 실패: ${error.message}"
        } finally {
            _busy.value = false
        }
    }

    fun clearMessage() { _message.value = null }
}
