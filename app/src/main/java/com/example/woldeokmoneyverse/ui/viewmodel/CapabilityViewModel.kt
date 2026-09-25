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

data class CapabilityExecutionState(
    val capabilityId: String? = null,
    val loading: Boolean = false,
    val result: String? = null,
    val error: String? = null
)

class CapabilityViewModel : ViewModel() {
    private val _state = MutableStateFlow(CapabilityExecutionState())
    val state: StateFlow<CapabilityExecutionState> = _state.asStateFlow()

    fun execute(capability: AppCapability, values: Map<String, String>) = viewModelScope.launch {
        val missing = capability.fields.firstOrNull { it.required && values[it.name].isNullOrBlank() }
        if (missing != null) {
            _state.value = CapabilityExecutionState(capability.id, error = "${missing.name} 항목을 입력해 주세요.")
            return@launch
        }
        _state.value = CapabilityExecutionState(capability.id, loading = true)
        runCatching {
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
        }.onFailure { error ->
            _state.value = CapabilityExecutionState(
                capability.id,
                error = error.message?.takeIf { !containsRoute(it) } ?: "${capability.title} 처리 중 오류가 발생했습니다."
            )
        }
    }

    private fun buildBody(capability: AppCapability, values: Map<String, String>): JsonElement? {
        if (!capability.hasBody) return null
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

    private fun containsRoute(value: String): Boolean =
        value.contains("app-api", ignoreCase = true) ||
            value.contains("http://", ignoreCase = true) ||
            value.contains("https://", ignoreCase = true)
}
