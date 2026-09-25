package com.example.woldeokmoneyverse.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class AppCapabilityContract(
    val contractVersion: String = "",
    val apiVersion: String = "",
    val endpoints: List<AppCapabilityEndpoint> = emptyList()
)

data class AppCapabilityParameter(
    val name: String,
    @SerializedName("in") val location: String,
    val required: Boolean = false,
    val schema: JsonObject? = null
)

data class AppCapabilityRequestBody(
    val required: Boolean = false,
    val mediaType: String? = null,
    val schemaName: String? = null,
    val resolvedSchema: JsonObject? = null
)

data class CapabilityInputField(
    val source: String,
    val name: String,
    val label: String,
    val required: Boolean,
    val type: String,
    val options: List<String> = emptyList(),
    val defaultValue: String = ""
)

data class AppCapabilityEndpoint(
    val method: String,
    val path: String,
    val purpose: String? = null,
    val callWhen: String? = null,
    val authorization: String? = null,
    val operationId: String? = null,
    val parameters: List<AppCapabilityParameter> = emptyList(),
    val requestBody: AppCapabilityRequestBody? = null,
    val responseMode: String? = null
) {
    val isAdmin: Boolean get() = path.startsWith("/app-api/v1/admin/")
    val isMutation: Boolean get() = method.uppercase() !in setOf("GET", "HEAD")
    val group: String
        get() = path.removePrefix("/app-api/v1/").substringBefore('/').ifBlank { "other" }

    val title: String
        get() = purpose?.takeIf { it.isNotBlank() }
            ?: operationId?.substringAfterLast('_')?.replace('-', ' ')?.replace('_', ' ')
            ?: "기능"

    val requiresDedicatedUi: Boolean
        get() {
            val mediaType = requestBody?.mediaType?.lowercase()
            if (mediaType != null && mediaType != "application/json") return true
            if (path.matches(Regex("/app-api/v1/auth/[^/]+/(authorize|callback)"))) return true
            if (path.startsWith("/app-api/v1/media/")) return true
            return false
        }

    fun inputFields(): List<CapabilityInputField> {
        val fields = mutableListOf<CapabilityInputField>()
        val pathNames = Regex("""(?::([A-Za-z0-9_]+)|\{([A-Za-z0-9_]+)\})""")
            .findAll(path)
            .map { it.groupValues[1].ifBlank { it.groupValues[2] } }
            .distinct()
        for (name in pathNames) {
            fields += CapabilityInputField(
                source = "path",
                name = name,
                label = friendlyFieldName(name),
                required = true,
                type = "string"
            )
        }

        parameters.filter { it.location == "query" }.forEach { parameter ->
            val schema = parameter.schema
            fields += CapabilityInputField(
                source = "query",
                name = parameter.name,
                label = friendlyFieldName(parameter.name),
                required = parameter.required,
                type = schemaType(schema),
                options = enumValues(schema),
                defaultValue = schema?.get("default")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
            )
        }

        val body = requestBody
        if (body != null) {
            val schema = body.resolvedSchema
            val properties = schema?.getAsJsonObject("properties")
            val requiredNames = schema?.getAsJsonArray("required")
                ?.mapNotNull { runCatching { it.asString }.getOrNull() }
                ?.toSet()
                .orEmpty()
            if (properties != null && properties.size() > 0) {
                properties.entrySet().forEach { (name, element) ->
                    val property = element.takeIf { it.isJsonObject }?.asJsonObject
                    fields += CapabilityInputField(
                        source = "body",
                        name = name,
                        label = friendlyFieldName(name),
                        required = name in requiredNames,
                        type = schemaType(property),
                        options = enumValues(property),
                        defaultValue = property?.get("default")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
                    )
                }
            } else if (body.required) {
                fields += CapabilityInputField(
                    source = "rawBody",
                    name = "__body_json",
                    label = "요청 내용",
                    required = true,
                    type = "json"
                )
            }
        }
        return fields.distinctBy { it.source + ":" + it.name }
    }
}

object AppCapabilityLoader {
    private val gson = Gson()

    fun load(context: Context): Result<AppCapabilityContract> = runCatching {
        context.assets.open("mobile_api_contract.json").bufferedReader().use { reader ->
            gson.fromJson(reader, AppCapabilityContract::class.java)
        }.also { contract ->
            require(contract.endpoints.isNotEmpty()) { "앱 기능 계약이 비어 있습니다." }
        }
    }
}

