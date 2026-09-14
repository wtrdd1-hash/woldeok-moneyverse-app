package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.model.CasinoDiceRequest
import com.example.woldeokmoneyverse.data.model.CasinoPlayRequest
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.MoneyverseButton
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import com.example.woldeokmoneyverse.ui.viewmodel.PlayViewModel

@Composable
fun CasinoScreen(
    playViewModel: PlayViewModel
) {
    var coinChoice by remember { mutableStateOf("heads") }
    var coinStake by remember { mutableFloatStateOf(50f) }

    var diceGameType by remember { mutableStateOf("dice_parity") }
    var diceChoice by remember { mutableStateOf("odd") }
    var diceStake by remember { mutableFloatStateOf(50f) }
    var themeStake by remember { mutableFloatStateOf(50f) }

    val playMessage by playViewModel.playMessage.collectAsState()
    val casinoLimitsState by playViewModel.casinoLimitsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(playMessage) {
        playMessage?.let {
            snackbarHostState.showSnackbar(it)
            playViewModel.clearPlayMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "🎰 가상 카지노",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "WLD 전용 · 서버가 결과/한도/원장을 결정하며 현금화 기능은 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (val limits = casinoLimitsState) {
                    is UiState.Success -> MoneyverseCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("보호 한도", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("일 배팅 한도 ${limits.data.dailyBetLimit} WLD · 일 손실 한도 ${limits.data.dailyLossLimit} WLD", style = MaterialTheme.typography.bodySmall)
                        limits.data.lockedUntil?.let { Text("이용 제한: $it", style = MaterialTheme.typography.labelSmall) }
                    }
                    is UiState.Error -> Text("카지노 한도를 불러오지 못했습니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    else -> Unit
                }
                Spacer(modifier = Modifier.height(12.dp))

                MoneyverseCard {
                    Text("🪙 동전 던지기", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("공개 기준: 50% 확률 · 서버 권한형 결과", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = coinChoice == "heads",
                            onClick = { coinChoice = "heads" },
                            label = { Text("앞면") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = coinChoice == "tails",
                            onClick = { coinChoice = "tails" },
                            label = { Text("뒷면") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("배팅 금액: ${coinStake.toLong()} WLD", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = coinStake,
                        onValueChange = { coinStake = it },
                        valueRange = 10f..200f
                    )
                    Text("허용 범위 10~200 WLD", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    MoneyverseButton(
                        text = "동전 던지기",
                        onClick = { playViewModel.playCoinFlip(CasinoPlayRequest(coinChoice, coinStake.toLong())) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                MoneyverseCard {
                    Text("🎲 주사위", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = diceGameType == "dice_parity",
                            onClick = {
                                diceGameType = "dice_parity"
                                diceChoice = "odd"
                            },
                            label = { Text("홀/짝") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = diceGameType == "dice_number",
                            onClick = {
                                diceGameType = "dice_number"
                                diceChoice = "1"
                            },
                            label = { Text("숫자 1~6") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (diceGameType == "dice_parity") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = diceChoice == "odd",
                                onClick = { diceChoice = "odd" },
                                label = { Text("홀수") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = diceChoice == "even",
                                onClick = { diceChoice = "even" },
                                label = { Text("짝수") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..6).forEach { num ->
                                FilterChip(
                                    selected = diceChoice == num.toString(),
                                    onClick = { diceChoice = num.toString() },
                                    label = { Text("$num") }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("배팅 금액: ${diceStake.toLong()} WLD", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = diceStake,
                        onValueChange = { diceStake = it },
                        valueRange = 10f..200f
                    )
                    Text("허용 범위 10~200 WLD", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    MoneyverseButton(
                        text = "주사위 굴리기",
                        onClick = { playViewModel.playDice(CasinoDiceRequest(diceGameType, diceChoice, diceStake.toLong())) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                MoneyverseCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("🎡 테마 카지노", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "휠·보물·젬·슬롯 테마는 검증된 서버 코인/주사위 확률 엔진을 사용합니다. 화면 연출만 다르고 결과와 WLD 정산은 서버 영수증이 기준입니다.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("배팅 금액: ${themeStake.toLong()} WLD", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = themeStake,
                        onValueChange = { themeStake = it },
                        valueRange = 10f..200f
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("🎡 컬러 휠", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { playViewModel.playCoinFlip(CasinoPlayRequest("heads", themeStake.toLong())) },
                            modifier = Modifier.weight(1f)
                        ) { Text("빨강") }
                        OutlinedButton(
                            onClick = { playViewModel.playCoinFlip(CasinoPlayRequest("tails", themeStake.toLong())) },
                            modifier = Modifier.weight(1f)
                        ) { Text("파랑") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("🗝 보물상자", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { playViewModel.playCoinFlip(CasinoPlayRequest("heads", themeStake.toLong())) },
                            modifier = Modifier.weight(1f)
                        ) { Text("왼쪽 상자") }
                        OutlinedButton(
                            onClick = { playViewModel.playCoinFlip(CasinoPlayRequest("tails", themeStake.toLong())) },
                            modifier = Modifier.weight(1f)
                        ) { Text("오른쪽 상자") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("💎 젬 선택", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { playViewModel.playCoinFlip(CasinoPlayRequest("heads", themeStake.toLong())) },
                            modifier = Modifier.weight(1f)
                        ) { Text("태양 젬") }
                        OutlinedButton(
                            onClick = { playViewModel.playCoinFlip(CasinoPlayRequest("tails", themeStake.toLong())) },
                            modifier = Modifier.weight(1f)
                        ) { Text("달 젬") }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("🎰 홀짝 슬롯", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { playViewModel.playDice(CasinoDiceRequest("dice_parity", "odd", themeStake.toLong())) },
                            modifier = Modifier.weight(1f)
                        ) { Text("홀수 슬롯") }
                        OutlinedButton(
                            onClick = { playViewModel.playDice(CasinoDiceRequest("dice_parity", "even", themeStake.toLong())) },
                            modifier = Modifier.weight(1f)
                        ) { Text("짝수 슬롯") }
                    }
                }
            }
        }
    }
}
