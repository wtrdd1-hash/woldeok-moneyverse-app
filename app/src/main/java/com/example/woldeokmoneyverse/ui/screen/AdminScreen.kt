package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import com.example.woldeokmoneyverse.ui.viewmodel.AdminViewModel

@Composable
fun AdminScreen(adminRoles: List<String>, adminViewModel: AdminViewModel = viewModel()) {
    val state by adminViewModel.state.collectAsState()
    var reply by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { adminViewModel.openAndLoad() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("관리자", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text("권한: ${adminRoles.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
        }
        item {
            MoneyverseCard {
                Text("서버 관리자 콘솔", fontWeight = FontWeight.Bold)
                Text(if (state.consoleOpen) "관리자 세션 연결 완료" else if (state.loading) "관리자 세션 연결 중…" else "연결 필요")
                Text("회원 ${state.memberCount?.toString() ?: "-"}명 · ${state.workSummary}", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { adminViewModel.refresh() }, modifier = Modifier.fillMaxWidth()) { Text("운영 정보 새로고침") }
            }
        }
        if (state.livePanels.isNotEmpty()) {
            item { Text("실시간 운영 API", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
            items(state.livePanels) { panel ->
                MoneyverseCard {
                    Text(panel.label, fontWeight = FontWeight.Bold)
                    Text(
                        if (panel.status in 200..299) "정상 연결" else "연결 확인 필요",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (panel.status in 200..299) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(panel.preview, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            MoneyverseCard {
                Text("📋 실제 관리자 활동 로그", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("서버 활동 로그 · 최근 ${state.activityLogs.size}건", style = MaterialTheme.typography.bodySmall)
                if (state.activityLogs.isEmpty()) Text("표시할 활동 로그가 없습니다.", style = MaterialTheme.typography.bodySmall)
                state.activityLogs.take(20).forEach { log ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Text("${log.eventType} · ${log.username.ifBlank { log.userId ?: "비로그인" }}", fontWeight = FontWeight.SemiBold)
                    Text(log.path, style = MaterialTheme.typography.bodySmall)
                    Text(log.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("🛟 회원 문의함", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
        if (state.threads.isEmpty()) {
            item { MoneyverseCard { Text(if (state.loading) "문의함 불러오는 중…" else "현재 문의가 없습니다.") } }
        } else {
            items(state.threads) { thread ->
                MoneyverseCard(onClick = { adminViewModel.selectThread(thread.threadId) }) {
                    Text(thread.subject, fontWeight = FontWeight.Bold)
                    Text("${thread.displayName ?: "회원"} · ${thread.status}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        state.selectedThreadId?.let { id ->
            item {
                MoneyverseCard {
                    Text("선택한 문의 대화", fontWeight = FontWeight.Bold)
                    state.messages.forEach { msg ->
                        Text((if (msg.senderKind == "admin") "관리자: " else "회원: ") + msg.body, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    OutlinedTextField(reply, { reply = it.take(2000) }, label = { Text("관리자 답장") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Button(onClick = { adminViewModel.reply(reply); reply = "" }, enabled = reply.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("답장 보내기") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { adminViewModel.setStatus("open") }) { Text("답변 대기") }
                        OutlinedButton(onClick = { adminViewModel.setStatus("waiting_user") }) { Text("회원 대기") }
                        OutlinedButton(onClick = { adminViewModel.setStatus("resolved") }) { Text("완료") }
                    }
                }
            }
        }
        state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
    }
}
