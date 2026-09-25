package com.example.woldeokmoneyverse

import com.example.woldeokmoneyverse.data.remote.AppCapabilityContract
import com.example.woldeokmoneyverse.data.remote.AppCapabilityExecutor
import com.example.woldeokmoneyverse.data.remote.FullAppApiCatalog
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppCapabilityContractTest {
    private fun asset(name: String): File {
        val candidates = listOf(
            File("src/main/assets/$name"),
            File("app/src/main/assets/$name")
        )
        return candidates.firstOrNull { it.isFile } ?: error("$name asset is missing")
    }

    private fun contract(): AppCapabilityContract =
        Gson().fromJson(asset("mobile_api_contract.json").readText(), AppCapabilityContract::class.java)

    private fun fullCatalog(): FullAppApiCatalog =
        Gson().fromJson(asset("full_app_api_catalog.json").readText(), FullAppApiCatalog::class.java)

    @Test
    fun fullCatalogCoversEveryAppSafeEndpointIncludingAllAdminRoutes() {
        val catalog = fullCatalog()
        assertEquals("v2026.09.25.443", catalog.catalogVersion)
        assertEquals(337, catalog.endpoints.size)
        assertEquals(118, catalog.endpoints.count { it.isAdmin })
        assertTrue(catalog.endpoints.all { it.method.uppercase() in setOf("GET", "POST", "PUT", "PATCH", "DELETE") })
        assertFalse(catalog.endpoints.any { it.path.contains("/health") })
        assertFalse(catalog.endpoints.any { it.path.contains("/integrations/") })
        assertTrue(catalog.endpoints.any { it.path == "/app-api/v1/admin/treasury/overview" })
        assertTrue(catalog.endpoints.any { it.path == "/app-api/v1/admin/stocks" })
        assertTrue(catalog.endpoints.any { it.path == "/app-api/v1/admin/security/ip-blocks" })
    }

    @Test
    fun detailedContractStillSuppliesTypedSchemasForEstablishedMobileSurface() {
        val contract = contract()
        assertEquals("v2026.09.22.359", contract.contractVersion)
        assertEquals(179, contract.endpoints.size)
        assertEquals(11, contract.endpoints.count { it.isAdmin })
    }

    @Test
    fun capabilityInputsAreGeneratedForMissingAppFeatures() {
        val contract = contract()
        val alert = contract.endpoints.first {
            it.method == "POST" && it.path == "/app-api/v1/stocks/alerts"
        }
        val fields = alert.inputFields()
        assertTrue(fields.any { it.name == "stockId" && it.required })
        assertTrue(fields.any { it.name == "conditionKind" && it.required })
        assertTrue(fields.any { it.name == "cooldownSeconds" })
    }

    @Test
    fun safePreviewNeverLeaksNetworkAddressesOrSecrets() {
        val input = JsonParser.parseString(
            """{
              "csrfToken":"secret",
              "authorizationUrl":"https://example.invalid/api/login",
              "nested":{"path":"/app-api/v1/admin/users","displayName":"member"}
            }"""
        )
        val preview = AppCapabilityExecutor.safePreview(input)
        assertFalse(preview.contains("https://"))
        assertFalse(preview.contains("/api/"))
        assertFalse(preview.contains("app-api/"))
        assertFalse(preview.contains("secret"))
        assertTrue(preview.contains("member"))
        assertTrue(preview.contains("[보호됨]"))
    }
}
