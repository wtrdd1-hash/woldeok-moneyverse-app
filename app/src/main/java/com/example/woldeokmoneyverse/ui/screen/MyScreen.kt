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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.woldeokmoneyverse.R
import com.example.woldeokmoneyverse.data.local.SessionManager
import com.example.woldeokmoneyverse.ui.theme.ThemePreset
import com.example.woldeokmoneyverse.ui.viewmodel.AuthViewModel
import com.example.woldeokmoneyverse.ui.viewmodel.SettingsViewModel

@Composable
fun MyScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme by settingsViewModel.selectedTheme.collectAsState()
    val customColor by settingsViewModel.customPrimaryColor.collectAsState()

    var showTermsModal by remember { mutableStateOf(false) }
    var showPrivacyModal by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf(SessionManager.getProfileImageUri(context)) }

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
                    Text("• 네트워크 연결: 공식 서비스 보안 경로 사용", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("앱 버전: 1.0.18 (Native Compose)", style = MaterialTheme.typography.bodySmall)
                    Text("네트워크 보안: 공식 서비스 연결만 허용", style = MaterialTheme.typography.bodySmall)
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
}
