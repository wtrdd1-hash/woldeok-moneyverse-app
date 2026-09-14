package com.example.woldeokmoneyverse.data.remote

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.math.BigDecimal
import java.util.UUID

/**
 * Compatibility boundary for the canonical /app-api/v1 contract.
 *
 * The Android UI was initially implemented against several legacy response
 * envelopes/field names. Production now exposes one canonical BFF contract.
 * This interceptor keeps the wire format canonical while adapting only at the
 * app boundary, so requests accepted by the server and objects consumed by the
 * existing UI stay consistent during the typed-model migration.
 */
class ApiContractCompatibilityInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val normalizedRequest = normalizeRequest(chain.request())
        val response = chain.proceed(normalizedRequest)
        return normalizeResponse(normalizedRequest, response)
    }

    private fun normalizeRequest(request: Request): Request {
        val path = request.url.encodedPath
        if (!path.startsWith("/app-api/v1/")) return request

        val method = request.method
        var url = request.url
        var json = readJsonBody(request)

        if (method == "POST" && path == "/app-api/v1/privacy/requests" && json == null) {
            val legacyType = url.queryParameter("type")
            val requestType = when (legacyType?.uppercase()) {
                "EXPORT" -> "access"
                "DELETE" -> "deletion"
                else -> legacyType?.lowercase() ?: "access"
            }
            json = JsonObject().apply {
                addProperty("requestType", requestType)
                addProperty("idempotencyKey", UUID.randomUUID().toString())
            }
            url = url.newBuilder().removeAllQueryParameters("type").build()
        }

        if (json != null) {
            when {
                method == "POST" && path == "/app-api/v1/bank/loans" -> {
                    move(json, "amount", "principalAmount")
                }
                method == "POST" && (path == "/app-api/v1/casino/coin/plays" || path == "/app-api/v1/casino/dice/plays") -> {
                    ensureIdempotency(json)
                }
                method == "POST" && path == "/app-api/v1/board/posts" -> {
                    move(json, "content", "body")
                }
                method == "POST" && path.matches(Regex("/app-api/v1/board/posts/[^/]+/comments")) -> {
                    move(json, "content", "body")
                }
                method == "POST" && path.matches(Regex("/app-api/v1/businesses/catalog/[^/]+/purchases")) -> {
                    json.remove("catalogCode")
                    ensureIdempotency(json)
                }
                method == "POST" && path.matches(Regex("/app-api/v1/shop/items/[^/]+/purchases")) -> {
                    json.remove("quantity")
                    ensureIdempotency(json)
                }
                method == "POST" && path == "/app-api/v1/wallet/transfers" -> {
                    json.remove("memo")
                }
            }
        }

        if (method == "POST" && path.matches(Regex("/app-api/v1/businesses/[^/]+/settlements")) && json == null) {
            json = JsonObject().apply { addProperty("idempotencyKey", UUID.randomUUID().toString()) }
        }

        if (json == null && url == request.url) return request
        val builder = request.newBuilder().url(url)
        if (json != null && method in setOf("POST", "PUT", "PATCH", "DELETE")) {
            builder.method(method, json.toString().toRequestBody(JSON_MEDIA_TYPE))
        }
        return builder.build()
    }

    private fun normalizeResponse(request: Request, response: Response): Response {
        if (!response.isSuccessful) return response
        val body = response.body ?: return response
        val mediaType = body.contentType()
        if (mediaType?.subtype?.contains("json", ignoreCase = true) != true) return response
        val raw = body.string()
        if (raw.isBlank()) return response.newBuilder().body(raw.toResponseBody(body.contentType())).build()

        val root = runCatching { JsonParser.parseString(raw) }.getOrNull()
            ?: return response.newBuilder().body(raw.toResponseBody(body.contentType())).build()

        captureCsrf(root)
        val path = request.url.encodedPath
        val method = request.method
        val normalized = when {
            method == "GET" && path == "/app-api/v1/stocks" -> normalizeStocks(root)
            method == "GET" && path == "/app-api/v1/stocks/portfolio" -> normalizePortfolio(root)
            method == "POST" && path.matches(Regex("/app-api/v1/stocks/[^/]+/orders")) -> normalizeStockOrder(root)
            method == "GET" && path == "/app-api/v1/bank/loans" -> normalizeLoans(root)
            method == "GET" && path == "/app-api/v1/businesses/my-v2" -> normalizeBusinesses(root)
            method == "GET" && path == "/app-api/v1/businesses/equity" -> normalizeBusinessEquity(root)
            method == "POST" && path.matches(Regex("/app-api/v1/businesses/[^/]+/settlements")) -> normalizeBusinessSettlement(root)
            method == "POST" && (path == "/app-api/v1/casino/coin/plays" || path == "/app-api/v1/casino/dice/plays") -> normalizeCasinoPlay(root)
            method == "GET" && path == "/app-api/v1/casino/self-limit" -> normalizeCasinoLimit(root)
            method == "GET" && path == "/app-api/v1/seasons/events" -> unwrapAndMap(root, "events", ::normalizeSeason)
            method == "GET" && path.matches(Regex("/app-api/v1/seasons/events/[^/]+/leaderboard")) -> unwrapAndMap(root, "entries", ::normalizeLeaderboard)
            method == "GET" && path == "/app-api/v1/shop/items" -> unwrapAndMap(root, "items", ::normalizeShopItem)
            method == "GET" && path == "/app-api/v1/board/posts" -> normalizeBoardList(root)
            method == "POST" && path == "/app-api/v1/board/posts" -> unwrapObject(root, "post", ::normalizeBoardPost)
            method == "POST" && path.matches(Regex("/app-api/v1/board/posts/[^/]+/comments")) -> unwrapObject(root, "comment", ::normalizeComment)
            method == "POST" && path == "/app-api/v1/privacy/requests" -> normalizePrivacy(root)
            method == "GET" && path == "/app-api/v1/content/photos" -> normalizeGallery(root)
            method == "GET" && path == "/app-api/v1/content/announcements" -> unwrapAndMap(root, "announcements", ::normalizeAnnouncement)
            else -> root
        }

        return response.newBuilder()
            .body(normalized.toString().toResponseBody(body.contentType() ?: JSON_MEDIA_TYPE))
            .build()
    }

    private fun normalizeStocks(root: JsonElement): JsonElement {
        if (root.isJsonArray) return root
        val array = root.obj()?.getAsJsonArray("stocks") ?: return root
        array.forEach { e ->
            val o = e.obj() ?: return@forEach
            alias(o, "currentPrice", "current_price")
            alias(o, "dayOpenPrice", "day_open_price")
            val current = number(o, "currentPrice")
            val open = number(o, "dayOpenPrice")
            if (!o.has("priceChangePercent")) {
                val pct = if (current != null && open != null && open.compareTo(BigDecimal.ZERO) != 0) {
                    current.subtract(open).divide(open, 8, java.math.RoundingMode.HALF_UP).multiply(BigDecimal(100))
                } else BigDecimal.ZERO
                o.addProperty("priceChangePercent", pct.toDouble())
            }
            if (!o.has("isMarketOpen")) o.addProperty("isMarketOpen", true)
            if (!o.has("historyPrices")) o.add("historyPrices", JsonArray())
        }
        return array
    }

    private fun normalizePortfolio(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        val holdings = o.getAsJsonArray("holdings") ?: return root
        var total = BigDecimal.ZERO
        holdings.forEach { e ->
            val h = e.obj() ?: return@forEach
            alias(h, "stockId", "stock_id")
            alias(h, "averageBuyPrice", "averageCost", "average_cost")
            alias(h, "currentPrice", "current_price")
            alias(h, "totalValue", "marketValue", "market_value")
            val q = h.get("quantity")?.asString?.toIntOrNull() ?: 0
            h.addProperty("quantity", q)
            val avg = number(h, "averageBuyPrice")
            val current = number(h, "currentPrice")
            val pct = if (avg != null && current != null && avg.compareTo(BigDecimal.ZERO) != 0) {
                current.subtract(avg).divide(avg, 8, java.math.RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble()
            } else 0.0
            h.addProperty("profitLossPercent", pct)
            total = total.add(number(h, "totalValue") ?: BigDecimal.ZERO)
        }
        if (!o.has("totalStockValue")) o.addProperty("totalStockValue", total.stripTrailingZeros().toPlainString())
        return o
    }

    private fun normalizeStockOrder(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        if (o.has("success")) return o
        alias(o, "executedPrice", "unitPrice", "unit_price")
        alias(o, "totalCost", "grossAmount", "gross_amount")
        o.addProperty("success", true)
        o.addProperty("message", "주문이 체결되었습니다.")
        return o
    }

    private fun normalizeLoans(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        o.getAsJsonArray("loans")?.forEach { e ->
            val loan = e.obj() ?: return@forEach
            alias(loan, "id", "loanId")
            alias(loan, "principal", "principalAmount")
            alias(loan, "remainingBalance", "outstandingAmount")
            if (!loan.has("interestRate")) loan.addProperty("interestRate", 0.0)
            if (!loan.has("dueDate")) loan.addProperty("dueDate", "—")
        }
        return o
    }

    private fun normalizeBusinesses(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        o.getAsJsonArray("businesses")?.forEach { e ->
            val b = e.obj() ?: return@forEach
            alias(b, "id", "ownershipId")
            alias(b, "typeId", "businessTypeId")
            if (!b.has("level")) b.addProperty("level", 1)
            val settled = b.get("isSettledToday")?.asBoolean ?: false
            val active = b.get("status")?.asString?.lowercase() !in setOf("inactive", "disabled")
            b.addProperty("isSettlementReady", !settled && active)
            if (!b.has("nextSettlementAt")) b.addProperty("nextSettlementAt", "")
            if (!b.has("pendingRevenue")) {
                val revenue = number(b, "dailyRevenue") ?: BigDecimal.ZERO
                val cost = number(b, "dailyOperatingCost") ?: BigDecimal.ZERO
                b.addProperty("pendingRevenue", revenue.subtract(cost).max(BigDecimal.ZERO).stripTrailingZeros().toPlainString())
            }
            b.addProperty("licenseActive", active)
        }
        return o
    }

    private fun normalizeBusinessEquity(root: JsonElement): JsonElement {
        val equity = root.obj()?.getAsJsonObject("equity") ?: return root
        alias(equity, "availableEquity", "equityAmount")
        alias(equity, "totalBusinessValuation", "holdingsAmount")
        if (!equity.has("maxLoanCapacity")) equity.addProperty("maxLoanCapacity", "0")
        return root
    }

    private fun normalizeBusinessSettlement(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        if (!o.has("success")) o.addProperty("success", true)
        alias(o, "collectedAmount", "netAmount")
        if (!o.has("nextSettlementAt")) o.addProperty("nextSettlementAt", "")
        if (!o.has("message")) o.addProperty("message", "정산이 완료되었습니다.")
        return o
    }

    private fun normalizeCasinoPlay(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        if (o.has("success")) return o
        alias(o, "resultOutcome", "outcome", "outcomeFace", "outcome_face")
        alias(o, "netProfit", "netAmount", "net_amount")
        alias(o, "payoutAmount", "netAmount", "net_amount")
        val net = number(o, "netProfit") ?: BigDecimal.ZERO
        o.addProperty("success", true)
        o.addProperty("isWin", net > BigDecimal.ZERO)
        o.addProperty("message", if (net > BigDecimal.ZERO) "게임 결과: 승리" else "게임 결과: 패배")
        return o
    }

    private fun normalizeCasinoLimit(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        alias(o, "dailyBetLimit", "daily_bet_limit")
        alias(o, "dailyLossLimit", "daily_loss_limit")
        alias(o, "lockedUntil", "locked_until")
        o.get("dailyBetLimit")?.asString?.toLongOrNull()?.let { o.addProperty("dailyBetLimit", it) }
        o.get("dailyLossLimit")?.asString?.toLongOrNull()?.let { o.addProperty("dailyLossLimit", it) }
        return o
    }

    private fun normalizeSeason(o: JsonObject) {
        if (o.has("id")) return
        alias(o, "id", "eventId", "event_id")
        alias(o, "name", "seasonName", "season_name", "title")
        alias(o, "endsAt", "ends_at")
        if (!o.has("currentProgress")) o.addProperty("currentProgress", 0)
        if (!o.has("totalMilestone")) o.addProperty("totalMilestone", 1)
    }

    private fun normalizeLeaderboard(o: JsonObject) {
        alias(o, "displayName", "display_name")
        alias(o, "score", "points")
        if (!o.has("userId")) o.addProperty("userId", "")
        if (!o.has("title")) o.addProperty("title", "참여 ${o.get("entries")?.asString ?: "0"}회")
    }

    private fun normalizeShopItem(o: JsonObject) {
        alias(o, "id", "itemId")
        if (!o.has("category")) o.addProperty("category", "")
        if (!o.has("isOwned")) o.addProperty("isOwned", false)
        if (!o.has("iconUrl")) o.add("iconUrl", com.google.gson.JsonNull.INSTANCE)
    }

    private fun normalizeBoardList(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        o.getAsJsonArray("posts")?.forEach { e -> e.obj()?.let(::normalizeBoardPost) }
        return root
    }

    private fun normalizeBoardPost(o: JsonObject) {
        alias(o, "content", "body")
        if (!o.has("authorId")) o.addProperty("authorId", "")
        if (!o.has("comments")) o.add("comments", JsonArray())
    }

    private fun normalizeComment(o: JsonObject) {
        alias(o, "id", "commentId")
        alias(o, "content", "body")
    }

    private fun normalizePrivacy(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        alias(o, "type", "requestType")
        alias(o, "requestedAt", "createdAt")
        return o
    }

    private fun normalizeGallery(root: JsonElement): JsonElement {
        val o = root.obj() ?: return root
        o.getAsJsonArray("photos")?.forEach { e ->
            val photo = e.obj() ?: return@forEach
            if (!photo.has("category")) photo.addProperty("category", "갤러리")
            if (!photo.has("likes")) photo.addProperty("likes", 0)
        }
        return root
    }

    private fun normalizeAnnouncement(o: JsonObject) {
        alias(o, "id", "announcementId")
        alias(o, "content", "body")
        alias(o, "isImportant", "isPinned")
        alias(o, "createdAt", "publishedAt")
    }

    private fun unwrapAndMap(root: JsonElement, key: String, mapper: (JsonObject) -> Unit): JsonElement {
        if (root.isJsonArray) return root
        val array = root.obj()?.getAsJsonArray(key) ?: return root
        array.forEach { it.obj()?.let(mapper) }
        return array
    }

    private fun unwrapObject(root: JsonElement, key: String, mapper: (JsonObject) -> Unit): JsonElement {
        val current = root.obj() ?: return root
        val nested = current.getAsJsonObject(key) ?: current
        mapper(nested)
        return nested
    }

    private fun readJsonBody(request: Request): JsonObject? {
        val body = request.body ?: return null
        val buffer = Buffer()
        return runCatching {
            body.writeTo(buffer)
            val raw = buffer.readUtf8()
            if (raw.isBlank()) null else JsonParser.parseString(raw).obj()
        }.getOrNull()
    }

    private fun move(o: JsonObject, from: String, to: String) {
        if (!o.has(to) && o.has(from)) o.add(to, o.get(from))
        if (from != to) o.remove(from)
    }

    private fun ensureIdempotency(o: JsonObject) {
        if (!o.has("idempotencyKey")) o.addProperty("idempotencyKey", UUID.randomUUID().toString())
    }

    private fun alias(o: JsonObject, target: String, vararg sources: String) {
        if (o.has(target)) return
        sources.firstOrNull { o.has(it) }?.let { o.add(target, o.get(it)) }
    }

    private fun number(o: JsonObject, key: String): BigDecimal? =
        o.get(key)?.takeUnless { it.isJsonNull }?.asString?.toBigDecimalOrNull()

    private fun JsonElement.obj(): JsonObject? = takeIf { it.isJsonObject }?.asJsonObject

    private fun captureCsrf(root: JsonElement) {
        val token = root.obj()?.get("csrfToken")?.takeUnless { it.isJsonNull }?.asString
        if (!token.isNullOrBlank()) ApiClient.csrfToken = token
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
