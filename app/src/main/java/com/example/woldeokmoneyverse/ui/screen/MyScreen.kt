package com.example.woldeokmoneyverse.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.woldeokmoneyverse.R
import com.example.woldeokmoneyverse.data.local.SessionManager
import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.ui.theme.ThemePreset
import com.example.woldeokmoneyverse.ui.viewmodel.AuthViewModel
import com.example.woldeokmoneyverse.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    accountViewModel: com.example.woldeokmoneyverse.ui.viewmodel.AccountViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    adminViewModel: com.example.woldeokmoneyverse.ui.viewmodel.AdminViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme by settingsViewModel.selectedTheme.collectAsState()
    val customColor by settingsViewModel.customPrimaryColor.collectAsState()

    val sessionsState by accountViewModel.sessionsState.collectAsState()
    val securityLogsState by accountViewModel.securityLogsState.collectAsState()
    val accountMessage by accountViewModel.message.collectAsState()
    val accountBusy by accountViewModel.busy.collectAsState()
    val adminMessage by adminViewModel.adminMessage.collectAsState()

    var showTermsModal by remember { mutableStateOf(false) }
    var showPrivacyModal by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showTwoFactorDialog by remember { mutableStateOf(false) }
    var showAdminControlTower by remember { mutableStateOf(false) }
    var showSecurityLogs by remember { mutableStateOf(false) }
    var showNotificationInboxDialog by remember { mutableStateOf(false) }
    var showSafetyCenterDialog by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf(SessionManager.getProfileImageUri(context)) }

    LaunchedEffect(Unit) {
        accountViewModel.loadSessions()
        accountViewModel.loadSecurityLogs()
    }

    LaunchedEffect(accountMessage) {
        accountMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            accountViewModel.clearMessage()
        }
    }

    LaunchedEffect(adminMessage) {
        adminMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            adminViewModel.clearAdminMessage()
        }
    }

    val profileImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            SessionManager.setProfileImageUri(context, it.toString())
            profileImageUri = it.toString()
        }
    }

    val displayName = remember { SessionManager.getDisplayName(context) }
    val userEmail = remember { SessionManager.getEmail(context) }
    val userTitle = remember { SessionManager.getTitle(context) }
    val userLevel = remember { SessionManager.getLevel(context) }

    val accentSwatches = listOf(
        Color(0xFF6366F1),
        Color(0xFF10B981),
        Color(0xFF06B6D4),
        Color(0xFFF59E0B),
        Color(0xFFEC4899),
        Color(0xFFA855F7),
        Color(0xFFEF4444)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("👤 MY 페이지 & 사용자 테마 설정", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (profileImageUri != null) {
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                    setImageURI(Uri.parse(profileImageUri))
                                }
                            },
                            update = { it.setImageURI(Uri.parse(profileImageUri)) },
                            modifier = Modifier.size(88.dp).clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", style = MaterialTheme.typography.headlineLarge)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(displayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Lv.$userLevel $userTitle", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { profileImagePicker.launch(arrayOf("image/*")) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (profileImageUri == null) "프로필 이미지 선택" else "프로필 이미지 변경") }
                        if (profileImageUri != null) {
                            TextButton(
                                onClick = {
                                    SessionManager.setProfileImageUri(context, null)
                                    profileImageUri = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("기본 이미지로 되돌리기") }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔐 로그인 계정 정보", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• 사용자 닉네임: $displayName (Lv.$userLevel $userTitle)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text("• 등록된 이메일: $userEmail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("• 연동 백엔드: easy-scraping.com (공식 BFF 규격 적용)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // --- 🔒 로그인 기기 세션 및 보안 관리 카드 ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒 로그인 기기 세션 관리", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { accountViewModel.loadSessions() }) {
                            Text("🔄", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text("현재 계정으로 로그인된 스마트폰/PC 기기 목록입니다. 분실 기기는 즉시 원격 로그아웃할 수 있습니다.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    when (val sState = sessionsState) {
                        is com.example.woldeokmoneyverse.data.model.UiState.Success -> {
                            val sessions = sState.data
                            if (sessions.isEmpty()) {
                                Text("활성 세션 정보가 없습니다.", style = MaterialTheme.typography.bodySmall)
                            } else {
                                sessions.forEach { session ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (session.isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(session.userAgent, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                    if (session.isCurrent) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        SuggestionChip(onClick = {}, label = { Text("현재 기기") })
                                                    }
                                                }
                                                Text("IP: ${session.ipAddress} · 최근 활동: ${session.lastActiveAt.ifBlank { "방금 전" }}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            if (!session.isCurrent) {
                                                TextButton(
                                                    onClick = { accountViewModel.revokeSession(session.sessionId) },
                                                    enabled = !accountBusy
                                                ) {
                                                    Text("원격 로그아웃", color = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (sessions.size > 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { accountViewModel.revokeOtherSessions() },
                                        enabled = !accountBusy,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("⚠️ 현재 기기 제외 모든 기기 일괄 로그아웃")
                                    }
                                }
                            }
                        }
                        is com.example.woldeokmoneyverse.data.model.UiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                        is com.example.woldeokmoneyverse.data.model.UiState.Error -> {
                            Text("세션 로드 실패: ${sState.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        else -> Unit
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("🛡️ 계정 보안 및 인증", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showChangePasswordDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("비밀번호 변경")
                        }
                        OutlinedButton(
                            onClick = {
                                accountViewModel.startTwoFactorSetup()
                                showTwoFactorDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("2FA 인증 등록")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showSecurityLogs = !showSecurityLogs },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showSecurityLogs) "▲ 최근 보안 이벤트 로그 닫기" else "▼ 최근 보안 이벤트 로그 확인")
                    }

                    if (showSecurityLogs) {
                        when (val lState = securityLogsState) {
                            is com.example.woldeokmoneyverse.data.model.UiState.Success -> {
                                val logs = lState.data
                                if (logs.isEmpty()) {
                                    Text("보안 활동 로그가 없습니다.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    logs.take(5).forEach { log ->
                                        Text("• [${log.eventType}] ${log.ipAddress} - ${log.createdAt}", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            else -> Text("보안 로그를 불러오는 중...", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // --- 👑 시스템 관리자 관제탑 카드 ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("시스템 관리자 관제탑 (Admin Tower)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("실시간 금융 관제, 시스템 킬스위치 토글, 이상 유저 계정 동결 및 감사 로그를 원스톱으로 제어합니다.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            adminViewModel.loadControlTowerData()
                            showAdminControlTower = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("관제탑 콘솔 열기 (Control Tower)")
                    }
                }
            }

            // --- 📬 알림 거버넌스 인박스 카드 ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📬", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("알림 거버넌스 인박스 (Notification Center)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("중요 거래(TRANSACTIONAL) 및 보안(SECURITY_CRITICAL) 알림을 확인하고, 알림 수신 동의 설정을 관리합니다.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            accountViewModel.loadNotifications()
                            accountViewModel.loadNotificationPreferences()
                            showNotificationInboxDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("알림함 및 수신 설정 열기")
                    }
                }
            }

            // --- 🚸 미성년자 안전 및 긴급 삭제 센터 카드 ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚸", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("미성년자 안전 & 긴급 삭제 센터", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("디지털 잊힐 권리를 보장하는 긴급 삭제(Takedown) 신청, 접수 상태 조회 및 계정 안전 상태를 확인합니다.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            accountViewModel.loadAccountSafety()
                            showSafetyCenterDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("안전 센터 및 긴급 삭제 열기")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎨 앱 테마 & 강조 색상(Accent Color) 커스텀", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("원하는 프리셋 테마 또는 커스텀 시그니처 색상을 직접 선택하세요.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("🎯 커스텀 메인 강조 색상 선택:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.size(40.dp).clip(CircleShape).clickable { settingsViewModel.setCustomPrimaryColor(null) },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) { Text("🔄", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                        items(accentSwatches) { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (customColor == color) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        shape = CircleShape
                                    )
                                    .clickable { settingsViewModel.setCustomPrimaryColor(color) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎨 프리셋 테마 모드:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))

                    ThemePreset.entries.forEach { preset ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                settingsViewModel.setCustomPrimaryColor(null)
                                settingsViewModel.setThemePreset(preset)
                            }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == preset && customColor == null,
                                onClick = {
                                    settingsViewModel.setCustomPrimaryColor(null)
                                    settingsViewModel.setThemePreset(preset)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(preset.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📄 약관 및 개인정보 처리방침", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showTermsModal = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(stringResource(R.string.terms_title)) }
                        OutlinedButton(
                            onClick = { showPrivacyModal = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(stringResource(R.string.privacy_title)) }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ℹ️ 앱 및 보안 정보", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("앱 패키지: com.woldeok.moneyverse", style = MaterialTheme.typography.bodySmall)
                    Text("앱 버전: v1.2.9 (Native Compose)", style = MaterialTheme.typography.bodySmall)
                    Text("BFF 규격: /app-api/v1/* (easy-scraping.com 고정)", style = MaterialTheme.typography.bodySmall)
                    Text("보안 정책: INTERNAL_API_TOKEN 저장 금지, Session+CSRF 적용", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showLogoutDialog = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text(stringResource(R.string.logout_button)) }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("회원 탈퇴 (계정 및 데이터 영구 삭제)") }
        }
    }

    if (showTermsModal) {
        AlertDialog(
            onDismissRequest = { showTermsModal = false },
            title = { Text(stringResource(R.string.terms_title)) },
            text = { Text(stringResource(R.string.terms_content)) },
            confirmButton = { Button(onClick = { showTermsModal = false }) { Text("확인") } }
        )
    }

    if (showPrivacyModal) {
        AlertDialog(
            onDismissRequest = { showPrivacyModal = false },
            title = { Text(stringResource(R.string.privacy_title)) },
            text = { Text(stringResource(R.string.privacy_content)) },
            confirmButton = { Button(onClick = { showPrivacyModal = false }) { Text("확인") } }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_button)) },
            text = { Text("현재 기기에서 로그아웃하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout {
                            SessionManager.clearSession(context)
                            onLoggedOut()
                        }
                    }
                ) { Text(stringResource(R.string.logout_button)) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("취소") } }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("회원 탈퇴 (계정 삭제)") },
            text = {
                Text(
                    "정말로 월덕 머니버스 계정을 탈퇴하시겠습니까?\n\n" +
                        "탈퇴 시 계정에 연결된 잔액, 투자 자산, 거래 내역 및 모든 활동 정보가 영구적으로 삭제되며 복구할 수 없습니다."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        authViewModel.deleteAccount { SessionManager.clearSession(context) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("탈퇴 및 데이터 삭제") }
            },
            dismissButton = { TextButton(onClick = { showDeleteAccountDialog = false }) { Text("취소") } }
        )
    }

    // 비밀번호 변경 다이얼로그
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            busy = accountBusy,
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { current, new ->
                accountViewModel.changePassword(current, new) {
                    showChangePasswordDialog = false
                }
            }
        )
    }

    // 2FA 설정 다이얼로그
    if (showTwoFactorDialog) {
        val setupState by accountViewModel.twoFactorSetupState.collectAsState()
        TwoFactorDialog(
            setupState = setupState,
            busy = accountBusy,
            onDismiss = { showTwoFactorDialog = false },
            onVerify = { code ->
                accountViewModel.verifyTwoFactor(code) {
                    showTwoFactorDialog = false
                }
            }
        )
    }

    // 시스템 관리자 관제탑 시트
    if (showAdminControlTower) {
        AdminControlTowerSheet(
            adminViewModel = adminViewModel,
            onDismiss = { showAdminControlTower = false }
        )
    }

    if (showNotificationInboxDialog) {
        NotificationInboxDialog(
            accountViewModel = accountViewModel,
            onDismiss = { showNotificationInboxDialog = false }
        )
    }

    if (showSafetyCenterDialog) {
        SafetyCenterDialog(
            accountViewModel = accountViewModel,
            onDismiss = { showSafetyCenterDialog = false }
        )
    }
}

