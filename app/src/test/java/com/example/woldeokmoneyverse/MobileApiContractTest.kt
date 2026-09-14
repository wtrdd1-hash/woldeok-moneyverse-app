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
    }

    @Test
    fun moneyWritesUseCanonicalJsonFields() {
        val gson = Gson()
        val transfer = gson.fromJson(gson.toJson(TransferRequest("user-123", 1500L)), Map::class.java)
        val movement = gson.fromJson(gson.toJson(BankMovementRequest("deposit", 1500L)), Map::class.java)

        assertEquals("user-123", transfer["recipientUserId"])
        assertTrue(transfer["amount"] is Number)
        assertTrue(transfer.containsKey("idempotencyKey"))
        assertEquals("deposit", movement["direction"])
        assertTrue(movement["amount"] is Number)
        assertFalse(transfer.containsKey("recipient"))
    }
}
