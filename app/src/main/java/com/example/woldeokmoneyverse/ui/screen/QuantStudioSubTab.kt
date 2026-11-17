package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.woldeokmoneyverse.data.model.QuantBacktestResultDto
import com.example.woldeokmoneyverse.data.model.QuantStrategyDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.EconomyViewModel

@Composable
fun QuantStudioSubTab(
    viewModel: EconomyViewModel,
    haptic: FintechHapticController
) {
    val strategiesState by viewModel.quantStrategiesState.collectAsState()
    val backtestState by viewModel.backtestResultState.collectAsState()
    var isCreateModalOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadQuantStrategies()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 헤더 & 신규 봇 생성 버튼
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
                            Text(
                                text = "🤖 노코드 퀀트 봇 스튜디오",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "수식 코딩 없이 조건식만으로 주식/자산 자동매매 봇을 구축합니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            haptic.click()
                            viewModel.loadQuantStrategies()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            haptic.click()
                            isCreateModalOpen = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("새 퀀트 알고리즘 봇 만들기", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. 과거 30일 시뮬레이션 백테스팅 차트 (결과가 있을 때 표시)
        item {
            val bt = backtestState
            if (bt is UiState.Success && bt.data.profitCurve.isNotEmpty()) {
                QuantProfitChartCard(result = bt.data)
            }
        }

        // 3. 전략 목록
        item {
            Text(
                text = "가동 중인 퀀트 봇",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        when (val state = strategiesState) {
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
                        text = "전략 로드 실패: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            is UiState.Success -> {
                val list: List<QuantStrategyDto> = state.data
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
                            Text("등록된 퀀트 봇이 없습니다. 새 봇을 만들어보세요!", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    items(list) { strategy: QuantStrategyDto ->
                        QuantStrategyCard(
                            strategy = strategy,
                            onToggle = {
                                haptic.click()
                                viewModel.toggleQuantStrategy(strategy.id)
                            },
                            onRunBacktest = {
                                haptic.click()
                                viewModel.runQuantBacktest(
                                    symbol = strategy.symbol,
                                    indicator = strategy.indicator,
                                    condition = strategy.condition,
                                    threshold = strategy.threshold
                                )
                            }
                        )
                    }
                }
            }
            else -> {}
        }
    }

    if (isCreateModalOpen) {
        CreateQuantStrategyDialog(
            onDismiss = { isCreateModalOpen = false },
            onSubmit = { name, symbol, indicator, condition, threshold, action ->
                haptic.success()
                viewModel.createQuantStrategy(name, symbol, indicator, condition, threshold, action)
                isCreateModalOpen = false
            },
            onPreBacktest = { symbol, indicator, condition, threshold ->
                haptic.click()
                viewModel.runQuantBacktest(symbol, indicator, condition, threshold)
            },
            backtestState = backtestState
        )
    }
}

@Composable
fun QuantStrategyCard(
    strategy: QuantStrategyDto,
    onToggle: () -> Unit,
    onRunBacktest: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (strategy.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = strategy.symbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strategy.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = strategy.isActive,
                    onCheckedChange = { onToggle() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 조건식 칩 표출
            val conditionKorean = when (strategy.condition) {
                "LESS_THAN" -> "이하"
                "GREATER_THAN" -> "이상"
                "CROSS_OVER" -> "상향돌파"
                "CROSS_UNDER" -> "하향이탈"
                else -> strategy.condition
            }
            val actionKorean = if (strategy.action == "BUY") "자동 매수" else "자동 매도"
            val actionColor = if (strategy.action == "BUY") Color(0xFFEF4444) else Color(0xFF3B82F6)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "조건: ${strategy.indicator} ${strategy.threshold} $conditionKorean",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "▶ $actionKorean",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = actionColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 벤토 성과 지표 (승률, 총수익률, MDD)
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
                        Text("승률", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${strategy.winRate}%",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
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
                        Text("누적수익률", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "+${strategy.totalReturnRate}%",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = Color(0xFF10B981)
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
                        Text("최대낙폭(MDD)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${strategy.maxDrawdown}%",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onRunBacktest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("과거 30일 시뮬레이션 백테스팅 실행", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun QuantProfitChartCard(result: QuantBacktestResultDto) {
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
                Text(
                    text = "📈 백테스팅 시뮬레이션 수익 곡선 (30일)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "승률 ${result.winRate}% | 총 ${result.tradeCount}회 체결",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas 인터랙티브 손익 곡선 렌더링
            val curve = result.profitCurve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (curve.size >= 2) {
                        val minVal = curve.minOrNull() ?: 0.0
                        val maxVal = curve.maxOrNull() ?: 1.0
                        val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

                        val path = Path()
                        val fillPath = Path()

                        val stepX = size.width / (curve.size - 1)

                        curve.forEachIndexed { index, value ->
                            val x = index * stepX
                            val normalizedY = ((value - minVal) / range).toFloat()
                            val y = size.height - (normalizedY * (size.height - 20f)) - 10f

                            if (index == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, size.height)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                        }

                        fillPath.lineTo(size.width, size.height)
                        fillPath.close()

                        // 채우기 그라데이션
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

                        // 외곽선 라인
                        drawPath(
                            path = path,
                            color = Color(0xFF10B981),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "누적 수익률: +${result.totalReturnRate}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
                Text(
                    text = "최대낙폭(MDD): ${result.maxDrawdown}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun CreateQuantStrategyDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, symbol: String, indicator: String, condition: String, threshold: Double, action: String) -> Unit,
    onPreBacktest: (symbol: String, indicator: String, condition: String, threshold: Double) -> Unit,
    backtestState: UiState<QuantBacktestResultDto>?
) {
    var name by remember { mutableStateOf("자동매매 봇") }
    var symbol by remember { mutableStateOf("WLD") }
    var indicator by remember { mutableStateOf("RSI") }
    var condition by remember { mutableStateOf("LESS_THAN") }
    var thresholdStr by remember { mutableStateOf("30.0") }
    var action by remember { mutableStateOf("BUY") }

    val symbols = listOf("WLD", "NVDA", "TSLA", "AAPL", "MSFT", "BTC")
    val indicators = listOf("RSI", "SMA", "BOLLINGER")
    val conditions = listOf(
        "LESS_THAN" to "미만 (<=)",
        "GREATER_THAN" to "초과 (>=)",
        "CROSS_OVER" to "골든크로스 상향",
        "CROSS_UNDER" to "데드크로스 하향"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("신규 퀀트 알고리즘 봇 빌더", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("봇 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 종목 선택
                Text("대상 종목", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    symbols.take(4).forEach { s ->
                        FilterChip(
                            selected = symbol == s,
                            onClick = { symbol = s },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }

                // 지표 선택
                Text("기술적 지표", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    indicators.forEach { ind ->
                        FilterChip(
                            selected = indicator == ind,
                            onClick = {
                                indicator = ind
                                if (ind == "RSI") thresholdStr = "30.0"
                                else if (ind == "SMA") thresholdStr = "50.0"
                                else thresholdStr = "2.0"
                            },
                            label = { Text(ind, fontSize = 11.sp) }
                        )
                    }
                }

                // 조건 선택
                Text("발동 조건", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    conditions.take(2).forEach { (condKey, condLabel) ->
                        FilterChip(
                            selected = condition == condKey,
                            onClick = { condition = condKey },
                            label = { Text(condLabel, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = thresholdStr,
                    onValueChange = { thresholdStr = it },
                    label = { Text("기준 임계값 (예: RSI 30, SMA 50, 볼린저 2.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 실행 액션
                Text("조건 충족 시 실행 액션", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = action == "BUY",
                        onClick = { action = "BUY" },
                        label = { Text("자동 매수 (BUY)") }
                    )
                    FilterChip(
                        selected = action == "SELL",
                        onClick = { action = "SELL" },
                        label = { Text("자동 매도 (SELL)") }
                    )
                }

                OutlinedButton(
                    onClick = {
                        val th = thresholdStr.toDoubleOrNull() ?: 30.0
                        onPreBacktest(symbol, indicator, condition, th)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("시뮬레이션 사전 백테스팅 실행", fontSize = 12.sp)
                }

                if (backtestState is UiState.Success) {
                    Text(
                        text = "백테스팅 결과: 승률 ${backtestState.data.winRate}% / 수익률 +${backtestState.data.totalReturnRate}%",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val th = thresholdStr.toDoubleOrNull() ?: 30.0
                    onSubmit(name, symbol, indicator, condition, th, action)
                }
            ) {
                Text("전략 등록 및 봇 가동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
