package com.example.woldeokmoneyverse.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.data.remote.AppCapability
import com.example.woldeokmoneyverse.data.remote.CapabilityFieldSource
import com.example.woldeokmoneyverse.data.remote.apiProblem
import com.example.woldeokmoneyverse.data.remote.koreanApiProblem
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class CapabilityExecutionState(
    val capabilityId: String? = null,
    val loading: Boolean = false,
    val result: String? = null,
    val binaryBytes: ByteArray? = null,
    val error: String? = null
)

class CapabilityViewModel : ViewModel() {
    private val _state = MutableStateFlow(CapabilityExecutionState())
    val state: StateFlow<CapabilityExecutionState> = _state.asStateFlow()

    fun execute(capability: AppCapability, values: Map<String, String>) = viewModelScope.launch {
        if (capability.rawByteUpload || capability.binaryResponse) {
            _state.value = CapabilityExecutionState(
                capability.id,
                error = "이 기능은 전용 미디어 처리 방식으로 실행해야 합니다."
            )
            return@launch
        }
        val validationError = validate(capability, values)
        if (validationError != null) {
            _state.value = CapabilityExecutionState(capability.id, error = validationError)
            return@launch
        }
        _state.value = CapabilityExecutionState(capability.id, loading = true)
        runCatching {
            val path = buildPath(capability, values)
            val body = buildBody(capability, values)
            when (capability.method) {
                "GET" -> ApiClient.api.contractGet(path)
                "POST" -> ApiClient.api.contractPost(path, body)
                "PUT" -> ApiClient.api.contractPut(path, body)
                "DELETE" -> if (body != null) ApiClient.api.contractDelete(path, body) else ApiClient.api.contractDeleteNoBody(path)
                else -> error("지원하지 않는 요청 방식")
            }
        }.onSuccess { response ->
            if (response.isSuccessful) {
                val message = response.body()?.let(::safePreview) ?: "처리가 완료되었습니다."
                _state.value = CapabilityExecutionState(capability.id, result = message)
            } else {
                _state.value = CapabilityExecutionState(
                    capability.id,
                    error = koreanApiProblem(apiProblem(response), capability.title)
                )
            }
        }.onFailure {
            _state.value = CapabilityExecutionState(
                capability.id,
                error = "${capability.title} 처리 중 오류가 발생했습니다."
            )
        }
    }

