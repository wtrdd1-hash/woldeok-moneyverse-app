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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.DailyRewardViewModel
import com.example.woldeokmoneyverse.ui.viewmodel.PlayViewModel
import com.example.woldeokmoneyverse.ui.viewmodel.WorkFeatureViewModel

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
    playViewModel: PlayViewModel,
    workFeatureViewModel: WorkFeatureViewModel = viewModel(),
    dailyRewardViewModel: DailyRewardViewModel = viewModel()
) {
    val workState by playViewModel.workState.collectAsState()
    val progressionState by playViewModel.progressionState.collectAsState()
    val tasksState by playViewModel.tasksState.collectAsState()
    val playMessage by playViewModel.playMessage.collectAsState()
    val selectedJob by workFeatureViewModel.selectedJob.collectAsState()
    val workTasks by workFeatureViewModel.tasks.collectAsState()
    val workFeatureState by workFeatureViewModel.featureState.collectAsState()
    val workBusy by workFeatureViewModel.busy.collectAsState()
    val workMessage by workFeatureViewModel.message.collectAsState()
    val dailyRewardState by dailyRewardViewModel.state.collectAsState()
    val dailyRewardBusy by dailyRewardViewModel.busy.collectAsState()
    val dailyRewardMessage by dailyRewardViewModel.message.collectAsState()
    val workEnabled = workFeatureState == "enabled"

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { workFeatureViewModel.load() }
    LaunchedEffect(playMessage) {
        playMessage?.let {
            snackbarHostState.showSnackbar(it)
            playViewModel.clearPlayMessage()
        }
    }
    LaunchedEffect(workMessage) {
        workMessage?.let {
            snackbarHostState.showSnackbar(it)
            workFeatureViewModel.clearMessage()
            playViewModel.loadPlayData()
        }
    }
    LaunchedEffect(dailyRewardMessage) {
        dailyRewardMessage?.let {
            snackbarHostState.showSnackbar(it)
            dailyRewardViewModel.clearMessage()
            playViewModel.loadPlayData()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text("🎮 플레이 & 플레이 루프", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))

                MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("🎁 일일 출석 보상", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    when (val state = dailyRewardState) {
                        is UiState.Success -> {
                            val reward = state.data
                            Text(
                                if (reward.available) "오늘의 출석 보상을 받을 수 있습니다."
                                else "오늘 출석 보상은 이미 받았거나 아직 수령 시간이 아닙니다.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!reward.available && reward.nextEligibleAt != null) {
                                Text("다음 수령 가능: ${reward.nextEligibleAt}", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            MoneyverseButton(
                                text = when {
                                    dailyRewardBusy -> "출석 처리 중…"
                                    reward.available -> "일일 출석 보상 받기"
                                    else -> "오늘 출석 완료"
                                },
                                onClick = { dailyRewardViewModel.claim() },
                                enabled = reward.available && !dailyRewardBusy,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is UiState.Error -> {
                            Text("출석 상태를 불러오지 못했습니다: ${state.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { dailyRewardViewModel.load() }, modifier = Modifier.fillMaxWidth()) {
                                Text("출석 상태 다시 확인")
                            }
                        }
                        else -> SkeletonLoader()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("💼 직업 선택 & WLD 근무", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("직업을 선택한 뒤 아래 근무 과제를 완료하면 서버 원장을 통해 WLD와 EXP가 지급됩니다.", style = MaterialTheme.typography.bodySmall)
                if (!workEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("관리자 제한 적용 중 · $workFeatureState") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                workFeatureViewModel.careers.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { career ->
                            val selected = selectedJob == career.code
                            if (selected) {
                                Button(
                                    onClick = { workFeatureViewModel.selectCareer(career.code) },
                                    enabled = !workBusy && workEnabled,
                                    modifier = Modifier.weight(1f)
                                ) { Text("✓ ${career.label}") }
                            } else {
                                OutlinedButton(
                                    onClick = { workFeatureViewModel.selectCareer(career.code) },
                                    enabled = !workBusy && workEnabled,
                                    modifier = Modifier.weight(1f)
                                ) { Text(career.label) }
                            }
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            item {
                when (val wState = workState) {
                    is UiState.Success -> {
                        val work = wState.data
                        MoneyverseCard {
                            Text("근무 지급 현황", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(work.estimatedReward, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            work.lastWorkedAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                    is UiState.Loading -> SkeletonLoader()
                    else -> {}
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("🧰 근무 과제", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    when {
                        !workEnabled -> "관리자 정책으로 현재 직업 작업 기능이 제한되어 있습니다."
                        selectedJob == null -> "먼저 위에서 직업을 선택하세요. 직업 선택 후 해당 과제를 바로 수행할 수 있습니다."
                        else -> "선택한 직업에 맞는 과제를 완료해 WLD를 벌 수 있습니다. 일일 한도는 서버 기준으로 표시됩니다."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val visibleTasks = if (selectedJob == null) workTasks.filter { it.recommended }.take(4)
                else workTasks.filter { it.jobType == selectedJob }
            items(visibleTasks, key = { it.id }) { task ->
                MoneyverseCard(containerColor = if (task.recommended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
                    Text(task.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(task.description, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("보상 ${task.reward} WLD · ${task.experience} EXP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    if (task.dailyLimit > 0) {
                        val quotaText = if (task.quotaReached) {
                            "오늘 ${task.takenToday}/${task.dailyLimit}회 · 일일 한도 소진"
                        } else {
                            "오늘 ${task.takenToday}/${task.dailyLimit}회 · 남은 횟수 ${task.remainingToday}회"
                        }
                        Text(
                            quotaText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.quotaReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (task.minimumDurationSeconds > 0) {
                        Text("최소 수행시간 ${task.minimumDurationSeconds}초", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    MoneyverseButton(
                        text = when {
                            workBusy -> "처리 중…"
                            task.quotaReached -> "오늘 수행 한도 완료"
                            else -> "근무 완료 · 보상 받기"
                        },
                        onClick = { workFeatureViewModel.completeTask(task) },
                        enabled = workEnabled && !workBusy && !task.quotaReached && selectedJob != null && task.jobType == selectedJob,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

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
                                progress = { if (p.requiredExp > 0) p.currentExp.toFloat() / p.requiredExp.toFloat() else 0f },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val unlockedFeatures = p.unlockedFeatures.orEmpty()
                            Text("해금된 혜택: ${unlockedFeatures.joinToString(", ").ifBlank { "아직 해금된 혜택이 없습니다" }}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    else -> {}
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("🚀 초반 진행 미션", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (val tState = tasksState) {
                is UiState.Success -> {
                    items(tState.data) { task ->
                        MoneyverseCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
