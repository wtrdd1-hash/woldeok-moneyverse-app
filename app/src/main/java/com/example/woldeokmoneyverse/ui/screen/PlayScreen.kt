package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.*
import com.example.woldeokmoneyverse.util.formatMoneyAmount
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val subTabs = listOf("데일리 보상 & 루프", "커리어 & 전문직 근무", "월드 시즌 & 리그 패스")

    Column(modifier = Modifier.fillMaxSize()) {
        MoneyverseSubTabRow(
            tabs = subTabs,
            selectedTabIndex = selectedSubTab,
            onTabSelected = { selectedSubTab = it }
        )

        when (selectedSubTab) {
            0 -> PlayMainLoopSubTab(playViewModel)
            1 -> CareerWorkSubTab(playViewModel)
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
    val engagementState by playViewModel.engagementState.collectAsState()
    val profileTitlesState by playViewModel.profileTitlesState.collectAsState()
    val creditGradeState by playViewModel.creditGradeState.collectAsState()
    val playMessage by playViewModel.playMessage.collectAsState()
    val selectedJob by workFeatureViewModel.selectedJob.collectAsState()
    val workTasks by workFeatureViewModel.tasks.collectAsState()
    val workFeatureState by workFeatureViewModel.featureState.collectAsState()
    val workBusy by workFeatureViewModel.busy.collectAsState()
    val workRewardQuotaReached by workFeatureViewModel.rewardQuotaReached.collectAsState()
    val workRewardQuotaSummary by workFeatureViewModel.rewardQuotaSummary.collectAsState()
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

                // 🎯 일일 & 주간 인게이지먼트 목표 및 NPC 오더
                when (val engState = engagementState) {
                    is UiState.Success -> {
                        val eng = engState.data
                        MoneyverseCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯 일일 & 주간 인게이지먼트", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                if (eng.streakDays > 0) {
                                    Surface(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(8.dp)) {
                                        Text("🔥 ${eng.streakDays}일 연속 달성", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSecondary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val allGoals = eng.todayGoals + eng.weeklyGoals
                            if (allGoals.isNotEmpty()) {
                                allGoals.take(4).forEach { goal ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(goal.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            Text("${goal.description} (${goal.current}/${goal.target})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(
                                            if (goal.isCompleted) "✓ 완료" else "+${formatMoneyAmount(goal.rewardWld)} WLD",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (goal.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("🤝 NPC 일일 의뢰 수락", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { playViewModel.takeNpcOrder("ORDER_WORK") },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp)
                                ) { Text("💼 직업 의뢰", style = MaterialTheme.typography.labelMedium) }
                                Button(
                                    onClick = { playViewModel.takeNpcOrder("ORDER_STOCK") },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp)
                                ) { Text("📈 주식 의뢰", style = MaterialTheme.typography.labelMedium) }
                                Button(
                                    onClick = { playViewModel.takeNpcOrder("ORDER_STUDY") },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp)
                                ) { Text("📚 금융 의뢰", style = MaterialTheme.typography.labelMedium) }
                            }
                        }
                    }
                    is UiState.Loading -> SkeletonLoader()
                    else -> Unit
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
                        workRewardQuotaReached -> "관리자 정책의 근무 보상 한도에 도달했습니다. ${workRewardQuotaSummary.orEmpty()}"
                        else -> "선택한 직업에 맞는 과제를 완료해 WLD를 벌 수 있습니다. ${workRewardQuotaSummary.orEmpty()}"
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
                    Text("보상 ${formatMoneyAmount(task.reward)} WLD · ${task.experience} EXP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
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
                            workRewardQuotaReached -> "근무 보상 한도 도달"
                            task.quotaReached -> "오늘 수행 한도 완료"
                            else -> "근무 완료 · 보상 받기"
                        },
                        onClick = { workFeatureViewModel.completeTask(task) },
                        enabled = workEnabled && !workBusy && !workRewardQuotaReached && !task.quotaReached && selectedJob != null && task.jobType == selectedJob,
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

                // 💳 금융 신용 등급 및 우대 혜택
                when (val cState = creditGradeState) {
                    is UiState.Success -> {
                        val c = cState.data
                        Spacer(modifier = Modifier.height(10.dp))
                        MoneyverseCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("💳 금융 신용 등급 [${c.grade}등급]", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("신용 점수: ${c.score}점 · 대출 한도: ${c.maxLoanLimit} WLD", style = MaterialTheme.typography.bodySmall)
                                    Text("기준 금리: 연 ${c.interestRateApr}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        "${c.grade} GRADE",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (c.perks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("등급 우대: ${c.perks.joinToString(" · ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    else -> Unit
                }

                // 🏷️ 보유 칭호 목록
                when (val titleState = profileTitlesState) {
                    is UiState.Success -> {
                        val titles = titleState.data
                        if (titles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            MoneyverseCard {
                                Text("🏷️ 보유 칭호 (${titles.size}종)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                titles.take(5).forEach { title ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(title.name.ifBlank { title.code }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                        Text(if (title.isEquipped) "장착 중" else "보유", style = MaterialTheme.typography.labelSmall, color = if (title.isEquipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    else -> Unit
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
                                    text = if (task.isCompleted) "✓ 완료" else "+${formatMoneyAmount(task.rewardAmount)} WLD",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerWorkSubTab(
    playViewModel: PlayViewModel,
    workFeatureViewModel: WorkFeatureViewModel = viewModel()
) {
    val selectedJob by workFeatureViewModel.selectedJob.collectAsState()
    val workTasks by workFeatureViewModel.tasks.collectAsState()
    val workFeatureState by workFeatureViewModel.featureState.collectAsState()
    val workBusy by workFeatureViewModel.busy.collectAsState()
    val workRewardQuotaReached by workFeatureViewModel.rewardQuotaReached.collectAsState()
    val workRewardQuotaSummary by workFeatureViewModel.rewardQuotaSummary.collectAsState()
    val workMessage by workFeatureViewModel.message.collectAsState()
    val workEnabled = workFeatureState == "enabled"

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        workFeatureViewModel.load()
    }

    LaunchedEffect(workMessage) {
        workMessage?.let {
            snackbarHostState.showSnackbar(it)
            workFeatureViewModel.clearMessage()
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
                Text("🏢 커리어 & WLD 근무 센터", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("선택한 직업에 맞춰 근무 과제를 수행하고, 블록체인 원장을 통해 WLD 급여와 EXP를 획득하세요.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))

                // 근무 한도 요약 카드
                MoneyverseCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("📊 근무 급여 및 쿼터 현황", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        workRewardQuotaSummary ?: "근무 한도 정보를 불러오는 중입니다...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (workRewardQuotaReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!workEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("⚠️ 현재 관리자 정책에 의해 직업 근무 기능이 일시 제한되어 있습니다.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("👔 직업 선택", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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

                Spacer(modifier = Modifier.height(16.dp))
                Text("🧰 배정된 근무 과제 목록", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    when {
                        !workEnabled -> "관리자 정책으로 현재 직업 작업 기능이 제한되어 있습니다."
                        selectedJob == null -> "먼저 위에서 직업을 선택하세요."
                        workRewardQuotaReached -> "근무 보상 한도에 도달했습니다."
                        else -> "과제 완료 시 서버 원장을 통해 WLD가 즉시 정산됩니다."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val visibleTasks = if (selectedJob == null) workTasks.filter { it.recommended }.take(4)
                else workTasks.filter { it.jobType == selectedJob }

            if (visibleTasks.isEmpty()) {
                item {
                    Text("수행 가능한 근무 과제가 없습니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            items(visibleTasks, key = { it.id }) { task ->
                MoneyverseCard(containerColor = if (task.recommended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
                    Text(task.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(task.description, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("보상: +${formatMoneyAmount(task.reward)} WLD · +${task.experience} EXP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)

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
                        Text("최소 수행 시간: ${task.minimumDurationSeconds}초", style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    MoneyverseButton(
                        text = when {
                            workBusy -> "정산 처리 중…"
                            workRewardQuotaReached -> "급여 한도 도달"
                            task.quotaReached -> "일일 수행 완료"
                            else -> "근무 완료 · 보상 받기"
                        },
                        onClick = { workFeatureViewModel.completeTask(task) },
                        enabled = workEnabled && !workBusy && !workRewardQuotaReached && !task.quotaReached && selectedJob != null && task.jobType == selectedJob,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

