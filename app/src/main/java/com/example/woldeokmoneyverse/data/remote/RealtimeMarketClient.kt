package com.example.woldeokmoneyverse.data.remote

import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.net.URI

data class LiveMarketQuote(val price: String, val open: String)

class RealtimeMarketClient {
    private var socket: Socket? = null

    fun connect(onPrices: (Map<String, LiveMarketQuote>) -> Unit, onConnected: (Boolean) -> Unit) {
        if (socket != null) return
        val headers = linkedMapOf<String, List<String>>(
            "Origin" to listOf(ApiClient.BASE_URL.removeSuffix("/")),
            "User-Agent" to listOf("WoldeokMoneyverse-Android/1.0.11"),
            "x-moneyverse-client" to listOf("android"),
            "x-moneyverse-app-version" to listOf("1.0.11")
        )
        val cookies = ApiClient.cookieJar
            ?.loadForRequest(ApiClient.BASE_URL.toHttpUrl())
            ?.joinToString("; ") { "${it.name}=${it.value}" }
            .orEmpty()
        if (cookies.isNotBlank()) headers["Cookie"] = listOf(cookies)

        val options = IO.Options().apply {
            transports = arrayOf("websocket", "polling")
            extraHeaders = headers
            reconnection = true
            reconnectionAttempts = Int.MAX_VALUE
            reconnectionDelay = 1_000
            reconnectionDelayMax = 15_000
            timeout = 15_000
        }
        val created = IO.socket(URI.create(ApiClient.BASE_URL), options)
        created.on(Socket.EVENT_CONNECT) {
            onConnected(true)
            created.emit("market:subscribe")
        }
        created.on(Socket.EVENT_DISCONNECT) { onConnected(false) }
        created.on(Socket.EVENT_CONNECT_ERROR) { onConnected(false) }
        created.on("market:prices") { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@on
            val rows = payload.optJSONArray("prices") ?: return@on
            val updates = LinkedHashMap<String, LiveMarketQuote>()
            for (index in 0 until rows.length()) {
                val row = rows.optJSONObject(index) ?: continue
                val id = row.optString("id")
                val price = row.optString("price")
                val open = row.optString("open")
                if (id.isNotBlank() && price.matches(Regex("\\d+")) && open.matches(Regex("\\d+"))) {
                    updates[id] = LiveMarketQuote(price, open)
                }
            }
            if (updates.isNotEmpty()) onPrices(updates)
        }
        socket = created
        created.connect()
    }

    fun close() {
        socket?.emit("market:unsubscribe")
        socket?.off()
        socket?.disconnect()
        socket = null
    }
}
