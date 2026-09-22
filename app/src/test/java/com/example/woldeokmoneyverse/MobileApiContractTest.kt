package com.example.woldeokmoneyverse

import com.example.woldeokmoneyverse.data.model.BankMovementRequest
import com.example.woldeokmoneyverse.data.model.TransferRequest
import com.example.woldeokmoneyverse.data.remote.MoneyverseApi
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

/** Guards mobile-BFF routes and money-write payloads against accidental drift. */
class MobileApiContractTest {
    @Test
    fun canonicalAuthRoutesArePinned() {
        val paths = MoneyverseApi::class.java.methods.flatMap { method ->
            listOfNotNull(
                method.getAnnotation(GET::class.java)?.value,
                method.getAnnotation(POST::class.java)?.value,
                method.getAnnotation(PUT::class.java)?.value,
                method.getAnnotation(DELETE::class.java)?.value
            )
        }.toSet()

        assertTrue("app-api/v1/auth/mobile/handoff" in paths)
        assertTrue("app-api/v1/auth/viewer" in paths)
        assertTrue("app-api/v1/auth/local/verify-email" in paths)
        assertTrue("app-api/v1/account" in paths)
        assertTrue("app-api/v1/support/threads" in paths)
        assertTrue("app-api/v1/support/threads/{id}/messages" in paths)
        assertTrue("app-api/v1/admin/support/threads" in paths)
        assertTrue("app-api/v1/admin/support/threads/{id}/status" in paths)
        assertTrue("app-api/v1/casino/coin/terms" in paths)
        assertTrue("app-api/v1/casino/coin/plays" in paths)
        assertTrue("app-api/v1/casino/dice/plays" in paths)
        assertTrue("app-api/v1/chat/conversations" in paths)
        assertTrue("app-api/v1/chat/unread-count" in paths)
        assertTrue("app-api/v1/chat/conversations/{id}/messages" in paths)
        assertTrue("app-api/v1/chat/conversations/{id}/read" in paths)
        assertTrue("app-api/v1/chat/conversations/{id}/mute" in paths)
        assertTrue("app-api/v1/chat/conversations/{id}/archive" in paths)
        assertTrue("app-api/v1/chat/conversations/{id}/report" in paths)
        assertTrue("app-api/v1/chat/users/{id}/block" in paths)
        assertTrue("app-api/v1/work" in paths)
    }

    @Test
    fun universalTransportKeepsAllCoreHttpVerbsAvailable() {
        val methods = MoneyverseApi::class.java.methods.associateBy { it.name }

        assertTrue(methods.getValue("universalGet").isAnnotationPresent(GET::class.java))
        assertTrue(methods.getValue("universalPost").isAnnotationPresent(POST::class.java))
        assertTrue(methods.getValue("universalPut").isAnnotationPresent(PUT::class.java))
        assertTrue(methods.getValue("universalPatch").isAnnotationPresent(PATCH::class.java))
        assertEquals("DELETE", methods.getValue("universalDelete").getAnnotation(HTTP::class.java)!!.method)
        assertEquals("DELETE", methods.getValue("universalDeleteNoBody").getAnnotation(HTTP::class.java)!!.method)
    }

    @Test
    fun moneyWritesUseCanonicalJsonFields() {
        val gson = Gson()
        val transfer = gson.fromJson(gson.toJson(TransferRequest("user-123", "1500")), Map::class.java)
        val movement = gson.fromJson(gson.toJson(BankMovementRequest("deposit", "1500")), Map::class.java)

        assertEquals("user-123", transfer["recipientUserId"])
        assertEquals("1500", transfer["amount"])
        assertTrue(transfer.containsKey("idempotencyKey"))
        assertEquals("deposit", movement["direction"])
        assertEquals("1500", movement["amount"])
        assertFalse(transfer.containsKey("recipient"))

        val huge = "100000000000000000000000000000000000000"
        val hugeTransfer = gson.fromJson(gson.toJson(TransferRequest("user-123", huge)), Map::class.java)
        assertEquals(huge, hugeTransfer["amount"])
    }
}
