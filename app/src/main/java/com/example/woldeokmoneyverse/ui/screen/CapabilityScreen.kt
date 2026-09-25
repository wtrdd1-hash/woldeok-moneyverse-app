package com.example.woldeokmoneyverse.ui.screen

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woldeokmoneyverse.data.remote.AppCapability
import com.example.woldeokmoneyverse.data.remote.AppCapabilityCatalog
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import com.example.woldeokmoneyverse.ui.viewmodel.CapabilityViewModel

@Composable
fun CapabilityScreen(
    adminOnly: Boolean = false,
    capabilityViewModel: CapabilityViewModel = viewModel()
) {
    val state by capabilityViewModel.state.collectAsState()
    var search by remember { mutableStateOf("") }
    val source = if (adminOnly) AppCapabilityCatalog.admin else AppCapabilityCatalog.member
    val capabilities = remember(search, adminOnly) {
        source.filter {
            search.isBlank() ||
                it.title.contains(search, ignoreCase = true) ||
                groupLabel(it.group).contains(search, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                if (adminOnly) "관리자 전체 기능" else "전체 기능",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                if (adminOnly) "서버가 허용한 관리자 기능을 역할 검증 후 실행합니다."
                else "계정·경제·게임·커뮤니티 등 서버 기능을 한곳에서 이용할 수 있습니다.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = search,
                onValueChange = { search = it.take(80) },
                label = { Text("기능 검색") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(capabilities, key = { it.id }) { capability ->
            CapabilityCard(
                capability,
                capabilityViewModel,
                state.capabilityId,
                state.loading,
                state.result,
                state.binaryBytes,
                state.error
            )
        }
    }
}

@Composable
fun CapabilityCard(
    capability: AppCapability,
    capabilityViewModel: CapabilityViewModel,
    activeId: String?,
    loading: Boolean,
    result: String?,
    binaryBytes: ByteArray?,
    error: String?
) {
    val context = LocalContext.current
    var expanded by remember(capability.id) { mutableStateOf(false) }
    var confirm by remember(capability.id) { mutableStateOf(false) }
    val values = remember(capability.id) { mutableStateMapOf<String, String>() }
    val active = activeId == capability.id
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val buffer = ByteArray(4 * 1024 * 1024 + 1)
                    var total = 0
                    while (total < buffer.size) {
                        val read = input.read(buffer, total, buffer.size - total)
                        if (read <= 0) break
                        total += read
                    }
                    buffer.copyOf(total)
                }
            }.getOrNull()
            if (bytes != null) capabilityViewModel.uploadRawBytes(capability, values.toMap(), bytes)
        }
    }

    MoneyverseCard(onClick = { expanded = !expanded }) {
        Text(capability.title, fontWeight = FontWeight.Bold)
        Text(groupLabel(capability.group), style = MaterialTheme.typography.labelSmall)
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            capability.fields.forEach { field ->
                OutlinedTextField(
                    value = values[field.name].orEmpty(),
                    onValueChange = { values[field.name] = it.take(8000) },
                    label = { Text(fieldLabel(field.name) + if (field.required) " *" else "") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (field.kind in setOf("object", "array", "json")) 2 else 1
                )
                Spacer(Modifier.height(6.dp))
            }
            Button(
                onClick = {
                    when {
                        capability.rawByteUpload -> imagePicker.launch("image/*")
                        capability.binaryResponse -> capabilityViewModel.loadBinary(capability, values.toMap())
                        capability.destructive -> confirm = true
                        else -> capabilityViewModel.execute(capability, values.toMap())
                    }
                },
                enabled = !(active && loading),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        active && loading -> "처리 중…"
                        capability.rawByteUpload -> "이미지 선택 및 업로드"
                        capability.binaryResponse -> "미디어 불러오기"
                        capability.method == "GET" -> "조회"
                        else -> "실행"
                    }
                )
            }
            if (active && !result.isNullOrBlank()) {
                Text(result, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
            if (active && binaryBytes != null) {
                val bitmap = remember(binaryBytes) {
                    BitmapFactory.decodeByteArray(binaryBytes, 0, binaryBytes.size)
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "불러온 미디어",
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).padding(top = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("미디어 형식을 표시할 수 없습니다.", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (active && !error.isNullOrBlank()) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("작업 확인") },
            text = { Text("‘${capability.title}’ 작업을 실행할까요? 되돌리기 어려울 수 있습니다.") },
            confirmButton = {
                Button(onClick = {
                    confirm = false
                    capabilityViewModel.execute(capability, values.toMap())
                }) { Text("실행") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("취소") } }
        )
    }
}

private fun groupLabel(group: String): String = when (group) {
    "account" -> "계정"
    "auth" -> "로그인·보안"
    "bank", "banking" -> "은행"
    "board" -> "게시판"
    "businesses" -> "사업"
    "casino" -> "카지노"
    "crafting" -> "제작"
    "early-game" -> "초기 성장"
    "engagement" -> "활동"
    "marketplace" -> "거래소"
    "media", "photos", "content" -> "콘텐츠"
    "newspaper" -> "신문"
    "notifications" -> "알림"
    "privacy" -> "개인정보"
    "profile" -> "프로필"
    "progression", "rewards" -> "성장·보상"
    "seasons" -> "시즌"
    "shop" -> "상점"
    "stocks" -> "주식"
    "support" -> "고객지원"
    "wallet" -> "지갑"
    "work" -> "직업"
    "admin" -> "관리자"
    else -> "서비스"
}

private fun fieldLabel(name: String): String = when (name) {
    "id" -> "대상 ID"
    "userId" -> "사용자 ID"
    "provider" -> "로그인 제공자"
    "amount" -> "금액"
    "quantity" -> "수량"
    "status" -> "상태"
    "body", "message" -> "내용"
    "request" -> "요청 데이터(JSON)"
    else -> name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
}