data class CapabilityExecution(
    val status: Int,
    val preview: String
)

object AppCapabilityExecutor {
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()
    private val sensitiveKey = Regex(
        "(token|secret|password|cookie|csrf|authorization|handoff|url|uri|path|host|origin)",
        RegexOption.IGNORE_CASE
    )

    suspend fun execute(
        endpoint: AppCapabilityEndpoint,
        values: Map<String, String>,
        allowAdmin: Boolean
    ): Result<CapabilityExecution> = runCatching {
        if (endpoint.isAdmin && !allowAdmin) {
            throw SecurityException("관리자 권한이 확인되지 않았습니다.")
        }
        if (endpoint.requiresDedicatedUi) {
            throw IllegalStateException("이 기능은 전용 앱 화면에서 사용합니다.")
        }

        val relativeUrl = buildRelativeUrl(endpoint, values)
        val body = buildBody(endpoint, values)
        val response = when (endpoint.method.uppercase()) {
            "GET" -> ApiClient.api.contractGet(relativeUrl)
            "POST" -> ApiClient.api.contractPost(relativeUrl, body)
            "PUT" -> ApiClient.api.contractPut(relativeUrl, body)
            "PATCH" -> ApiClient.api.contractPatch(relativeUrl, body)
            "DELETE" -> if (body == null) {
                ApiClient.api.contractDeleteNoBody(relativeUrl)
            } else {
                ApiClient.api.contractDelete(relativeUrl, body)
            }
            else -> throw IllegalArgumentException("지원하지 않는 기능 방식입니다.")
        }

        if (!response.isSuccessful) {
            throw IllegalStateException("요청을 완료하지 못했습니다. (HTTP ${response.code()})")
        }
        CapabilityExecution(
            status = response.code(),
            preview = safePreview(response.body())
        )
    }

    private fun buildRelativeUrl(
        endpoint: AppCapabilityEndpoint,
        values: Map<String, String>
    ): String {
        var result = endpoint.path.removePrefix("/")
        val matcher = Regex("""(?::([A-Za-z0-9_]+)|\{([A-Za-z0-9_]+)\})""")
        result = matcher.replace(result) { match ->
            val name = match.groupValues[1].ifBlank { match.groupValues[2] }
            val value = values["path:$name"]?.trim().orEmpty()
            require(value.isNotBlank()) { "${friendlyFieldName(name)} 항목을 입력해 주세요." }
            encode(value)
        }

        val query = endpoint.parameters
            .filter { it.location == "query" }
            .mapNotNull { parameter ->
                val value = values["query:${parameter.name}"]?.trim().orEmpty()
                if (value.isBlank()) {
                    require(!parameter.required) { "${friendlyFieldName(parameter.name)} 항목을 입력해 주세요." }
                    null
                } else {
                    encode(parameter.name) + "=" + encode(value)
                }
            }
        if (query.isNotEmpty()) result += "?" + query.joinToString("&")
        return result
    }

    private fun buildBody(
        endpoint: AppCapabilityEndpoint,
        values: Map<String, String>
    ): JsonElement? {
        val requestBody = endpoint.requestBody ?: return null
        val raw = values["rawBody:__body_json"]?.trim()
        if (!raw.isNullOrBlank()) {
            return JsonParser.parseString(raw)
        }

        val fields = endpoint.inputFields().filter { it.source == "body" }
        if (fields.isEmpty()) return if (requestBody.required) JsonObject() else null

        val body = JsonObject()
        fields.forEach { field ->
            val rawValue = values["body:${field.name}"]?.trim().orEmpty()
            if (rawValue.isBlank()) {
                if (field.required) {
                    throw IllegalArgumentException("${field.label} 항목을 입력해 주세요.")
                }
                return@forEach
            }
            body.add(field.name, jsonValue(rawValue, field.type))
        }
        return body
    }

    private fun jsonValue(value: String, type: String): JsonElement = when (type) {
        "boolean" -> JsonParser.parseString(value.lowercase())
        "number", "integer" -> JsonParser.parseString(value)
        "array", "object", "json" -> JsonParser.parseString(value)
        "null" -> JsonNull.INSTANCE
        else -> Gson().toJsonTree(value)
    }

