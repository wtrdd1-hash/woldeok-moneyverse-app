package com.example.woldeokmoneyverse.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.PATCH

class ApiFeatureRegistryTest {
    @Test
    fun registryPinsCompleteMobileContract() {
        assertEquals("v2026.09.22.359", ApiFeatureRegistry.CONTRACT_VERSION)
        assertEquals(190, ApiFeatureRegistry.endpoints.size)
        assertEquals(190, ApiFeatureRegistry.endpoints.map { it.method to it.path }.distinct().size)
        assertTrue(ApiFeatureRegistry.endpoints.all { it.path.startsWith("/app-api/v1/") })
    }

    @Test
    fun newlyAddedUserFeatureGroupsAreReachable() {
        val groups = ApiFeatureRegistry.groups.toSet()
        assertEquals(33, groups.size)
        listOf("banking", "chat", "clubs", "crafting", "developer", "engagement", "marketplace", "newspaper", "notifications", "spaces").forEach {
            assertTrue(it in groups)
        }
    }

    @Test
    fun universalTransportExposesPatchForMasterContractOperations() {
        val patch = MoneyverseApi::class.java.methods.first { it.name == "universalPatch" }
        assertTrue(patch.isAnnotationPresent(PATCH::class.java))
    }
}
