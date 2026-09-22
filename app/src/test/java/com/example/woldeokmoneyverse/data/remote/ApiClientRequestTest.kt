package com.example.woldeokmoneyverse.data.remote

import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiClientRequestTest {
    @Test
    fun preservesCallerAcceptHeader() {
        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "app-api/v1/content/status")
            .header("Accept", "text/csv")
            .build()

        val decorated = ApiClient.decorateRequest(request)

        assertEquals("text/csv", decorated.header("Accept"))
    }

    @Test
    fun advertisesOneReleaseVersion() {
        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "app-api/v1/content/status")
            .build()

        val decorated = ApiClient.decorateRequest(request)

        assertEquals("WoldeokMoneyverse-Android/${ApiClient.APP_VERSION}", decorated.header("User-Agent"))
    }
}
