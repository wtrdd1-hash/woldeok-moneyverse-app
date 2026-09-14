package com.example.woldeokmoneyverse.data.remote

import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ApiContractCompatibilityInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before fun setUp() {
        server = MockWebServer().apply { start() }
        client = OkHttpClient.Builder()
            .addInterceptor(ApiContractCompatibilityInterceptor())
            .build()
    }

    @After fun tearDown() = server.shutdown()

    private fun url(path: String) = server.url(path)
    @Test fun stocksEnvelopeIsNormalizedForLegacyUi() {
        server.enqueue(MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""{"stocks":[{"id":"s1","symbol":"WLD","name":"Woldeok","current_price":"110","day_open_price":"100"}]}"""))

        val response = client.newCall(Request.Builder().url(url("/app-api/v1/stocks")).build()).execute()
        val json = JsonParser.parseString(response.body!!.string()).asJsonArray
        assertEquals(1, json.size())
        val stock = json[0].asJsonObject
        assertEquals("110", stock["currentPrice"].asString)
        assertEquals(10.0, stock["priceChangePercent"].asDouble, 0.0001)
        assertTrue(stock["isMarketOpen"].asBoolean)
    }

    @Test fun borrowRequestUsesCanonicalPrincipalAmount() {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("{}"))
        val body = """{"amount":5000,"idempotencyKey":"k"}""".toRequestBody(JSON)
        client.newCall(Request.Builder().url(url("/app-api/v1/bank/loans")).post(body).build()).execute().close()
        val sent = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals(5000, sent["principalAmount"].asInt)
        assertFalse(sent.has("amount"))
    }
    @Test fun settlementGetsIdempotencyBody() {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("{}"))
        client.newCall(Request.Builder()
            .url(url("/app-api/v1/businesses/biz-1/settlements"))
            .post(ByteArray(0).toRequestBody(null))
            .build()).execute().close()
        val sent = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertTrue(sent["idempotencyKey"].asString.isNotBlank())
    }

    @Test fun privacyLegacyQueryBecomesCanonicalJsonBody() {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("{}"))
        client.newCall(Request.Builder()
            .url(url("/app-api/v1/privacy/requests?type=EXPORT"))
            .post(ByteArray(0).toRequestBody(null))
            .build()).execute().close()
        val recorded = server.takeRequest()
        assertNull(recorded.requestUrl!!.queryParameter("type"))
        val sent = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        assertEquals("access", sent["requestType"].asString)
        assertTrue(sent["idempotencyKey"].asString.isNotBlank())
    }
    @Test fun binaryResponsesAreNotConsumedOrReencoded() {
        val bytes = byteArrayOf(0x00, 0x7f, 0x01, 0x02, 0x03)
        server.enqueue(MockResponse()
            .setHeader("Content-Type", "image/png")
            .setBody(okio.Buffer().write(bytes)))
        val response = client.newCall(Request.Builder().url(url("/app-api/v1/media/test.png")).build()).execute()
        assertArrayEquals(bytes, response.body!!.bytes())
    }

    @Test fun boardContentIsSentAsCanonicalBodyField() {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("{}"))
        val body = """{"title":"hello","content":"world","idempotencyKey":"k"}""".toRequestBody(JSON)
        client.newCall(Request.Builder().url(url("/app-api/v1/board/posts")).post(body).build()).execute().close()
        val sent = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("world", sent["body"].asString)
        assertFalse(sent.has("content"))
    }

    companion object {
        private val JSON = "application/json".toMediaType()
    }
}
