package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woldeokmoneyverse.data.model.AdminFeatureSwitchDto
import com.example.woldeokmoneyverse.data.model.AdminOverviewDto
import com.example.woldeokmoneyverse.data.model.AdminUserDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import com.example.woldeokmoneyverse.ui.viewmodel.AdminViewModel

@Composable
fun AdminScreen(
    adminRoles: List<String>,
    adminViewModel: AdminViewModel = viewModel()
) {
    val state by adminViewModel.state.collectAsState()
    val overviewState by adminViewModel.overviewState.collectAsState()
    val switchesState by adminViewModel.featureSwitchesState.collectAsState()
    val usersState by adminViewModel.adminUsersState.collectAsState()
    val adminMessage by adminViewModel.adminMessage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("관제 & 킬스위치", "감사 & 실시간 API", "1:1 문의함", "개발자 포털 (전체 API)")

    LaunchedEffect(Unit) {
        adminViewModel.openAndLoad()
        adminViewModel.loadControlTowerData()
    }

    Scaffold(
        snackbarHost = {
            adminMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { adminViewModel.clearAdminMessage() }) {
                            Text("확인", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 헤더 정보
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "👑 마스터 콘솔 (Admin Tower)",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "보유 권한: ${adminRoles.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (state.consoleOpen) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (state.consoleOpen) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.error
                                        )
                                )
                                Text(
                                    if (state.consoleOpen) "세션 활성" else "세션 대기",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (state.consoleOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // 서브 내비게이션 탭 (ScrollableTabRow)
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // 탭별 콘텐츠
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> AdminControlsTab(
                        state = state,
                        overviewState = overviewState,
                        switchesState = switchesState,
                        usersState = usersState,
                        adminViewModel = adminViewModel
                    )
                    1 -> AdminLogsAndApiTab(
                        state = state,
                        adminViewModel = adminViewModel
                    )
                    2 -> AdminSupportTab(
                        state = state,
                        adminViewModel = adminViewModel
                    )
                    3 -> CapabilityScreen(adminOnly = false)
                }
            }
        }
    }
}

