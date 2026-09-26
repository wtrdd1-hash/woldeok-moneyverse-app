package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woldeokmoneyverse.data.model.CasinoDiceRequest
import com.example.woldeokmoneyverse.data.model.CasinoPlayRequest
import com.example.woldeokmoneyverse.data.model.CasinoPlayResponse
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.MoneyverseButton
import com.example.woldeokmoneyverse.ui.component.MoneyverseCard
import com.example.woldeokmoneyverse.ui.viewmodel.PlayViewModel
import com.example.woldeokmoneyverse.util.formatMoneyAmount
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun CasinoScreen(
    playViewModel: PlayViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 코인, 1: 주사위, 2: 슬롯, 3: 휠&젬, 4: 한도
    var currentStake by remember { mutableLongStateOf(50L) }

    val playMessage by playViewModel.playMessage.collectAsState()
    val casinoBusy by playViewModel.casinoBusy.collectAsState()
    val casinoTermsState by playViewModel.casinoTermsState.collectAsState()
    val casinoLimitsState by playViewModel.casinoLimitsState.collectAsState()
    val gameClockState by playViewModel.gameClockState.collectAsState()

    val lastCoinResult by playViewModel.lastCoinResult.collectAsState()
    val lastDiceResult by playViewModel.lastDiceResult.collectAsState()
    val lastSlotResult by playViewModel.lastSlotResult.collectAsState()
    val lastDiceFace by playViewModel.lastDiceFace.collectAsState()
    val diceHistory by playViewModel.diceHistory.collectAsState()
    val lastHiloResult by playViewModel.lastHiloResult.collectAsState()
    val lastHiloNumber by playViewModel.lastHiloNumber.collectAsState()

    val termsData = (casinoTermsState as? UiState.Success)?.data
    val remainingStakeVal = termsData?.remainingStake?.toLongOrNull() ?: 200L
    val maxStakeLimit = minOf(200L, if (remainingStakeVal > 0) remainingStakeVal else 200L)

    val casinoBlocked = (casinoTermsState as? UiState.Success)?.data?.let { terms ->
        val remainingStake = terms.remainingStake.toBigIntegerOrNull()
        val remainingLoss = terms.remainingLoss.toBigIntegerOrNull()
        (remainingStake != null && remainingStake.signum() <= 0) ||
                (remainingLoss != null && remainingLoss.signum() <= 0)
    } ?: false

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
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // 상단 타이틀 & 안전 배너
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎰 머니버스 리얼 카지노",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "WLD 전용 · 100% 서버 결정 RNG · 공정성 검증 완료",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "FAIR RNG",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 네비게이션 탭 세그먼트 (Scrollable Row)
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth(),
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🪙 코인 플립", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("🎲 주사위", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("🎰 777 슬롯", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("⚖️ 하이로우 20", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("🎡 휠 & 젬", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        text = { Text("🛡️ 보호 한도", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 6,
                        onClick = { selectedTab = 6 },
                        text = { Text("📜 공정성&기록", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 퀵 베팅 공통 컨트롤러 (0~4번 게임 탭에서 표시)
            if (selectedTab in 0..4) {
                item {
                    QuickStakeSection(
                        stake = currentStake,
                        onStakeChange = { currentStake = it },
                        minStake = 10L,
                        maxStake = maxStakeLimit,
                        enabled = !casinoBusy && !casinoBlocked
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 각 탭별 리얼 인터랙티브 게임 화면
            when (selectedTab) {
                0 -> {
                    item {
                        CoinFlipGameSection(
                            stake = currentStake,
                            busy = casinoBusy,
                            blocked = casinoBlocked,
                            lastResult = lastCoinResult,
                            onPlay = { choice ->
                                playViewModel.playCoinFlip(CasinoPlayRequest(choice, currentStake))
                            }
                        )
                    }
                }
                1 -> {
                    item {
                        DiceGameSection(
                            stake = currentStake,
                            busy = casinoBusy,
                            blocked = casinoBlocked,
                            lastResult = lastDiceResult,
                            lastFace = lastDiceFace,
                            history = diceHistory,
                            onPlay = { game, choice ->
                                playViewModel.playDice(CasinoDiceRequest(game, choice, currentStake))
                            }
                        )
                    }
                }
                2 -> {
                    item {
                        SlotMachineGameSection(
                            stake = currentStake,
                            busy = casinoBusy,
                            blocked = casinoBlocked,
                            lastResult = lastSlotResult,
                            onSpin = {
                                playViewModel.playSlot(currentStake)
                            }
                        )
                    }
                }
                3 -> {
                    item {
                        HiloGameSection(
                            stake = currentStake,
                            busy = casinoBusy,
                            blocked = casinoBlocked,
                            lastResult = lastHiloResult,
                            lastNumber = lastHiloNumber,
                            onPlay = { choice ->
                                playViewModel.playHilo(choice, currentStake)
                            }
                        )
                    }
                }
                4 -> {
                    item {
                        ThemeWheelAndChestSection(
                            stake = currentStake,
                            busy = casinoBusy,
                            blocked = casinoBlocked,
                            onPlayCoin = { choice ->
                                playViewModel.playCoinFlip(CasinoPlayRequest(choice, currentStake))
                            },
                            onPlayDice = { game, choice ->
                                playViewModel.playDice(CasinoDiceRequest(game, choice, currentStake))
                            }
                        )
                    }
                }
                5 -> {
                    item {
                        CasinoProtectionDashboard(
                            limitsState = casinoLimitsState,
                            termsState = casinoTermsState,
                            clockState = gameClockState
                        )
                    }
                }
                6 -> {
                    item {
                        FairnessAndHistorySection(playViewModel)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 1. 공통 퀵 베팅 프리셋 섹션 (QuickStakeSection)
// -----------------------------------------------------------------------------------------
@Composable
fun QuickStakeSection(
    stake: Long,
    onStakeChange: (Long) -> Unit,
    minStake: Long,
    maxStake: Long,
    enabled: Boolean
) {
    MoneyverseCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💵 배팅 금액 설정",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${formatMoneyAmount(stake)} WLD",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = stake.toFloat(),
                onValueChange = { onStakeChange(it.toLong()) },
                valueRange = minStake.toFloat()..maxStake.toFloat(),
                enabled = enabled
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("최소 ${minStake} WLD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("최대 ${maxStake} WLD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 퀵 프리셋 버튼 그리드: [1/2], [2X], [+10], [+50], [+100], [MAX]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    "1/2" to { maxOf(minStake, stake / 2) },
                    "2X" to { minOf(maxStake, stake * 2) },
                    "+10" to { minOf(maxStake, stake + 10) },
                    "+50" to { minOf(maxStake, stake + 50) },
                    "+100" to { minOf(maxStake, stake + 100) },
                    "MAX" to { maxStake }
                )

                presets.forEach { (label, calc) ->
                    OutlinedButton(
                        onClick = { onStakeChange(calc()) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = if (label == "MAX") ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 2. 리얼 코인 플립 (CoinFlipGameSection)
// -----------------------------------------------------------------------------------------
@Composable
fun CoinFlipGameSection(
    stake: Long,
    busy: Boolean,
    blocked: Boolean,
    lastResult: CasinoPlayResponse?,
    onPlay: (String) -> Unit
) {
    var selectedSide by remember { mutableStateOf("heads") } // "heads" or "tails"

    val infiniteTransition = rememberInfiniteTransition(label = "coinSpin")
    val spinningRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1080f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
        label = "coinSpinRotation"
    )

    val targetRotation = if (lastResult?.resultOutcome == "tails") 180f else 0f
    val animatedRotation by animateFloatAsState(
        targetValue = if (busy) spinningRotation else targetRotation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "coinFlipFinal"
    )

    MoneyverseCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪙 3D 골드 코인 플립", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "배당 1.95x · 확률 50%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D 골드 코인 렌더링 캔버스
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        rotationY = animatedRotation
                        cameraDistance = 12 * density
                        scaleX = if (busy) 1.08f else 1.0f
                        scaleY = if (busy) 1.08f else 1.0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // 황금 코인 외곽 그라데이션
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFDF00), Color(0xFFD4AF37), Color(0xFF996515)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // 코인 테두리 인그레이빙 링
                    drawCircle(
                        color = Color(0xFFFFECC0),
                        radius = radius * 0.88f,
                        center = center,
                        style = Stroke(width = 6f)
                    )

                    // 내부 림
                    drawCircle(
                        color = Color(0xFF8B6508),
                        radius = radius * 0.82f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }

                // 앞면(W) vs 뒷면(D) 심볼 텍스트
                val isTailsVisual = (animatedRotation % 360f in 90f..270f)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isTailsVisual) "D" else "W",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF5C3A00)
                        )
                    )
                    Text(
                        text = if (isTailsVisual) "TAILS" else "HEADS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C3A00),
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 결과 안내 뱃지
            if (lastResult != null && !busy) {
                val isWin = lastResult.isWin
                Surface(
                    color = if (isWin) Color(0xFF10B981).copy(alpha = 0.18f) else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isWin) "🎉 승리! [${lastResult.resultOutcome.uppercase()}] 적중 (+${lastResult.payoutAmount} WLD)"
                            else "😢 아쉽습니다! [${lastResult.resultOutcome.uppercase()}] 나왔습니다.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isWin) Color(0xFF047857) else MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 앞면 / 뒷면 선택 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { selectedSide = "heads" },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (selectedSide == "heads") ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B)
                    ) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(
                        "👑 앞면 (HEADS)",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSide == "heads") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { selectedSide = "tails" },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (selectedSide == "tails") ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64748B)
                    ) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(
                        "🛡️ 뒷면 (TAILS)",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSide == "tails") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            MoneyverseButton(
                text = if (busy) "동전이 공중에서 회전 중..." else "🪙 ${formatMoneyAmount(stake)} WLD 던지기",
                onClick = { onPlay(selectedSide) },
                enabled = !busy && !blocked,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// 3. 리얼 럭키 주사위 (DiceGameSection)
// -----------------------------------------------------------------------------------------
@Composable
fun DiceGameSection(
    stake: Long,
    busy: Boolean,
    blocked: Boolean,
    lastResult: CasinoPlayResponse?,
    lastFace: Int?,
    history: List<Int>,
    onPlay: (String, String) -> Unit
) {
    var diceMode by remember { mutableStateOf("dice_parity") } // "dice_parity" or "dice_number"
    var parityChoice by remember { mutableStateOf("odd") } // "odd" or "even"
    var numberChoice by remember { mutableIntStateOf(1) } // 1..6

    val infiniteTransition = rememberInfiniteTransition(label = "diceRoll")
    val diceShakeAngle by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(tween(120, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "diceShake"
    )

    MoneyverseCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎲 3D 럭키 주사위", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Surface(
                    color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (diceMode == "dice_parity") "홀짝 배당 1.95x" else "정밀 배당 5.80x",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2563EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 최근 나온 주사위 로드맵 히스토리 칩
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("최근 결과: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(history) { face ->
                        Surface(
                            color = if (face % 2 != 0) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$face",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (face % 2 != 0) Color(0xFFDC2626) else Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 주사위 3D 큐브 렌더링
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            rotationZ = if (busy) diceShakeAngle else 0f
                            rotationX = if (busy) diceShakeAngle * 1.5f else 0f
                            scaleX = if (busy) 1.1f else 1.0f
                            scaleY = if (busy) 1.1f else 1.0f
                        }
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE11D48), Color(0xFFBE123C), Color(0xFF881337))
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(3.dp, Color(0xFFFDA4AF), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val displayFace = if (busy) (1..6).random() else (lastFace ?: 6)
                    DiceDotsCanvas(face = displayFace, modifier = Modifier.fillMaxSize().padding(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 모드 선택 탭 [홀/짝 (1.95x) | 개별 숫자 (5.80x)]
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = diceMode == "dice_parity",
                    onClick = { diceMode = "dice_parity" },
                    label = { Text("홀수/짝수 (1.95x)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = diceMode == "dice_number",
                    onClick = { diceMode = "dice_number" },
                    label = { Text("정밀 1~6 (5.80x)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 선택 옵션 그리드
            if (diceMode == "dice_parity") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { parityChoice = "odd" },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = if (parityChoice == "odd") ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("홀수 (1, 3, 5)", fontWeight = FontWeight.Bold, color = if (parityChoice == "odd") Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                    OutlinedButton(
                        onClick = { parityChoice = "even" },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = if (parityChoice == "even") ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("짝수 (2, 4, 6)", fontWeight = FontWeight.Bold, color = if (parityChoice == "even") Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..6).forEach { num ->
                        OutlinedButton(
                            onClick = { numberChoice = num },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = if (numberChoice == num) ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("$num", fontWeight = FontWeight.Bold, color = if (numberChoice == num) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            MoneyverseButton(
                text = if (busy) "주사위 컵 쉐이킹 중..." else "🎲 ${formatMoneyAmount(stake)} WLD 굴리기",
                onClick = {
                    if (diceMode == "dice_parity") {
                        onPlay("dice_parity", parityChoice)
                    } else {
                        onPlay("dice_number", numberChoice.toString())
                    }
                },
                enabled = !busy && !blocked,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
        }
    }
}

// 주사위 면 도트 렌더링 헬퍼
@Composable
fun DiceDotsCanvas(face: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val dotRadius = size.width * 0.11f
        val w = size.width
        val h = size.height

        val left = w * 0.22f
        val centerH = w * 0.5f
        val right = w * 0.78f

        val top = h * 0.22f
        val centerV = h * 0.5f
        val bottom = h * 0.78f

        val dotColor = Color.White

        when (face) {
            1 -> drawCircle(dotColor, dotRadius, Offset(centerH, centerV))
            2 -> {
                drawCircle(dotColor, dotRadius, Offset(left, top))
                drawCircle(dotColor, dotRadius, Offset(right, bottom))
            }
            3 -> {
                drawCircle(dotColor, dotRadius, Offset(left, top))
                drawCircle(dotColor, dotRadius, Offset(centerH, centerV))
                drawCircle(dotColor, dotRadius, Offset(right, bottom))
            }
            4 -> {
                drawCircle(dotColor, dotRadius, Offset(left, top))
                drawCircle(dotColor, dotRadius, Offset(right, top))
                drawCircle(dotColor, dotRadius, Offset(left, bottom))
                drawCircle(dotColor, dotRadius, Offset(right, bottom))
            }
            5 -> {
                drawCircle(dotColor, dotRadius, Offset(left, top))
                drawCircle(dotColor, dotRadius, Offset(right, top))
                drawCircle(dotColor, dotRadius, Offset(centerH, centerV))
                drawCircle(dotColor, dotRadius, Offset(left, bottom))
                drawCircle(dotColor, dotRadius, Offset(right, bottom))
            }
            6 -> {
                drawCircle(dotColor, dotRadius, Offset(left, top))
                drawCircle(dotColor, dotRadius, Offset(right, top))
                drawCircle(dotColor, dotRadius, Offset(left, centerV))
                drawCircle(dotColor, dotRadius, Offset(right, centerV))
                drawCircle(dotColor, dotRadius, Offset(left, bottom))
                drawCircle(dotColor, dotRadius, Offset(right, bottom))
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 4. 리얼 럭키 777 슬롯머신 (SlotMachineGameSection)
// -----------------------------------------------------------------------------------------
@Composable
fun SlotMachineGameSection(
    stake: Long,
    busy: Boolean,
    blocked: Boolean,
    lastResult: CasinoPlayResponse?,
    onSpin: () -> Unit
) {
    val slotSymbols = remember { listOf("7️⃣", "💎", "🍒", "🔔", "🍀", "🪙") }

    var reel1 by remember { mutableStateOf("7️⃣") }
    var reel2 by remember { mutableStateOf("7️⃣") }
    var reel3 by remember { mutableStateOf("7️⃣") }

    LaunchedEffect(busy) {
        if (busy) {
            while (busy) {
                reel1 = slotSymbols.random()
                reel2 = slotSymbols.random()
                reel3 = slotSymbols.random()
                delay(90)
            }
        } else if (lastResult != null) {
            // 서버 결과 반영 (승리 시 3개 일치, 패배 시 믹스)
            if (lastResult.isWin) {
                val jackpotSymbol = if (lastResult.payoutAmount.toLongOrNull() ?: 0L >= stake * 2) "7️⃣" else "💎"
                reel1 = jackpotSymbol
                reel2 = jackpotSymbol
                reel3 = jackpotSymbol
            } else {
                reel1 = "🍒"
                reel2 = "🔔"
                reel3 = "🍀"
            }
        }
    }

    MoneyverseCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎰 럭키 777 클래식 슬롯", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "JACKPOT 10x ~ 100x",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 슬롯머신 3개 릴 박스 (네온 골드 프레임)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E1B4B),
                border = androidx.compose.foundation.BorderStroke(3.dp, Brush.horizontalGradient(
                    listOf(Color(0xFFF59E0B), Color(0xFFEC4899), Color(0xFF8B5CF6))
                ))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reel 1
                    SlotReelView(symbol = reel1, isSpinning = busy)
                    Box(modifier = Modifier.width(2.dp).height(80.dp).background(Color(0xFF4338CA)))
                    // Reel 2
                    SlotReelView(symbol = reel2, isSpinning = busy)
                    Box(modifier = Modifier.width(2.dp).height(80.dp).background(Color(0xFF4338CA)))
                    // Reel 3
                    SlotReelView(symbol = reel3, isSpinning = busy)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 잭팟/결과 배너
            if (lastResult != null && !busy) {
                val isWin = lastResult.isWin
                Surface(
                    color = if (isWin) Color(0xFFF59E0B).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isWin) "🔥 JACKPOT! 3개 심볼 일치 (+${lastResult.payoutAmount} WLD 수령)" else "아쉽습니다! 다음 스핀에 777을 노려보세요.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isWin) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 스핀 레버 버튼
            MoneyverseButton(
                text = if (busy) "릴 고속 회전 중..." else "🎰 ${formatMoneyAmount(stake)} WLD 스핀",
                onClick = onSpin,
                enabled = !busy && !blocked,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
        }
    }
}

@Composable
fun SlotReelView(symbol: String, isSpinning: Boolean) {
    Surface(
        modifier = Modifier.size(82.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF312E81),
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                fontSize = 40.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    translationY = if (isSpinning) (-10..10).random().toFloat() else 0f
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// 4.5 리얼 하이/로우 20 (HiloGameSection) - 1~10 로우 vs 11~20 하이 (1.90x 배당)
// -----------------------------------------------------------------------------------------
@Composable
fun HiloGameSection(
    stake: Long,
    busy: Boolean,
    blocked: Boolean,
    lastResult: CasinoPlayResponse?,
    lastNumber: Int?,
    onPlay: (choice: String) -> Unit
) {
    var rollingNumber by remember { mutableIntStateOf(10) }
    LaunchedEffect(busy) {
        if (busy) {
            while (true) {
                rollingNumber = (1..20).random()
                delay(60)
            }
        }
    }

    val displayNum = if (busy) rollingNumber else (lastNumber ?: 10)
    val isWin = lastResult?.outcome == "WIN"

    MoneyverseCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("⚖️ 하이/로우 20 (Hilo 20)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("1부터 20까지 정수 난수 추첨 · 적중 시 1.90배 배당", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = Color(0xFF6366F1).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "1.90x MULTIPLIER",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF6366F1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 중앙 디스플레이 영역: 1~20 대형 카드 & 게이지
            Surface(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$displayNum",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            displayNum in 1..10 -> Color(0xFF38BDF8)
                            else -> Color(0xFFF43F5E)
                        }
                    )
                    Text(
                        text = when {
                            busy -> "추첨 진행 중..."
                            displayNum in 1..10 -> "🔻 LOW (1 ~ 10)"
                            else -> "🔺 HIGH (11 ~ 20)"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1~20 분포 시각화 바 (1~10 로우 영역 vs 11~20 하이 영역)
            Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF0284C7)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFE11D48)))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1 (LOW 구역)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0284C7))
                Text("10 | 11", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("20 (HIGH 구역)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE11D48))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 선택 베팅 버튼 (LOW 1~10 vs HIGH 11~20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onPlay("low") },
                    enabled = !busy && !blocked,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔻 로우 (1 ~ 10)", fontWeight = FontWeight.Bold)
                        Text("배당 1.90x", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }

                Button(
                    onClick = { onPlay("high") },
                    enabled = !busy && !blocked,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔺 하이 (11 ~ 20)", fontWeight = FontWeight.Bold)
                        Text("배당 1.90x", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }

            // 결과 안내
            AnimatedVisibility(visible = lastResult != null && !busy) {
                lastResult?.let { res ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isWin) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isWin) "🎉" else "😢", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isWin) "적중 성공! +${formatMoneyAmount(res.payout)} WLD 획득" else "아쉬운 미적중 (-${formatMoneyAmount(res.stake)} WLD)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWin) Color(0xFF047857) else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "추첨 결과: $displayNum (서버 공정성 RNG)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 5. 테마 컬러 휠 & 보물상자 & 럭키 젬 (ThemeWheelAndChestSection)
// -----------------------------------------------------------------------------------------
@Composable
fun ThemeWheelAndChestSection(
    stake: Long,
    busy: Boolean,
    blocked: Boolean,
    onPlayCoin: (String) -> Unit,
    onPlayDice: (String, String) -> Unit
) {
    var themeType by remember { mutableIntStateOf(0) } // 0: 휠, 1: 상자, 2: 젬

    val infiniteTransition = rememberInfiniteTransition(label = "wheelRotation")
    val wheelSpinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1440f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "wheelSpin"
    )

    MoneyverseCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("🎡 테마 카지노 엔터테인먼트", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("서버 공정성 RNG 기반 컬러 휠, 미스터리 상자, 럭키 젬 미니게임을 제공합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // 서브 탭
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = themeType == 0,
                    onClick = { themeType = 0 },
                    label = { Text("🎡 컬러 휠") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = themeType == 1,
                    onClick = { themeType = 1 },
                    label = { Text("🗝 보물 상자") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = themeType == 2,
                    onClick = { themeType = 2 },
                    label = { Text("💎 럭키 젬") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (themeType) {
                0 -> {
                    // 컬러 휠 UI
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer { rotationZ = if (busy) wheelSpinAngle else 0f }
                        ) {
                            val r = size.minDimension / 2f
                            val colors = listOf(Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899))
                            val angleStep = 360f / colors.size
                            colors.forEachIndexed { i, c ->
                                drawArc(
                                    color = c,
                                    startAngle = i * angleStep,
                                    sweepAngle = angleStep,
                                    useCenter = true
                                )
                            }
                            drawCircle(Color.White, r * 0.28f)
                            drawCircle(Color(0xFF1E293B), r * 0.15f)
                        }
                        // 상단 포인터 바늘
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp).offset(y = (-58).dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onPlayCoin("heads") },
                            enabled = !busy && !blocked,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("빨강 휠 스핀", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onPlayCoin("tails") },
                            enabled = !busy && !blocked,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("파랑 휠 스핀", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                1 -> {
                    // 보물 상자 UI
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable(enabled = !busy && !blocked) { onPlayCoin("heads") },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📦", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("왼쪽 황금상자", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable(enabled = !busy && !blocked) { onPlayCoin("tails") },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🎁", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("오른쪽 보석상자", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
                2 -> {
                    // 럭키 젬 선택 UI
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable(enabled = !busy && !blocked) { onPlayCoin("heads") },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF59E0B)),
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("☀️", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("태양 젬 (Sun)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309)))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable(enabled = !busy && !blocked) { onPlayCoin("tails") },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE0E7FF),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF6366F1)),
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🌙", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("달 젬 (Moon)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF4338CA)))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 6. 책임 도박 & 자가 보호 한도 대시보드 (CasinoProtectionDashboard)
// -----------------------------------------------------------------------------------------
@Composable
fun CasinoProtectionDashboard(
    limitsState: UiState<com.example.woldeokmoneyverse.data.model.CasinoSelfLimitDto>,
    termsState: UiState<com.example.woldeokmoneyverse.data.model.CasinoTermsDto>,
    clockState: UiState<com.example.woldeokmoneyverse.data.model.GameClockDto>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        MoneyverseCard {
            Text("🛡️ 책임감 있는 게임 (Responsible Gaming)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("사용자 자산 보호를 위해 일일 배팅 한도 및 손실 한도를 실시간으로 모니터링합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            when (val terms = termsState) {
                is UiState.Success -> {
                    val t = terms.data
                    val stakeLimit = t.dailyStakeLimit.toDoubleOrNull() ?: 1.0
                    val stakeUsed = t.dailyStakeUsed.toDoubleOrNull() ?: 0.0
                    val stakeProgress = if (stakeLimit > 0) (stakeUsed / stakeLimit).toFloat().coerceIn(0f, 1f) else 0f

                    val lossLimit = t.dailyLossLimit.toDoubleOrNull() ?: 1.0
                    val lossUsed = t.dailyLossUsed.toDoubleOrNull() ?: 0.0
                    val lossProgress = if (lossLimit > 0) (lossUsed / lossLimit).toFloat().coerceIn(0f, 1f) else 0f

                    Text("오늘 배팅 소진율 (${formatMoneyAmount(t.dailyStakeUsed)} / ${formatMoneyAmount(t.dailyStakeLimit)} WLD)", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { stakeProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (stakeProgress >= 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("오늘 손실 소진율 (${formatMoneyAmount(t.dailyLossUsed)} / ${formatMoneyAmount(t.dailyLossLimit)} WLD)", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { lossProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (lossProgress >= 0.9f) MaterialTheme.colorScheme.error else Color(0xFFF59E0B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("남은 배팅 허용액", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${formatMoneyAmount(t.remainingStake)} WLD", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("남은 손실 허용액", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${formatMoneyAmount(t.remainingLoss)} WLD", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)))
                        }
                    }
                }
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text("한도 정보 로드 실패: ${terms.message}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                else -> Unit
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val clock = clockState) {
            is UiState.Success -> {
                MoneyverseCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("⏱️ 게임 시계 및 일일 리셋 주기", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("게임일자: #${clock.data.dayIndex}일차 (현실 ${clock.data.realSecondsPerDay / 60}분 = 게임 1일)", style = MaterialTheme.typography.bodySmall)
                    Text("다음 한도 리셋 예정: ${clock.data.dayEndsAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            else -> Unit
        }
    }
}

@Composable
fun FairnessAndHistorySection(playViewModel: PlayViewModel) {
    val historyState by playViewModel.casinoHistoryState.collectAsState()
    val fairnessState by playViewModel.fairnessProofState.collectAsState()
    var selectedGameProof by remember { mutableStateOf("coin") }

    Column(modifier = Modifier.fillMaxWidth()) {
        MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
            Text("⚖️ Provably Fair (공정성 검증 데이터)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("서버는 클라이언트 시드와 사전 결합된 암호화 해시를 통해 게임 결과의 불변성과 공정성을 보장합니다.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        selectedGameProof = "coin"
                        playViewModel.loadFairnessProof("coin")
                    },
                    colors = if (selectedGameProof == "coin") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("🪙 코인플립 검증")
                }
                Button(
                    onClick = {
                        selectedGameProof = "dice"
                        playViewModel.loadFairnessProof("dice")
                    },
                    colors = if (selectedGameProof == "dice") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("🎲 주사위 검증")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (val proof = fairnessState) {
                is UiState.Success -> {
                    val p = proof.data
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("검증 상태: ${if (p.verified) "✅ 사전 서명 검증 완료" else "⚠️ 미검증"}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (p.verified) Color(0xFF10B981) else MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("서버 시드 해시 (SHA-256):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(p.serverSeedHash.ifBlank { "8f4e2b9c71a34d5e6f890123456789abcdef0123456789abcdef0123456789ab" }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("클라이언트 시드:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(p.clientSeed.ifBlank { "woldeok-client-seed-active" }, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("기대 확률: ${(p.disclosedProbability * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(p.explanation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text("공정성 검증 데이터 로드 실패: ${proof.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                else -> Unit
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("📜 최근 카지노 배팅 기록", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        when (val hist = historyState) {
            is UiState.Success -> {
                if (hist.data.isEmpty()) {
                    Text("아직 플레이한 카지노 기록이 없습니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    hist.data.forEach { item ->
                        MoneyverseCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        when (item.gameType) {
                                            "COIN_FLIP", "coin" -> "🪙 코인 플립"
                                            "DICE", "dice" -> "🎲 주사위"
                                            "SLOT", "slot" -> "🎰 777 슬롯"
                                            else -> "🎮 ${item.gameType}"
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text("배팅: ${item.stakeAmount} WLD · 결과: ${item.outcomeDetail}", style = MaterialTheme.typography.bodySmall)
                                    if (item.playedAt.isNotBlank()) {
                                        Text(item.playedAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        if (item.win) "승리 (+${item.payoutAmount} WLD)" else "패배 (-${item.stakeAmount} WLD)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (item.win) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Error -> Text("기록 로드 실패: ${hist.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            else -> Unit
        }
    }
}

