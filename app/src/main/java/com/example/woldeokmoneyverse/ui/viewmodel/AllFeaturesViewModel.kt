package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.data.remote.ApiFeatureEndpoint
import com.example.woldeokmoneyverse.data.remote.ApiFeatureRegistry
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class AllFeaturesState(
    val group: String = "banking",
    val endpoint: ApiFeatureEndpoint? = null,
    val requestPath: String = "",
    val requestBody: String = "",
    val responseText: String = "",
    val statusCode: Int? = null,
    val busy: Boolean = false,
    val error: String? = null
)

class AllFeaturesViewModel : ViewModel() {
    private val _state = MutableStateFlow(AllFeaturesState())
    val state: StateFlow<AllFeaturesState> = _state.asStateFlow()
    val groups = ApiFeatureRegistry.groups

    init { selectGroup(_state.value.group) }

    fun selectGroup(group: String) {
        val first = ApiFeatureRegistry.endpoints.firstOrNull { it.group == group }
        _state.value = _state.value.copy(group = group, endpoint = null, requestPath = "", requestBody = "", responseText = "", statusCode = null, error = null)
        if (first != null) selectEndpoint(first)
    }

    fun endpointsFor(group: String): List<ApiFeatureEndpoint> = ApiFeatureRegistry.endpoints.filter { it.group == group }

    fun selectEndpoint(endpoint: ApiFeatureEndpoint) {
        _state.value = _state.value.copy(endpoint = endpoint, requestPath = endpoint.path, requestBody = endpoint.bodyTemplate.orEmpty(), responseText = "", statusCode = null, error = null)
    }

    fun setPath(value: String) { _state.value = _state.value.copy(requestPath = value) }
    fun setBody(value: String) { _state.value = _state.value.copy(requestBody = value) }

    fun execute() = viewModelScope.launch {
        val endpoint = _state.value.endpoint ?: return@launch
        val path = _state.value.requestPath.trim()
        if (path.contains(":")) {
            _state.value = _state.value.copy(error = "경로의 :파라미터를 실제 값으로 바꿔 주세요. (예: :id -> 123)")
            return@launch
        }
        if (!path.startsWith("/app-api/v1/") && !path.startsWith("/api/v1/") && !path.startsWith("/")) {
            _state.value = _state.value.copy(error = "경로는 /app-api/v1/ 또는 /api/v1/ 로 시작해야 합니다.")
            return@launch
        }
        _state.value = _state.value.copy(busy = true, error = null, responseText = "")
        val relative = path.removePrefix("/")
        val jsonBody = _state.value.requestBody.ifBlank { "{}" }.replace("<uuid>", java.util.UUID.randomUUID().toString())
        val requestBody = jsonBody.toRequestBody(JSON)
        runCatching {
            when (endpoint.method) {
                "GET" -> ApiClient.api.universalGet(relative)
                "POST" -> ApiClient.api.universalPost(relative, requestBody)
                "PUT" -> ApiClient.api.universalPut(relative, requestBody)
                "PATCH" -> ApiClient.api.universalPatch(relative, requestBody)
                "DELETE" -> if (endpoint.bodyTemplate == null) ApiClient.api.universalDeleteNoBody(relative) else ApiClient.api.universalDelete(relative, requestBody)
                else -> error("지원하지 않는 HTTP method: ${endpoint.method}")
            }
        }.onSuccess { response ->
            val raw = if (response.isSuccessful) response.body()?.string().orEmpty() else response.errorBody()?.string().orEmpty()
            _state.value = _state.value.copy(busy = false, statusCode = response.code(), responseText = pretty(raw), error = if (response.isSuccessful) null else "HTTP ${response.code()}")
        }.onFailure {
            _state.value = _state.value.copy(busy = false, error = it.message ?: "API 호출 실패")
        }
    }

    private fun pretty(raw: String): String = runCatching {
        GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(raw))
    }.getOrDefault(raw)

    companion object { private val JSON = "application/json; charset=utf-8".toMediaType() }
}