@Composable
private fun AdminControlsTab(
    state: com.example.woldeokmoneyverse.ui.viewmodel.AdminUiState,
    overviewState: UiState<AdminOverviewDto>,
    switchesState: UiState<List<AdminFeatureSwitchDto>>,
    usersState: UiState<List<AdminUserDto>>,
    adminViewModel: AdminViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 서버 콘솔 및 경제 지표 카드
        item {
            MoneyverseCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📊 실시간 경제 & 시스템 지표", fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = {
                        adminViewModel.refresh()
                        adminViewModel.loadControlTowerData()
                    }) {
                        Text("새로고침", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                when (val ov = overviewState) {
                    is UiState.Success -> {
                        val data = ov.data
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricBox("전체 유저", "${data.totalUsers}명", Modifier.weight(1f))
                            MetricBox("금일 활성", "${data.activeUsersToday}명", Modifier.weight(1f))
                            MetricBox("대기 문의", "${data.pendingSupports}건", Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricBox("총 WLD 발행량", data.totalWldSupply, Modifier.weight(1f))
                            MetricBox("카지노 회전량", data.casinoTurnover24h, Modifier.weight(1f))
                        }
                    }
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                    }
                    is UiState.Error -> {
                        Text("지표 로딩 실패: ${ov.message}", color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "서버 등록 회원: ${state.memberCount?.toString() ?: "-"}명 · ${state.workSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 킬스위치 및 기능 플래그 제어 그리드
        item {
            MoneyverseCard {
                Text(
                    "🛑 긴급 킬스위치 & 피처 플래그 제어",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "특정 기능의 장애 또는 남용 발생 시 실시간으로 앱 내 진입을 차단합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                when (val sw = switchesState) {
                    is UiState.Success -> {
                        sw.data.forEach { item ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(item.name, fontWeight = FontWeight.SemiBold)
                                    Text(item.description.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = item.enabled,
                                    onCheckedChange = { adminViewModel.toggleFeatureSwitch(item.featureKey, item.enabled) }
                                )
                            }
                        }
                    }
                    is UiState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(8.dp))
                    }
                    is UiState.Error -> {
                        Text("스위치 목록 불러오기 실패: ${sw.message}", color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }
        }

        // 회원 권한 및 동결 관리
        item {
            MoneyverseCard {
                Text(
                    "👤 유저 디렉토리 & 계정 동결(Freeze) 제어",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "어뷰징 유저 및 규정 위반 계정의 활동을 즉각 정지하거나 해제합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                when (val us = usersState) {
                    is UiState.Success -> {
                        us.data.forEach { user ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(user.displayName, fontWeight = FontWeight.SemiBold)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (user.role == "ADMIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                user.role,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { adminViewModel.freezeUser(user.userId, !user.isFrozen) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (user.isFrozen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(if (user.isFrozen) "동결 해제" else "계정 동결", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    is UiState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(8.dp))
                    }
                    is UiState.Error -> {
                        Text("유저 목록 불러오기 실패: ${us.message}", color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun AdminLogsAndApiTab(
    state: com.example.woldeokmoneyverse.ui.viewmodel.AdminUiState,
    adminViewModel: AdminViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 실시간 운영 API 응답 패널
        if (state.livePanels.isNotEmpty()) {
            item {
                Text(
                    "⚡ 실시간 백엔드 운영 API 상태",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            items(state.livePanels) { panel ->
                MoneyverseCard {
                    Text(panel.label, fontWeight = FontWeight.Bold)
                    Text(
                        "HTTP ${panel.status} · /${panel.path}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (panel.status in 200..299) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        panel.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 실제 관리자 활동 로그
        item {
            MoneyverseCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋 실제 관리자 활동 및 감사 로그", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("최근 ${state.activityLogs.size}건", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (state.activityLogs.isEmpty()) {
                    Text("기록된 활동 로그가 없습니다.", style = MaterialTheme.typography.bodySmall)
                } else {
                    state.activityLogs.take(30).forEach { log ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${log.eventType} · ${log.username.ifBlank { log.userId ?: "시스템" }}", fontWeight = FontWeight.SemiBold)
                            Text(log.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(log.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSupportTab(
    state: com.example.woldeokmoneyverse.ui.viewmodel.AdminUiState,
    adminViewModel: AdminViewModel
) {
    var reply by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("🛟 1:1 고객 지원 문의함", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text("회원들이 접수한 고객 문의를 실시간 확인하고 직접 답변합니다.", style = MaterialTheme.typography.bodySmall)
        }

        if (state.threads.isEmpty()) {
            item {
                MoneyverseCard {
                    Text(if (state.loading) "문의함 불러오는 중…" else "현재 등록된 문의가 없습니다.")
                }
            }
        } else {
            item {
                Text("접수된 문의 스레드", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            items(state.threads) { thread ->
                MoneyverseCard(onClick = { adminViewModel.selectThread(thread.threadId) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(thread.subject, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (thread.status) {
                                "open" -> MaterialTheme.colorScheme.errorContainer
                                "waiting_user" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                when (thread.status) {
                                    "open" -> "답변 대기"
                                    "waiting_user" -> "회원 확인중"
                                    "resolved" -> "해결 완료"
                                    else -> thread.status
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text("${thread.displayName ?: "회원"} · ${thread.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        state.selectedThreadId?.let { id ->
            item {
                MoneyverseCard {
                    Text("💬 선택한 문의 대화 내역", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    state.messages.forEach { msg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (msg.senderKind == "admin") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    if (msg.senderKind == "admin") "👑 관리자 답변" else "👤 회원 질문",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg.body, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reply,
                        onValueChange = { reply = it.take(2000) },
                        label = { Text("관리자 공식 답변 작성") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            adminViewModel.reply(reply)
                            reply = ""
                        },
                        enabled = reply.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("답장 전송")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { adminViewModel.setStatus("open") }, modifier = Modifier.weight(1f)) {
                            Text("답변 대기", fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = { adminViewModel.setStatus("waiting_user") }, modifier = Modifier.weight(1f)) {
                            Text("회원 대기", fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = { adminViewModel.setStatus("resolved") }, modifier = Modifier.weight(1f)) {
                            Text("해결 완료", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
