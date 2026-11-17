package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woldeokmoneyverse.data.model.ClubStrongholdDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.FintechBottomSheetContainer
import com.example.woldeokmoneyverse.ui.component.FintechHapticController
import com.example.woldeokmoneyverse.ui.component.FloatingDamageText
import com.example.woldeokmoneyverse.ui.component.WoldeokWorldMapDialog
import com.example.woldeokmoneyverse.ui.viewmodel.CommunityViewModel
import com.example.woldeokmoneyverse.util.formatWld
import kotlinx.coroutines.delay

@Composable
fun ClubsWarfareSubTab(
    viewModel: CommunityViewModel,
    haptic: FintechHapticController
) {
    val strongholdsState by viewModel.strongholdsState.collectAsState()
    var selectedStrongholdForAttack by remember { mutableStateOf<ClubStrongholdDto?>(null) }
    var showWorldMap by remember { mutableStateOf(false) }
    var damageText by remember { mutableStateOf<String?>(null) }
    var showDamageAnim by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadStrongholds()
    }

    LaunchedEffect(showDamageAnim) {
        if (showDamageAnim) {
            delay(1200)
            showDamageAnim = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 헤더 배너 & 대륙 지도 열기 버튼
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Castle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "클럽 영지 공성전 허브",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "금융 특별 구역의 핵심 거점을 점령하고 일일 세수를 징수하세요.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                haptic.click()
                                viewModel.loadStrongholds()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 대륙 지도 열기 버튼
                        Button(
                            onClick = {
                                haptic.click()
                                showWorldMap = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🗺️ 월덕 대륙 인터랙티브 영지 맵 열기", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                    }
                }
            }

            // 2. 요새 목록
            when (val state = strongholdsState) {
                is UiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(
                            text = "영지 정보 조회 실패: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                is UiState.Success -> {
                    val list: List<ClubStrongholdDto> = state.data
                    if (list.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("등록된 거점 요새가 없습니다.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        items(list) { stronghold: ClubStrongholdDto ->
                            StrongholdBentoCard(
                                stronghold = stronghold,
                                onAttackClick = {
                                    haptic.click()
                                    selectedStrongholdForAttack = stronghold
                                }
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        // 플로팅 대미지 텍스트 연출 (화면 중앙 상단에 팝업)
        damageText?.let { dmg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                FloatingDamageText(
                    damageText = dmg,
                    isVisible = showDamageAnim
                )
            }
        }
    }

    // 공성전 타격 모달 바텀시트
    selectedStrongholdForAttack?.let { sh ->
        StrongholdAttackSheet(
            stronghold = sh,
            onDismiss = { selectedStrongholdForAttack = null },
            onConfirmAttack = { troopCount, tactic ->
                haptic.heavy()
                val damage = troopCount * 2500L
                damageText = "-${formatWld(damage)} HP CRITICAL!"
                showDamageAnim = true
                viewModel.attackStronghold(sh.id, troopCount, tactic)
                selectedStrongholdForAttack = null
            }
        )
    }

    // 월덕 대륙 맵 다이얼로그
    if (showWorldMap) {
        WoldeokWorldMapDialog(onDismiss = { showWorldMap = false })
    }
}

@Composable
fun StrongholdBentoCard(
    stronghold: ClubStrongholdDto,
    onAttackClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (stronghold.isUnderAttack) Color(0xFFEF4444).copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stronghold.district,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stronghold.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "점령 길드: ${stronghold.currentOwnerClubName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (stronghold.isUnderAttack) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚔️ 공성전 진행 중",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 요새 방어 내구도 HP 게이지 바
            val hpFraction = (stronghold.defenseHp.toFloat() / stronghold.maxDefenseHp.toFloat()).coerceIn(0f, 1f)
            val hpColor = when {
                hpFraction > 0.5f -> Color(0xFF10B981)
                hpFraction > 0.2f -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "요새 내구도 (HP)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatWld(stronghold.defenseHp)} / ${formatWld(stronghold.maxDefenseHp)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { hpFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = hpColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 벤토 정보 (일일 세수 풀 & 남은 공성 시간)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("일일 누적 세수 풀", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${formatWld(stronghold.dailyTaxPoolWld)} WLD",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("공성 잔여 시간", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${stronghold.endMinutesRemaining}분 남음",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAttackClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("공성전 타격 작전 개시", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StrongholdAttackSheet(
    stronghold: ClubStrongholdDto,
    onDismiss: () -> Unit,
    onConfirmAttack: (troopCount: Int, tactic: String) -> Unit
) {
    var troopCount by remember { mutableFloatStateOf(20f) }
    var selectedTactic by remember { mutableStateOf("CHARGE") }

    FintechBottomSheetContainer(
        visible = true,
        onDismiss = onDismiss,
        title = "⚔️ ${stronghold.name} 공성전 타격"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "현재 점령 길드 '${stronghold.currentOwnerClubName}'의 방어벽을 타격합니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 파병 병력 수 슬라이더
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("투입 병력", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${troopCount.toInt()} 사단",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = troopCount,
                    onValueChange = { troopCount = it },
                    valueRange = 5f..100f,
                    steps = 18
                )
            }

            // 공격 전술 선택
            Column {
                Text("공격 전술 선택", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tactics = listOf(
                        "CHARGE" to "⚡ 정면 돌파",
                        "INFILTRATE" to "🎯 야간 침투",
                        "ARTILLERY" to "💥 중포 포격"
                    )
                    tactics.forEach { (tKey, tLabel) ->
                        FilterChip(
                            selected = selectedTactic == tKey,
                            onClick = { selectedTactic = tKey },
                            label = { Text(tLabel, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // 예상 피해량 및 보상 미리보기
            val expectedDamage = troopCount.toInt() * 2500L
            val expectedReward = troopCount.toInt() * 120L
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "예상 피해: ${formatWld(expectedDamage)} HP",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "예상 전리품: +${formatWld(expectedReward)} WLD",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            Button(
                onClick = { onConfirmAttack(troopCount.toInt(), selectedTactic) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("타격 명령 하달 (${troopCount.toInt()} 사단 투입)", fontWeight = FontWeight.Bold)
            }
        }
    }
}
