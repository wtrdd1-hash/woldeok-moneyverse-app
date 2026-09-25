package com.example.woldeokmoneyverse.data.remote

import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.net.URI

data class LobbyMessageDto(val sender: String, val text: String, val sentAt: String)

class RealtimeLobbyClient {
    private var socket: Socket? = null

    fun connect(
        onConnection: (Boolean) -> Unit,
        onPermission: (Boolean) -> Unit,
        onOnline: (Int) -> Unit,
        onMessage: (LobbyMessageDto) -> Unit,
        onError: (String) -> Unit
    ) {
        if (socket != null) return
        val headers = linkedMapOf<String, List<String>>(
            "Origin" to listOf(ApiClient.BASE_URL.removeSuffix("/")),
            "User-Agent" to listOf("WoldeokMoneyverse-Android/${ApiClient.APP_VERSION}"),
            "x-moneyverse-client" to listOf("android"),
            "x-moneyverse-app-version" to listOf("1.0.14")
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
        created.on(Socket.EVENT_CONNECT) { onConnection(true) }
        created.on(Socket.EVENT_DISCONNECT) { onConnection(false) }
        created.on(Socket.EVENT_CONNECT_ERROR) { args ->
            onConnection(false)
            onError(args.firstOrNull()?.toString() ?: "채팅 연결 실패")
        }
        created.on("lobby:permissions") { args ->
            val o = args.firstOrNull() as? JSONObject ?: return@on
            onPermission(o.optBoolean("canChat", false))
        }
        created.on("online") { args -> onOnline((args.firstOrNull() as? Number)?.toInt() ?: 0) }
        created.on("message:error") { args -> onError(args.firstOrNull()?.toString() ?: "메시지를 보낼 수 없습니다.") }
        created.on("lobby:message") { args ->
            val o = args.firstOrNull() as? JSONObject ?: return@on
            val text = o.optString("text").trim()
            if (text.isBlank()) return@on
            onMessage(LobbyMessageDto(o.optString("sender", "회원"), text, o.optString("sentAt", "")))
        }
        socket = created
        created.connect()
    }

    fun send(text: String) {
        val safe = text.trim().take(180)
        if (safe.isNotBlank()) socket?.emit("message", safe)
    }

    fun close() {
        socket?.off()
        socket?.disconnect()
        socket = null
    }
}
