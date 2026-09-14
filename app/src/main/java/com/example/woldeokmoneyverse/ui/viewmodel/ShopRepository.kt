package com.example.woldeokmoneyverse.ui.viewmodel

import com.example.woldeokmoneyverse.data.model.AuthResponse
import com.example.woldeokmoneyverse.data.model.ShopItemDto
import com.example.woldeokmoneyverse.data.model.ShopPurchaseDto
import com.example.woldeokmoneyverse.data.model.ShopPurchaseRequest
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class ShopRepository {
    suspend fun getShopItems(query: String = ""): Result<List<ShopItemDto>> = runCatching {
        val normalized = query.trim()
        val suffix = if (normalized.isBlank()) "" else "?q=${URLEncoder.encode(normalized, StandardCharsets.UTF_8.toString())}"
        val response = ApiClient.api.contractGet("app-api/v1/shop/catalog$suffix")
        if (!response.isSuccessful || response.body() == null) {
            throw Exception(response.code().toString())
        }
        val root = response.body()!!.asJsonObject
        root.getAsJsonArray("catalogItems")?.mapNotNull { element ->
            val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val id = item.string("catalogId", "catalog_id") ?: return@mapNotNull null
            val ownedQuantity = item.int("userOwnedQuantity", "user_owned_quantity") ?: 0
            val purchaseLimit = item.string("purchaseLimit", "purchase_limit") ?: "account_one"
            val blocksRepurchase = purchaseLimit.lowercase() !in setOf("unlimited", "repeatable")
            val preview = item.getAsJsonObject("previewData") ?: item.getAsJsonObject("preview_data")
            ShopItemDto(
                id = id,
                name = item.string("name") ?: "이름 없는 상품",
                category = item.string("category") ?: "general",
                price = item.string("price") ?: "0",
                description = item.string("description") ?: "",
                isOwned = ownedQuantity > 0 && blocksRepurchase,
                iconUrl = preview?.string("iconUrl", "icon_url", "icon")
            )
        }.orEmpty()
    }

    suspend fun getPurchasedItems(): Result<List<ShopPurchaseDto>> = runCatching {
        val response = ApiClient.api.contractGet("app-api/v1/shop/holdings")
        if (!response.isSuccessful || response.body() == null) {
            throw Exception(response.code().toString())
        }
        val root = response.body()!!.asJsonObject
        root.getAsJsonArray("holdings")?.mapNotNull { element ->
            val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val id = item.string("catalogId", "catalog_id") ?: return@mapNotNull null
            ShopPurchaseDto(
                purchaseId = "holding-$id",
                itemId = id,
                itemName = item.string("name") ?: "보유 상품",
                transactionId = "",
                amount = item.string("quantity") ?: "1",
                purchasedAt = item.string("acquiredAt", "acquired_at") ?: ""
            )
        }.orEmpty()
    }

    suspend fun purchaseItem(itemId: String, req: ShopPurchaseRequest): Result<AuthResponse> = runCatching {
        val body = JsonObject().apply {
            addProperty("quantity", req.quantity)
            addProperty("idempotencyKey", req.idempotencyKey)
        }
        val response = ApiClient.api.contractPost("app-api/v1/shop/catalog/$itemId/purchases", body)
        if (!response.isSuccessful) {
            throw Exception(response.code().toString())
        }
        AuthResponse(success = true, message = "상품을 구매했습니다.")
    }

    private fun JsonObject.string(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asString }.getOrNull() }
    }

    private fun JsonObject.int(vararg names: String): Int? = names.firstNotNullOfOrNull { name ->
        get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asInt }.getOrNull() }
    }
}
