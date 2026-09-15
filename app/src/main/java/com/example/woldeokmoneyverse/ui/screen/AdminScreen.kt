package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import com.google.gson.JsonElement
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

private data class AdminLivePanel(
    val label: String,
    val path: String,
    val status: Int,
    val body: String
)

@Composable
fun AdminScreen(adminRoles: List<String>) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var consoleOpen by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var panels by remember { mutableStateOf<List<AdminLivePanel>>(emptyList()) }

    suspend fun read(path: String, label: String): AdminLivePanel {
        val response = ApiClient.api.contractGet("app-api/v1/$path")
        val body = response.body()?.prettySummary() ?: "응답 본문 없음"
        return AdminLivePanel(label, path, response.code(), body)
    }

    suspend fun load() {
        loading = true
        notice = null
        runCatching {
            if (!consoleOpen) {
                val opened = ApiClient.api.contractPost("app-api/v1/admin/security/sessions")
                if (!opened.isSuccessful) {
                    throw IllegalStateException("관리자 세션 시작 실패 (HTTP ${opened.code()})")
                }
                consoleOpen = true
            }
            panels = listOf(
                "admin/work" to "직업/보상 정책",
                "admin/bank?limit=20" to "은행/대출",
                "admin/economy/stats" to "경제 통계",
                "admin/controls/auto-policy" to "자동 정책",
                "admin/users" to "사용자 관리",
                "admin/discord" to "Discord 전달 상태"
            ).map { (path, label) -> scope.async { read(path, label) } }.awaitAll()
        }.onFailure {
            notice = it.message ?: "관리자 데이터를 불러오지 못했습니다."
        }
        loading = false
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "관리자",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        MoneyverseCard {
            Text("서버 관리자 권한 확인 완료", fontWeight = FontWeight.Bold)
            Text("권한: ${adminRoles.joinToString(", ")}")
            Text(
                if (consoleOpen) "운영 관리자 세션 연결됨 · 서버 API 실시간 조회 중"
                else "운영 관리자 세션 연결 중",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (loading) {
            CircularProgressIndicator()
            Text("관리자 기능을 서버에서 확인하고 있습니다…")
        }

        notice?.let {
            MoneyverseCard {
                Text("관리자 API 오류", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }

        panels.forEach { panel ->
            MoneyverseCard {
                Text(panel.label, fontWeight = FontWeight.Bold)
                Text(
                    "HTTP ${panel.status} · /${panel.path}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (panel.status in 200..299) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(6.dp))
                Text(panel.body, style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(
            onClick = { scope.launch { load() } },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("관리자 데이터 새로고침")
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    val response = ApiClient.api.contractDeleteNoBody("app-api/v1/admin/security/sessions")
                    consoleOpen = false
                    panels = emptyList()
                    notice = if (response.isSuccessful) "관리자 세션을 종료했습니다." else "관리자 세션 종료 실패 (HTTP ${response.code()})"
                }
            },
            enabled = consoleOpen && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("관리자 세션 종료")
        }
    }
}

private fun JsonElement.prettySummary(maxChars: Int = 1200): String {
    val raw = toString()
    return if (raw.length <= maxChars) raw else raw.take(maxChars) + "…"
}
