package com.example.woldeokmoneyverse.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.remote.AppCapabilityContract
import com.example.woldeokmoneyverse.data.remote.AppCapabilityEndpoint
import com.example.woldeokmoneyverse.data.remote.AppCapabilityExecutor
import com.example.woldeokmoneyverse.data.remote.AppCapabilityLoader
import com.example.woldeokmoneyverse.data.remote.CapabilityExecution
import com.example.woldeokmoneyverse.data.remote.capabilityGroupTitle
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import kotlinx.coroutines.launch

@Composable
fun FeatureCenterScreen(adminRoles: List<String>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var contract by remember { mutableStateOf<AppCapabilityContract?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var selectedEndpoint by remember { mutableStateOf<AppCapabilityEndpoint?>(null) }
    var confirmEndpoint by remember { mutableStateOf<AppCapabilityEndpoint?>(null) }
    var confirmValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var runningKey by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<CapabilityExecution?>(null) }
    var executionError by remember { mutableStateOf<String?>(null) }
    var pendingBinaryEndpoint by remember { mutableStateOf<AppCapabilityEndpoint?>(null) }
    val allowAdmin = adminRoles.isNotEmpty()
    val binaryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val endpoint = pendingBinaryEndpoint
        pendingBinaryEndpoint = null
        if (uri != null && endpoint != null) {
            val key = endpoint.operationId ?: endpoint.path
            runningKey = key
            executionError = null
            scope.launch {
                val bytes = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("이미지 파일을 읽지 못했습니다.")
                }.getOrElse {
                    executionError = "이미지 파일을 읽지 못했습니다."
                    runningKey = null
                    return@launch
                }
                val mediaType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                AppCapabilityExecutor.executeRaw(endpoint, bytes, mediaType, allowAdmin).fold(
                    onSuccess = { result = it },
                    onFailure = { executionError = it.message ?: "이미지 업로드에 실패했습니다." }
                )
                runningKey = null
            }
        }
    }

    LaunchedEffect(Unit) {
        AppCapabilityLoader.load(context).fold(
            onSuccess = { contract = it },
            onFailure = { loadError = it.message ?: "앱 기능 계약을 읽지 못했습니다." }
        )
    }

    fun execute(endpoint: AppCapabilityEndpoint, values: Map<String, String>) {
        val key = endpoint.operationId ?: endpoint.path
        runningKey = key
        executionError = null
        scope.launch {
            AppCapabilityExecutor.execute(endpoint, values, allowAdmin).fold(
                onSuccess = { result = it },
                onFailure = { executionError = it.message ?: "기능 실행에 실패했습니다." }
            )
            runningKey = null
        }
    }

    val available = contract?.endpoints.orEmpty().filter { allowAdmin || !it.isAdmin }
    val groups = available.map { it.group }.distinct().sortedBy(::capabilityGroupTitle)
    val filtered = available.filter { endpoint ->
        val groupMatches = selectedGroup == null || endpoint.group == selectedGroup
        val textMatches = query.isBlank() ||
            endpoint.title.contains(query, ignoreCase = true) ||
            endpoint.callWhen.orEmpty().contains(query, ignoreCase = true)
        groupMatches && textMatches
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text(
                "전체 기능",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "서버 기능 계약을 기준으로 앱 기능을 한곳에 연결합니다. 내부 서버 주소와 인증 정보는 화면에 표시하지 않습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (contract != null) {
                Text(
                    "사용 가능 ${available.size}개 · 계약 ${contract!!.endpoints.size}개" +
                        if (allowAdmin) " · 관리자 기능 포함" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(80) },
                label = { Text("기능 검색") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedGroup == null,
                        onClick = { selectedGroup = null },
                        label = { Text("전체") }
                    )
                }
                items(groups) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(capabilityGroupTitle(group)) }
                    )
                }
            }
        }

        if (contract == null && loadError == null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        loadError?.let { message ->
            item {
                MoneyverseCard {
                    Text("기능 계약 확인 실패", fontWeight = FontWeight.Bold)
                    Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        items(filtered, key = { it.operationId ?: it.path + it.method }) { endpoint ->
            val key = endpoint.operationId ?: endpoint.path
            MoneyverseCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(endpoint.title, fontWeight = FontWeight.Bold)
                        endpoint.callWhen?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                        endpoint.authorization?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (endpoint.isAdmin) {
                        AssistChip(onClick = {}, label = { Text("관리자") })
                    }
                }
                Spacer(Modifier.height(8.dp))
                val busy = runningKey == key
                when {
                    endpoint.isBinaryUpload -> {
                        Button(
                            onClick = {
                                pendingBinaryEndpoint = endpoint
                                binaryPicker.launch("image/*")
                            },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) "업로드 중…" else "이미지 선택 후 업로드") }
                    }
                    endpoint.requiresDedicatedUi -> {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("전용 화면에서 제공") }
                    }
                    endpoint.inputFields().isNotEmpty() -> {
                        Button(
                            onClick = { selectedEndpoint = endpoint },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) "처리 중…" else if (endpoint.isMutation) "입력 후 실행" else "조건 입력 후 조회") }
                    }
                    endpoint.isMutation -> {
                        Button(
                            onClick = {
                                confirmEndpoint = endpoint
                                confirmValues = emptyMap()
                            },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) "처리 중…" else "확인 후 실행") }
                    }
                    else -> {
                        Button(
                            onClick = { execute(endpoint, emptyMap()) },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) "조회 중…" else "조회") }
                    }
                }
            }
        }

        if (filtered.isEmpty() && contract != null) {
            item { MoneyverseCard { Text("조건에 맞는 기능이 없습니다.") } }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    selectedEndpoint?.let { endpoint ->
        CapabilityInputDialog(
            endpoint = endpoint,
            onDismiss = { selectedEndpoint = null },
            onSubmit = { values ->
                selectedEndpoint = null
                if (endpoint.isMutation) {
                    confirmEndpoint = endpoint
                    confirmValues = values
                } else {
                    execute(endpoint, values)
                }
            }
        )
    }

    confirmEndpoint?.let { endpoint ->
        AlertDialog(
            onDismissRequest = { confirmEndpoint = null },
            title = { Text(if (endpoint.isAdmin) "관리자 작업 확인" else "변경 작업 확인") },
            text = {
                Text(
                    if (endpoint.isAdmin)
                        "관리자 권한으로 서버 상태를 변경합니다. 대상과 입력값을 확인한 뒤 실행하세요."
                    else
                        "서버 상태를 변경하는 기능입니다. 입력 내용을 확인한 뒤 실행하세요."
                )
            },
            confirmButton = {
                Button(onClick = {
                    val values = confirmValues
                    confirmEndpoint = null
                    confirmValues = emptyMap()
                    execute(endpoint, values)
                }) { Text("실행") }
            },
            dismissButton = {
                TextButton(onClick = { confirmEndpoint = null }) { Text("취소") }
            }
        )
    }

    result?.let { execution ->
        AlertDialog(
            onDismissRequest = { result = null },
            title = { Text("처리 완료") },
            text = {
                Column {
                    Text("서버 응답 ${execution.status}", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    SelectionContainer {
                        Text(execution.preview, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { result = null }) { Text("확인") } }
        )
    }

    executionError?.let { message ->
        AlertDialog(
            onDismissRequest = { executionError = null },
            title = { Text("처리 실패") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { executionError = null }) { Text("확인") } }
        )
    }
}

@Composable
private fun CapabilityInputDialog(
    endpoint: AppCapabilityEndpoint,
    onDismiss: () -> Unit,
    onSubmit: (Map<String, String>) -> Unit
) {
    val fields = remember(endpoint.operationId, endpoint.path) { endpoint.inputFields() }
    val values = remember(endpoint.operationId, endpoint.path) {
        mutableStateMapOf<String, String>().apply {
            fields.forEach { field ->
                if (field.defaultValue.isNotBlank()) {
                    this[field.source + ":" + field.name] = field.defaultValue
                }
            }
        }
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(endpoint.title) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fields, key = { it.source + ":" + it.name }) { field ->
                    val key = field.source + ":" + field.name
                    OutlinedTextField(
                        value = values[key].orEmpty(),
                        onValueChange = { values[key] = it.take(4000) },
                        label = { Text(field.label + if (field.required) " *" else "") },
                        supportingText = {
                            val hint = when {
                                field.options.isNotEmpty() -> "가능 값: ${field.options.joinToString(", ")}"
                                field.type == "boolean" -> "true 또는 false"
                                field.type in setOf("array", "object", "json") -> "JSON 형식으로 입력"
                                else -> ""
                            }
                            if (hint.isNotBlank()) Text(hint)
                        },
                        minLines = if (field.type in setOf("array", "object", "json")) 3 else 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                validationError?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            }
        },
        confirmButton = {
            Button(onClick = {
                val missing = fields.firstOrNull { field ->
                    field.required && values[field.source + ":" + field.name].orEmpty().isBlank()
                }
                if (missing != null) {
                    validationError = "${missing.label} 항목을 입력해 주세요."
                } else {
                    onSubmit(values.toMap())
                }
            }) { Text(if (endpoint.isMutation) "다음" else "조회") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