    fun safePreview(body: JsonElement?): String {
        if (body == null || body.isJsonNull) return "완료되었습니다."
        val sanitized = sanitize(body, 0)
        val rendered = prettyGson.toJson(sanitized)
        return if (rendered.length <= 3500) rendered else rendered.take(3500) + "\n…"
    }

    private fun sanitize(element: JsonElement, depth: Int): JsonElement {
        if (depth > 8) return Gson().toJsonTree("…")
        if (element.isJsonArray) {
            val arr = com.google.gson.JsonArray()
            element.asJsonArray.take(20).forEach { arr.add(sanitize(it, depth + 1)) }
            return arr
        }
        if (element.isJsonObject) {
            val obj = JsonObject()
            element.asJsonObject.entrySet().take(50).forEach { (key, value) ->
                if (sensitiveKey.containsMatchIn(key)) {
                    obj.addProperty(key, "[보호됨]")
                } else {
                    obj.add(key, sanitize(value, depth + 1))
                }
            }
            return obj
        }
        if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            val text = element.asString
            if (
                text.contains("https://", ignoreCase = true) ||
                text.contains("http://", ignoreCase = true) ||
                text.contains("/api/", ignoreCase = true) ||
                text.contains("app-api/", ignoreCase = true)
            ) {
                return Gson().toJsonTree("[보호됨]")
            }
            return Gson().toJsonTree(if (text.length <= 500) text else text.take(500) + "…")
        }
        return element.deepCopy()
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20")
}

fun capabilityGroupTitle(group: String): String = when (group) {
    "account" -> "계정·보안"
    "activity" -> "활동"
    "admin" -> "관리자"
    "auth" -> "인증"
    "bank", "banking" -> "은행·금융"
    "board" -> "게시판"
    "businesses" -> "사업"
    "casino" -> "카지노"
    "chat", "support" -> "채팅·문의"
    "clubs" -> "클럽"
    "collections" -> "컬렉션"
    "content" -> "콘텐츠"
    "crafting" -> "제작"
    "developer" -> "개발자 기능"
    "early-game", "engagement" -> "초기 성장"
    "marketplace" -> "마켓플레이스"
    "newspaper" -> "신문"
    "notifications" -> "알림"
    "photos" -> "사진"
    "privacy" -> "개인정보"
    "profile" -> "프로필"
    "progression" -> "성장"
    "rewards" -> "보상"
    "seasons" -> "시즌"
    "game-clock" -> "서버 시간"
    "shop" -> "상점"
    "spaces" -> "개인 공간"
    "stocks" -> "주식"
    "wallet" -> "지갑"
    "work" -> "직업"
    else -> "기타"
}

fun friendlyFieldName(name: String): String = when (name) {
    "id" -> "대상 ID"
    "userId" -> "사용자 ID"
    "provider" -> "로그인 제공자"
    "stockId" -> "종목 ID"
    "commentId" -> "댓글 ID"
    "code" -> "코드"
    "key" -> "식별값"
    "amount", "principalAmount", "thresholdAmount" -> "금액"
    "quantity" -> "수량"
    "search" -> "검색어"
    "category" -> "분류"
    "limit" -> "조회 개수"
    "offset" -> "시작 위치"
    "status" -> "상태"
    "title" -> "제목"
    "body" -> "내용"
    "displayName" -> "표시 이름"
    "conditionKind" -> "조건"
    "thresholdBps" -> "변동 기준"
    "cooldownSeconds" -> "재알림 간격(초)"
    "idempotencyKey" -> "중복 방지 키"
    else -> name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
}

private fun schemaType(schema: JsonObject?): String {
    val direct = schema?.get("type")?.takeUnless { it.isJsonNull }?.asString
    if (!direct.isNullOrBlank()) return direct
    val oneOf = schema?.getAsJsonArray("oneOf")
    return oneOf?.mapNotNull { item ->
        item.takeIf { it.isJsonObject }?.asJsonObject?.get("type")?.takeUnless { it.isJsonNull }?.asString
    }?.firstOrNull { it != "null" } ?: "string"
}

private fun enumValues(schema: JsonObject?): List<String> =
    schema?.getAsJsonArray("enum")
        ?.mapNotNull { item -> runCatching { item.asString }.getOrNull() }
        .orEmpty()
