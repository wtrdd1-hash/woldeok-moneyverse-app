package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woldeokmoneyverse.data.remote.ApiFeatureRegistry
import com.example.woldeokmoneyverse.ui.viewmodel.AllFeaturesViewModel

@Composable
fun AllFeaturesScreen(viewModel: AllFeaturesViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var mutationApproved by remember(state.endpoint) { mutableStateOf(false) }
    val endpoints = viewModel.endpointsFor(state.group)

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("전체 API 기능", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("모바일 계약 ${ApiFeatureRegistry.CONTRACT_VERSION} · ${ApiFeatureRegistry.endpoints.size} endpoints", style = MaterialTheme.typography.bodySmall)
            Text("일반 화면에 아직 전용 UI가 없는 기능도 여기서 동일한 BFF 계약으로 사용할 수 있습니다.", style = MaterialTheme.typography.bodySmall)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(viewModel.groups) { group ->
                    FilterChip(selected = group == state.group, onClick = { viewModel.selectGroup(group) }, label = { Text(group) })
                }
            }
        }
        item { Text("${state.group} 기능 ${endpoints.size}개", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
        if (endpoints.isEmpty()) {
            item { Text("현재 모바일 BFF 계약에는 이 그룹의 직접 호출 endpoint가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(endpoints) { endpoint ->
            ElevatedCard(onClick = { viewModel.selectEndpoint(endpoint) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${endpoint.method} ${endpoint.path}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(endpoint.purpose, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        state.endpoint?.let { endpoint ->
            item { HorizontalDivider() }
            item { Text("선택 기능 실행", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
            item { Text(endpoint.purpose); Text(endpoint.authorization, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item {
                OutlinedTextField(value = state.requestPath, onValueChange = viewModel::setPath, modifier = Modifier.fillMaxWidth(), label = { Text("API 경로 · :id 등은 실제 값으로 변경") }, minLines = 1)
            }
            if (endpoint.method != "GET") {
                item {
                    OutlinedTextField(value = state.requestBody, onValueChange = viewModel::setBody, modifier = Modifier.fillMaxWidth(), label = { Text("JSON 요청 본문") }, minLines = 4)
                }
                item {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = mutationApproved, onCheckedChange = { mutationApproved = it })
                        Text("이 변경 요청을 실행합니다. 구매·삭제·전송 등은 실제 서버 상태를 변경할 수 있습니다.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Button(onClick = viewModel::execute, enabled = !state.busy && (endpoint.method == "GET" || mutationApproved), modifier = Modifier.fillMaxWidth()) {
                    if (state.busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("${endpoint.method} 실행")
                }
            }
            state.statusCode?.let { code -> item { Text("HTTP $code", fontWeight = FontWeight.Bold) } }
            state.error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
            if (state.responseText.isNotBlank()) { item { OutlinedCard(Modifier.fillMaxWidth()) { Text(state.responseText, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) } } }
        }
    }
}