@Composable
fun ChangePasswordDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    val isMatch = newPass.isNotBlank() && newPass == confirmPass

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("🔑 비밀번호 변경") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    label = { Text("현재 비밀번호") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("새 비밀번호") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = { Text("새 비밀번호 확인") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPass.isNotBlank() && !isMatch,
                    enabled = !busy
                )
                if (confirmPass.isNotBlank() && !isMatch) {
                    Text("새 비밀번호가 일치하지 않습니다.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentPass, newPass) },
                enabled = !busy && currentPass.isNotBlank() && isMatch
            ) {
                Text(if (busy) "변경 중…" else "비밀번호 변경")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("취소") }
        }
    )
}

@Composable
fun TwoFactorDialog(
    setupState: com.example.woldeokmoneyverse.data.model.UiState<com.example.woldeokmoneyverse.data.model.TwoFactorSetupResponse>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("🔐 2단계 인증(2FA/TOTP) 등록") },
        text = {
            Column {
                when (setupState) {
                    is com.example.woldeokmoneyverse.data.model.UiState.Success -> {
                        val setup = setupState.data
                        Text("Google Authenticator 앱에 아래 보안 키를 등록하세요:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                setup.secret.ifBlank { "WLD-MVERSE-SEC-7718" },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    is com.example.woldeokmoneyverse.data.model.UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    else -> {
                        Text("보안 키: WLD-MVERSE-TOTP-SEC", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it },
                    label = { Text("6자리 인증코드 입력") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onVerify(code) },
                enabled = !busy && code.length == 6
            ) {
                Text(if (busy) "인증 중…" else "2FA 등록 완료")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("취소") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlTowerSheet(
    adminViewModel: com.example.woldeokmoneyverse.ui.viewmodel.AdminViewModel,
    onDismiss: () -> Unit
) {
    val overviewState by adminViewModel.overviewState.collectAsState()
    val switchesState by adminViewModel.featureSwitchesState.collectAsState()
    val usersState by adminViewModel.adminUsersState.collectAsState()
    val adminUiState by adminViewModel.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("👑 시스템 관리자 관제탑", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("실시간 경제 통계 및 킬스위치 제어 콘솔", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { adminViewModel.loadControlTowerData(); adminViewModel.refresh() }) {
                    Text("🔄", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
            ) {
                // 1. 핵심 금융 및 활동 지표 그리드
                item {
                    Text("📊 실시간 핵심 지표 (Metrics)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    when (val oState = overviewState) {
                        is com.example.woldeokmoneyverse.data.model.UiState.Success -> {
                            val o = oState.data
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("총 회원수", style = MaterialTheme.typography.labelSmall)
                                            Text("${o.totalUsers}명", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("일일 활동(DAU)", style = MaterialTheme.typography.labelSmall)
                                            Text("${o.activeUsersToday}명", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("WLD 통화 유통량", style = MaterialTheme.typography.labelSmall)
                                            Text("${o.totalWldSupply} WLD", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("24h 카지노 볼륨", style = MaterialTheme.typography.labelSmall)
                                            Text("${o.casinoTurnover24h} WLD", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 2. 기능 킬스위치 & 피처 플래그 토글
                item {
                    Text("⚡ 피처 플래그 & 비상 킬스위치", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("이상 거래 발생 시 특정 기능의 인프라를 즉시 차단합니다.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (val swState = switchesState) {
                    is com.example.woldeokmoneyverse.data.model.UiState.Success -> {
                        items(swState.data) { sw ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (sw.enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sw.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(sw.description ?: sw.featureKey, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = sw.enabled,
                                        onCheckedChange = { adminViewModel.toggleFeatureSwitch(sw.featureKey, sw.enabled) }
                                    )
                                }
                            }
                        }
                    }
                    else -> Unit
                }

                // 3. 회원 관리 및 동결
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("👥 회원 관리 & 계정 동결 조치", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (val uState = usersState) {
                    is com.example.woldeokmoneyverse.data.model.UiState.Success -> {
                        items(uState.data) { u ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${u.displayName} (${u.email})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("권한: ${u.role} · 상태: ${if (u.isFrozen) "동결(차단됨)" else "정상"}", style = MaterialTheme.typography.labelSmall, color = if (u.isFrozen) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                                    }
                                    OutlinedButton(
                                        onClick = { adminViewModel.freezeUser(u.userId, !u.isFrozen) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (u.isFrozen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text(if (u.isFrozen) "동결 해제" else "계정 동결")
                                    }
                                }
                            }
                        }
                    }
                    else -> Unit
                }

                // 4. 실시간 감사 로그 피드
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📋 실시간 시스템 감사 로그 (Audit Logs)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    val logs = adminUiState.activityLogs
                    if (logs.isEmpty()) {
                        Text("기록된 감사 로그가 없습니다.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        logs.take(10).forEach { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("[${log.action}] ${log.details ?: log.actor}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("${log.createdAt} · ${log.ip ?: "내부 시스템"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("관제탑 닫기")
            }
        }
    }
}

@Composable
fun NotificationInboxDialog(
    accountViewModel: com.example.woldeokmoneyverse.ui.viewmodel.AccountViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val notificationsState by accountViewModel.notificationsState.collectAsState()
    val prefsState by accountViewModel.notificationPreferencesState.collectAsState()
    val busy by accountViewModel.busy.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📬 알림 거버넌스 인박스") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("거래") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("보안") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("설정") })
                }
                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTab) {
                    0, 1 -> {
                        val targetType = if (selectedTab == 0) "TRANSACTIONAL" else "SECURITY_CRITICAL"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selectedTab == 0) "입출금/주식 체결 중요 알림" else "로그인/인증 보안 긴급 알림",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { accountViewModel.markAllNotificationsRead() }, enabled = !busy) {
                                Text("모두 읽음")
                            }
                        }

                        when (val state = notificationsState) {
                            is UiState.Success -> {
                                val filtered = state.data.filter { it.type.equals(targetType, ignoreCase = true) || (targetType == "TRANSACTIONAL" && it.type.isBlank()) }
                                if (filtered.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                        Text("수신된 알림이 없습니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        items(filtered) { noti ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (noti.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(noti.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                        Spacer(modifier = Modifier.weight(1f))
                                                        if (!noti.read) {
                                                            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                                                                Text("NEW", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(noti.body, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(noti.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            is UiState.Loading -> {
                                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is UiState.Error -> {
                                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                    Text("알림 로드 실패: ${state.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            else -> Unit
                        }
                    }
                    2 -> {
                        var marketing by remember { mutableStateOf(false) }
                        var activity by remember { mutableStateOf(true) }
                        var quest by remember { mutableStateOf(true) }
                        var maintenance by remember { mutableStateOf(true) }

                        LaunchedEffect(prefsState) {
                            if (prefsState is UiState.Success) {
                                val p = (prefsState as UiState.Success<NotificationPreferencesDto>).data
                                marketing = p.marketing
                                activity = p.activity
                                quest = p.quest
                                maintenance = p.maintenance
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Text("알림 수신 동의 설정", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("마케팅 및 이벤트 알림", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("각종 프로모션 및 혜택 안내", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = marketing, onCheckedChange = { marketing = it })
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("활동 및 뱅킹 알림", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("입출금, 이체, 거래 체결 알림", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = activity, onCheckedChange = { activity = it })
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("퀘스트 & 챌린지 알림", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("일일 퀘스트 보상 및 칭호 획득", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = quest, onCheckedChange = { quest = it })
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("시스템 점검 & 긴급 공지", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("서버 점검 및 긴급 보안 패치", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = maintenance, onCheckedChange = { maintenance = it })
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { accountViewModel.updateNotificationPreferences(marketing, activity, quest, maintenance) },
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("수신 설정 저장")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } }
    )
}

@Composable
fun SafetyCenterDialog(
    accountViewModel: com.example.woldeokmoneyverse.ui.viewmodel.AccountViewModel,
    onDismiss: () -> Unit
) {
    var selectedSafetyTab by remember { mutableIntStateOf(0) }
    val safetyState by accountViewModel.safetyState.collectAsState()
    val takedownStatusState by accountViewModel.takedownStatusState.collectAsState()
    val busy by accountViewModel.busy.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🚸 미성년자 안전 & 긴급 삭제 센터") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                TabRow(selectedTabIndex = selectedSafetyTab) {
                    Tab(selected = selectedSafetyTab == 0, onClick = { selectedSafetyTab = 0 }, text = { Text("긴급 삭제") })
                    Tab(selected = selectedSafetyTab == 1, onClick = { selectedSafetyTab = 1 }, text = { Text("접수 조회") })
                    Tab(selected = selectedSafetyTab == 2, onClick = { selectedSafetyTab = 2 }, text = { Text("안전 상태") })
                }
                Spacer(modifier = Modifier.height(10.dp))

                when (selectedSafetyTab) {
                    0 -> {
                        var targetUrl by remember { mutableStateOf("") }
                        var reason by remember { mutableStateOf("") }
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }
                        var createdTrackingId by remember { mutableStateOf<String?>(null) }

                        if (createdTrackingId != null) {
                            Column(modifier = Modifier.fillMaxWidth().weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("🎉", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("긴급 삭제 신청이 접수되었습니다!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("접수 번호 (Tracking ID):", style = MaterialTheme.typography.labelSmall)
                                Text(createdTrackingId.orEmpty(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("상태 조회 탭에서 접수 번호와 비밀번호로 처리 현황을 확인할 수 있습니다.", style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { createdTrackingId = null }) { Text("추가 접수하기") }
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                item {
                                    Text("디지털 잊힐 권리 긴급 삭제(Takedown)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("개인정보, 본인 작성 데이터 또는 미성년자 유해 게시물에 대한 즉각 삭제를 접수합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = targetUrl,
                                        onValueChange = { targetUrl = it },
                                        label = { Text("대상 URL 또는 식별자") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = reason,
                                        onValueChange = { reason = it },
                                        label = { Text("삭제 사유 (개인정보 노출 등)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("신청자 연락처 이메일") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("조회용 비밀번호") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            if (targetUrl.isNotBlank() && reason.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                                accountViewModel.submitTakedown(targetUrl, reason, email, password) { trackingId ->
                                                    createdTrackingId = trackingId
                                                }
                                            }
                                        },
                                        enabled = !busy && targetUrl.isNotBlank() && reason.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("긴급 삭제 신청서 제출")
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        var trackingIdInput by remember { mutableStateOf("") }
                        var passInput by remember { mutableStateOf("") }

                        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Text("긴급 삭제 처리 현황 조회", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = trackingIdInput,
                                onValueChange = { trackingIdInput = it },
                                label = { Text("접수 번호 (Tracking ID)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = passInput,
                                onValueChange = { passInput = it },
                                label = { Text("신청 비밀번호") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { accountViewModel.checkTakedownStatus(trackingIdInput.trim(), passInput.trim()) },
                                enabled = !busy && trackingIdInput.isNotBlank() && passInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("상태 조회")
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            when (val st = takedownStatusState) {
                                is UiState.Success -> {
                                    val data = st.data
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("처리 상태: ${data.status}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                            Text("접수 일시: ${data.createdAt}", style = MaterialTheme.typography.labelSmall)
                                            data.resolvedAt?.let { Text("완료 일시: $it", style = MaterialTheme.typography.labelSmall) }
                                            data.reviewNotes?.let { Text("검토 메모: $it", style = MaterialTheme.typography.bodySmall) }
                                        }
                                    }
                                }
                                is UiState.Error -> {
                                    Text("조회 실패: ${st.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                                else -> Unit
                            }
                        }
                    }
                    2 -> {
                        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            when (val s = safetyState) {
                                is UiState.Success -> {
                                    val safe = s.data
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("🛡️ 아동 및 청소년 안전 보호 상태", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("• 미성년자 여부: ${if (safe.isMinor) "만 14세 미만 미성년자" else "일반 성인 회원"}", style = MaterialTheme.typography.bodySmall)
                                            Text("• 보호자(법정대리인) 동의: ${if (safe.guardianConsent) "완료" else "해당 없음 또는 미완료"}", style = MaterialTheme.typography.bodySmall)
                                            Text("• 보호자 이메일: ${safe.guardianEmail ?: "미등록"}", style = MaterialTheme.typography.bodySmall)
                                            Text("• 24시간 긴급 조치 채널: 정상 가동 중", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                                is UiState.Loading -> CircularProgressIndicator()
                                is UiState.Error -> Text("안전 상태 조회 실패: ${s.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                else -> Unit
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } }
    )
}


