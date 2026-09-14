package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Contract operations console. It is intentionally generic: all documented
 * endpoints remain callable while their domain-specific screens are rolled
 * out. Mutating calls require a deliberate user tap and show their server
 * result rather than treating a non-2xx response as success.
 */
@Composable
fun ApiOperationsScreen(scope: CoroutineScope) {
    var method by remember { mutableStateOf("GET") }
    var path by remember { mutableStateOf("app-api/v1/work") }
    var body by remember { mutableStateOf("{}") }
    var result by remember { mutableStateOf("아직 호출하지 않았습니다.") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("API 운영", style = MaterialTheme.typography.titleLarge)
        Text("명세서의 경로를 입력해 실제 서버 응답을 확인합니다. POST/PUT/DELETE는 사용자가 직접 실행해야 합니다.")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("GET", "POST", "PUT", "DELETE").forEach { option ->
                TextButton(onClick = { method = option }) { Text(if (method == option) "[$option]" else option) }
            }
        }
        OutlinedTextField(value = path, onValueChange = { path = it.trimStart('/') }, label = { Text("API 경로") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (method != "GET") {
            OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("JSON 요청 본문") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        }
        Button(
            enabled = !loading && path.startsWith("app-api/v1/"),
            onClick = {
                loading = true
                result = "요청 중…"
                scope.launch {
                    result = runCatching {
                        val json = if (method == "GET") null else JsonParser.parseString(body)
                        val response = when (method) {
                            "POST" -> ApiClient.api.contractPost(path, json)
                            "PUT" -> ApiClient.api.contractPut(path, json)
                            "DELETE" -> ApiClient.api.contractDelete(path, json)
                            else -> ApiClient.api.contractGet(path)
                        }
                        val payload = response.body()?.toString() ?: response.errorBody()?.string().orEmpty()
                        "HTTP ${response.code()}\n$payload"
                    }.getOrElse { "호출 실패: ${it.message}" }
                    loading = false
                }
            }
        ) { Text(if (loading) "호출 중…" else "$method 실행") }
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(result, modifier = Modifier.padding(12.dp), fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(24.dp))
    }
}
