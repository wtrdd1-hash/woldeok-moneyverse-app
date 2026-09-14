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
    var coinStake by remember { mutableFloatStateOf(1000000f) }

    var diceGameType by remember { mutableStateOf("dice_parity") } // "dice_parity" or "dice_number"
    var diceChoice by remember { mutableStateOf("odd") }
    var diceStake by remember { mutableFloatStateOf(1000000f) }

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
                    text = "🎰 조건부 가상 카지노 (`CAS-001`)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "서버 권한형 확률 및 한도가 적용되는 미니게임",
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

                // --- Coin Flip Game ---
                MoneyverseCard {
                    Text("🪙 동전 던지기 (Coin Flip)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = coinChoice == "heads",
                            onClick = { coinChoice = "heads" },
                            label = { Text("앞면 (Heads)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = coinChoice == "tails",
                            onClick = { coinChoice = "tails" },
                            label = { Text("뒷면 (Tails)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("배팅 금액: ${coinStake.toLong()} WLD", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = coinStake,
                        onValueChange = { coinStake = it },
                        valueRange = 100000f..5000000f,
                        steps = 9
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MoneyverseButton(
                        text = "동전 던지기 실행",
                        onClick = {
                            playViewModel.playCoinFlip(CasinoPlayRequest(coinChoice, coinStake.toLong()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Dice Game ---
                MoneyverseCard {
                    Text("🎲 주사위 미니게임 (Dice)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = diceGameType == "dice_parity",
                            onClick = {
                                diceGameType = "dice_parity"
                                diceChoice = "odd"
                            },
                            label = { Text("홀/짝 맞추기") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = diceGameType == "dice_number",
                            onClick = {
                                diceGameType = "dice_number"
                                diceChoice = "1"
                            },
                            label = { Text("숫자 맞추기 (1~6)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (diceGameType == "dice_parity") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = diceChoice == "odd",
                                onClick = { diceChoice = "odd" },
                                label = { Text("홀수 (Odd)") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = diceChoice == "even",
                                onClick = { diceChoice = "even" },
                                label = { Text("짝수 (Even)") },
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
                        valueRange = 100000f..5000000f,
                        steps = 9
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MoneyverseButton(
                        text = "주사위 굴리기",
                        onClick = {
                            playViewModel.playDice(CasinoDiceRequest(diceGameType, diceChoice, diceStake.toLong()))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
