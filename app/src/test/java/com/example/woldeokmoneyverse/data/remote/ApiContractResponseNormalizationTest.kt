package com.example.woldeokmoneyverse.data.remote

import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ApiContractResponseNormalizationTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before fun setUp() {
        server = MockWebServer().apply { start() }
        client = OkHttpClient.Builder().addInterceptor(ApiContractCompatibilityInterceptor()).build()
    }

    @After fun tearDown() = server.shutdown()

    private fun get(path: String, body: String): String {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(body))
        return client.newCall(Request.Builder().url(server.url(path)).build()).execute().use { it.body!!.string() }
    }

    @Test fun portfolioEnvelopeProducesUiFields() {
        val raw = """{"holdings":[{"stock_id":"s1","symbol":"WLD","name":"Woldeok","quantity":"2","average_cost":"100","current_price":"120","market_value":"240"}]}"""
        val json = JsonParser.parseString(get("/app-api/v1/stocks/portfolio", raw)).asJsonObject
        assertEquals("240", json["totalStockValue"].asString)
        val h = json.getAsJsonArray("holdings")[0].asJsonObject
        assertEquals("s1", h["stockId"].asString)
        assertEquals(2, h["quantity"].asInt)
        assertEquals(20.0, h["profitLossPercent"].asDouble, 0.0001)
    }

    @Test fun seasonsEnvelopeBecomesLegacyList() {
        val raw = """{"events":[{"event_id":"e1","season_name":"Season 1","description":"test","ends_at":"2026-09-30"}]}"""
        val json = JsonParser.parseString(get("/app-api/v1/seasons/events", raw)).asJsonArray
        val season = json[0].asJsonObject
        assertEquals("e1", season["id"].asString)
        assertEquals("Season 1", season["name"].asString)
        assertEquals(0, season["currentProgress"].asInt)
        assertEquals(1, season["totalMilestone"].asInt)
    }

    @Test fun shopEnvelopeBecomesLegacyList() {
        val raw = """{"items":[{"itemId":"i1","name":"Frame","description":"x","price":"100","createdAt":"2026-09-14"}]}"""
        val json = JsonParser.parseString(get("/app-api/v1/shop/items", raw)).asJsonArray
        val item = json[0].asJsonObject
        assertEquals("i1", item["id"].asString)
        assertFalse(item["isOwned"].asBoolean)
        assertTrue(item.has("iconUrl"))
    }

    @Test fun ownedBusinessesReceiveUiCompatibilityFields() {
        val raw = """{"businesses":[{"ownershipId":"b1","businessTypeId":"t1","name":"Cafe","status":"active","isSettledToday":false,"dailyRevenue":"1000","dailyOperatingCost":"250"}]}"""
        val json = JsonParser.parseString(get("/app-api/v1/businesses/my-v2", raw)).asJsonObject
        val b = json.getAsJsonArray("businesses")[0].asJsonObject
        assertEquals("b1", b["id"].asString)
        assertTrue(b["isSettlementReady"].asBoolean)
        assertEquals("750", b["pendingRevenue"].asString)
        assertTrue(b["licenseActive"].asBoolean)
    }

    @Test fun casinoLimitSnakeCaseIsNormalized() {
        val raw = """{"daily_bet_limit":"5000","daily_loss_limit":"1000","locked_until":null}"""
        val json = JsonParser.parseString(get("/app-api/v1/casino/self-limit", raw)).asJsonObject
        assertEquals(5000L, json["dailyBetLimit"].asLong)
        assertEquals(1000L, json["dailyLossLimit"].asLong)
        assertTrue(json.has("lockedUntil"))
    }

    @Test fun boardListKeepsEnvelopeAndAddsLegacyFields() {
        val raw = """{"posts":[{"postId":"p1","title":"Hello","body":"World","authorName":"tester","createdAt":"2026-09-14","commentCount":0,"mine":true,"hasImage":false}]}"""
        val json = JsonParser.parseString(get("/app-api/v1/board/posts", raw)).asJsonObject
        val post = json.getAsJsonArray("posts")[0].asJsonObject
        assertEquals("World", post["content"].asString)
        assertEquals("", post["authorId"].asString)
        assertTrue(post.getAsJsonArray("comments").isEmpty)
    }
}