    fun loadBinary(capability: AppCapability, values: Map<String, String>) = viewModelScope.launch {
        val validationError = validate(capability, values, includeBody = false)
        if (validationError != null) {
            _state.value = CapabilityExecutionState(capability.id, error = validationError)
            return@launch
        }
        if (!capability.binaryResponse || capability.method != "GET") {
            _state.value = CapabilityExecutionState(capability.id, error = "지원하지 않는 미디어 조회 방식입니다.")
            return@launch
        }
        _state.value = CapabilityExecutionState(capability.id, loading = true)
        runCatching { ApiClient.api.contractGetRaw(buildPath(capability, values)) }
            .onSuccess { response ->
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes().orEmpty()
                    if (bytes.isEmpty()) {
                        _state.value = CapabilityExecutionState(capability.id, error = "미디어 데이터가 비어 있습니다.")
                    } else if (bytes.size > MAX_MEDIA_BYTES) {
                        _state.value = CapabilityExecutionState(capability.id, error = "미디어 파일이 허용 크기를 초과했습니다.")
                    } else {
                        _state.value = CapabilityExecutionState(
                            capability.id,
                            result = "미디어를 불러왔습니다 (${bytes.size / 1024} KB)",
                            binaryBytes = bytes
                        )
                    }
                } else {
                    _state.value = CapabilityExecutionState(
                        capability.id,
                        error = koreanApiProblem(apiProblem(response), capability.title)
                    )
                }
            }
            .onFailure {
                _state.value = CapabilityExecutionState(capability.id, error = "${capability.title} 조회 중 오류가 발생했습니다.")
            }
    }

    fun uploadRawBytes(capability: AppCapability, values: Map<String, String>, bytes: ByteArray) = viewModelScope.launch {
        val validationError = validate(capability, values, includeBody = false)
        if (validationError != null) {
            _state.value = CapabilityExecutionState(capability.id, error = validationError)
            return@launch
        }
        if (!capability.rawByteUpload || capability.method != "POST") {
            _state.value = CapabilityExecutionState(capability.id, error = "지원하지 않는 업로드 방식입니다.")
            return@launch
        }
        if (bytes.isEmpty()) {
            _state.value = CapabilityExecutionState(capability.id, error = "업로드할 이미지가 비어 있습니다.")
            return@launch
        }
        if (bytes.size > MAX_UPLOAD_BYTES) {
            _state.value = CapabilityExecutionState(capability.id, error = "이미지는 4MB 이하만 업로드할 수 있습니다.")
            return@launch
        }
        _state.value = CapabilityExecutionState(capability.id, loading = true)
        val body = bytes.toRequestBody("application/octet-stream".toMediaType())
        runCatching {
            val path = buildPath(capability, values)
            if (capability.binaryResponse) {
                val response = ApiClient.api.contractPostRawBinary(path, body)
                if (response.isSuccessful) {
                    val returned = response.body()?.bytes().orEmpty()
                    CapabilityExecutionState(
                        capability.id,
                        result = if (returned.isEmpty()) "이미지 업로드가 완료되었습니다." else "이미지 업로드가 완료되었습니다 (${returned.size / 1024} KB)."
                    )
                } else {
                    CapabilityExecutionState(capability.id, error = koreanApiProblem(apiProblem(response), capability.title))
                }
            } else {
                val response = ApiClient.api.contractPostRaw(path, body)
                if (response.isSuccessful) {
                    CapabilityExecutionState(
                        capability.id,
                        result = response.body()?.let(::safePreview) ?: "이미지 업로드가 완료되었습니다."
                    )
                } else {
                    CapabilityExecutionState(capability.id, error = koreanApiProblem(apiProblem(response), capability.title))
                }
            }
        }.onSuccess { _state.value = it }
            .onFailure {
                _state.value = CapabilityExecutionState(capability.id, error = "${capability.title} 업로드 중 오류가 발생했습니다.")
            }
    }

    private fun validate(
        capability: AppCapability,
        values: Map<String, String>,
        includeBody: Boolean = true
    ): String? {
        val missing = capability.fields.firstOrNull {
            it.required && (includeBody || it.source != CapabilityFieldSource.BODY) && values[it.name].isNullOrBlank()
        }
        return missing?.let { "${it.name} 항목을 입력해 주세요." }
    }

    private fun buildPath(capability: AppCapability, values: Map<String, String>): String {
        var path = capability.internalPath
        capability.fields.filter { it.source == CapabilityFieldSource.PATH }.forEach { field ->
            path = path.replace(":${field.name}", Uri.encode(values[field.name].orEmpty()))
        }
        val query = capability.fields.filter { it.source == CapabilityFieldSource.QUERY }
            .mapNotNull { field -> values[field.name]?.takeIf { it.isNotBlank() }?.let { field.name to it } }
        if (query.isNotEmpty()) {
            path += query.joinToString(prefix = "?", separator = "&") {
                Uri.encode(it.first) + "=" + Uri.encode(it.second)
            }
        }
        return path
    }

    private fun buildBody(capability: AppCapability, values: Map<String, String>): JsonElement? {
        if (!capability.hasBody || capability.rawByteUpload) return null
        val bodyFields = capability.fields.filter { it.source == CapabilityFieldSource.BODY }
        if (bodyFields.size == 1 && bodyFields.first().name == "request") {
            return values["request"]?.takeIf { it.isNotBlank() }?.let { JsonParser.parseString(it) }
        }
        return JsonObject().apply {
            bodyFields.forEach { field ->
                val raw = values[field.name]?.takeIf { it.isNotBlank() } ?: return@forEach
                add(field.name, parseValue(raw, field.kind))
            }
        }
    }

    private fun parseValue(raw: String, kind: String): JsonElement = when (kind) {
        "integer" -> JsonParser.parseString(raw.toLong().toString())
        "number" -> JsonParser.parseString(raw.toBigDecimal().toPlainString())
        "boolean" -> JsonParser.parseString(raw.toBooleanStrict().toString())
        "array", "object", "json" -> JsonParser.parseString(raw)
        else -> com.google.gson.JsonPrimitive(raw)
    }

    private fun safePreview(element: JsonElement): String {
        val safe = sanitize(element)
        val text = GsonBuilder().setPrettyPrinting().create().toJson(safe)
            .replace(Regex("https?://[^\\s\"']+"), "[주소 숨김]")
            .replace(Regex("/app-api/v1/[^\\s\"']*"), "[경로 숨김]")
        return text.take(4000)
    }

    private fun sanitize(element: JsonElement): JsonElement = when {
        element.isJsonObject -> JsonObject().apply {
            element.asJsonObject.entrySet().forEach { (key, value) ->
                val normalized = key.lowercase()
                if (listOf("url", "path", "endpoint", "token", "secret", "host").none { normalized.contains(it) }) {
                    add(key, sanitize(value))
                }
            }
        }
        element.isJsonArray -> JsonArray().apply { element.asJsonArray.forEach { add(sanitize(it)) } }
        else -> element
    }

    private companion object {
        const val MAX_UPLOAD_BYTES = 4 * 1024 * 1024
        const val MAX_MEDIA_BYTES = 8 * 1024 * 1024
    }
}
