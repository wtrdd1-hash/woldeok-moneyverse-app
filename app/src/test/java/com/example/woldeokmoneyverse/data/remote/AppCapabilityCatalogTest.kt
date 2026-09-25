package com.example.woldeokmoneyverse.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppCapabilityCatalogTest {
    @Test
    fun catalog_matches_current_app_contract_shape() {
        val all = AppCapabilityCatalog.all
        assertEquals(179, all.size)
        assertEquals(179, all.map { it.method + " " + it.internalPath }.toSet().size)
        assertEquals(11, AppCapabilityCatalog.admin.size)
        assertEquals(168, AppCapabilityCatalog.member.size)
        assertEquals(3, all.count { it.rawByteUpload })
        assertEquals(5, all.count { it.binaryResponse })
        assertEquals(4, all.count { it.responseMode == "none" })

        all.forEach { capability ->
            assertTrue(capability.internalPath.startsWith("app-api/v1/"))
            assertTrue(capability.title.isNotBlank())
            val placeholders = Regex(":([A-Za-z][A-Za-z0-9_]*)")
                .findAll(capability.internalPath)
                .map { it.groupValues[1] }
                .toSet()
            val declaredPathFields = capability.fields
                .filter { it.source == CapabilityFieldSource.PATH }
                .map { it.name }
                .toSet()
            assertEquals(placeholders, declaredPathFields)
            assertEquals(capability.adminOnly, capability.internalPath.startsWith("app-api/v1/admin/"))
            if (capability.rawByteUpload) {
                assertEquals("application/octet-stream", capability.requestMediaType)
                assertFalse(capability.fields.any { it.source == CapabilityFieldSource.BODY })
            }
            if (capability.binaryResponse) {
                assertEquals("binary", capability.responseMode)
            }
        }
    }
}
