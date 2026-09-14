package com.example.woldeokmoneyverse.ui.viewmodel

import com.example.woldeokmoneyverse.data.model.AuthResponse
import com.example.woldeokmoneyverse.data.model.ShopItemDto
import com.example.woldeokmoneyverse.data.model.ShopPurchaseDto
import com.example.woldeokmoneyverse.data.model.ShopPurchaseRequest
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonObject

/**
 * Store 2.0 mobile adapter.
 *
 * The web shop renders the authoritative `/shop/catalog` contract. Older
 * Android code used the legacy `/shop/items` contract and therefore missed
 * the Store 2.0 catalogue. Keeping this adapter in the ViewModel package makes
 * EconomyViewModel resolve this implementation ahead of the wildcard-imported
 * legacy repository without duplicating any price/economy authority on-device.
 */
class ShopRepository {
    suspend fun getShopItems(): Result<List<ShopItemDto>> = runCatching {
        val response = ApiClient.api.contractGet("app-api/v1/shop/catalog")
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("상점 카탈로그 조회 실패 (${response.code()})")
        }
        val root = response.body()!!.asJsonObject
        root.getAsJsonArray("catalogItems")?.mapNotNull { element ->
            val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val id = item.string("catalogId", "catalog_id") ?: return@mapNotNull null
            val owned = (item.int("userOwnedQuantity", "user_owned_quantity") ?: 0) > 0
            val preview = item.getAsJsonObject("previewData") ?: item.getAsJsonObject("preview_data")
            ShopItemDto(
                id = id,
                name = item.string("name") ?: "이름 없는 상품",
                category = item.string("category") ?: "general",
                price = item.string("price") ?: "0",
                description = item.string("description") ?: "",
                isOwned = owned,
                iconUrl = preview?.string("iconUrl", "icon_url", "icon")
            )
        }.orEmpty()
    }

    suspend fun getPurchasedItems(): Result<List<ShopPurchaseDto>> = runCatching {
        // Store 2.0 ownership is represented by holdings. Convert it to the
        // existing UI purchase shape so the current app can render it safely.
        val response = ApiClient.api.contractGet("app-api/v1/shop/holdings")
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("보유 상품 조회 실패 (${response.code()})")
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
            throw Exception("상품 구매 실패 (${response.code()}): ${response.errorBody()?.string().orEmpty()}")
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
