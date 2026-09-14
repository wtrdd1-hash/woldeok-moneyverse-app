package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.PlayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    playViewModel: PlayViewModel
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("루프 & 근무", "🎰 카지노", "🏆 시즌 리더보드")

    Column(modifier = Modifier.fillMaxSize()) {
        MoneyverseSubTabRow(
            tabs = subTabs,
            selectedTabIndex = selectedSubTab,
            onTabSelected = { selectedSubTab = it }
        )

        when (selectedSubTab) {
            0 -> PlayMainLoopSubTab(playViewModel)
            1 -> CasinoScreen(playViewModel)
            2 -> SeasonsScreen(playViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayMainLoopSubTab(
    playViewModel: PlayViewModel
) {
    val workState by playViewModel.workState.collectAsState()
    val progressionState by playViewModel.progressionState.collectAsState()
    val tasksState by playViewModel.tasksState.collectAsState()
    val playMessage by playViewModel.playMessage.collectAsState()

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
                    text = "🎮 플레이 & 플레이 루프",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Daily Claim Reward Card ---
                MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("🎁 출석 일일 보상 (🔥 7일 연속 출석)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("매일 출석하고 무료 WLD 보상을 받아가세요!", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    MoneyverseButton(
                        text = "일일 출석 보상 받기",
                        onClick = { playViewModel.claimDailyReward() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("💼 직업 & 근무 작업", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                when (val wState = workState) {
                    is UiState.Success -> {
                        val work = wState.data
                        MoneyverseCard {
                            Text("현재 직업: ${work.jobTitle}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("예상 근무 보상: ${work.estimatedReward}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            // The legacy /work/execute route is not in the native API
                            // contract.  Keep this screen read-only until the task /
                            // assignment flow is rendered with its documented endpoints.
                            Text(
                                text = if (work.canWork) "근무 과제는 작업 목록에서 진행할 수 있습니다." else "쿨다운 진행 중",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is UiState.Loading -> SkeletonLoader()
                    else -> {}
                }
            }

            // --- Progression Level XP ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("⭐ 성취 & 레벨 진행도", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))

                when (val pState = progressionState) {
                    is UiState.Success -> {
                        val p = pState.data
                        MoneyverseCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                            Text("레벨 ${p.level} - ${p.title}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { p.currentExp.toFloat() / p.requiredExp.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val unlockedFeatures = p.unlockedFeatures.orEmpty()
                            Text(
                                "해금된 혜택: ${unlockedFeatures.joinToString(", ").ifBlank { "아직 해금된 혜택이 없습니다" }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    else -> {}
                }
            }

            // --- Early Game Onboarding Missions ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("🚀 초반 진행 미션", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (val tState = tasksState) {
                is UiState.Success -> {
                    items(tState.data) { task ->
                        MoneyverseCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = if (task.isCompleted) "✓ 완료" else "+${task.rewardAmount} WLD",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                is UiState.Loading -> item { SkeletonLoader() }
                else -> {}
            }
        }
    }
}
