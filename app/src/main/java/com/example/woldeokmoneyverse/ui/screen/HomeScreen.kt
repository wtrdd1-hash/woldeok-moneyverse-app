package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.theme.CardBackgroundDark
import com.example.woldeokmoneyverse.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val profileState by homeViewModel.profileState.collectAsState()
    val walletState by homeViewModel.walletState.collectAsState()
    val progressionState by homeViewModel.progressionState.collectAsState()
    val announcementsState by homeViewModel.announcementsState.collectAsState()

    var showGuideDialog by remember { mutableStateOf(false) }
    var showQuickTransferSheet by remember { mutableStateOf(false) }
    var transferRecipient by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var transferResultMsg by remember { mutableStateOf<String?>(null) }
    var showCharacterStatsDialog by remember { mutableStateOf(false) }
    var showGoldParticle by remember { mutableStateOf(false) }

    val haptic = rememberFintechHaptic()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- 0. 상단 실시간 글래스모피즘 티커 리본 ---
            GlobalFintechTickerRibbon()

            // --- 0.5 RPG 사이버펑크 네온 HUD 바 ---
            GameStatusHudHeader(
                level = (profileState as? UiState.Success)?.data?.level ?: 14,
                currentExp = (profileState as? UiState.Success)?.data?.experience?.toLongOrNull() ?: 68400L,
                maxExp = 100000L,
                tier = "PLATINUM",
                ap = 85,
                maxAp = 100,
                onHudClick = {
                    haptic.click()
                    showCharacterStatsDialog = true
                }
            )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- 1. 정밀 유저 프로필 헤더 바 ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (val pState = profileState) {
                            is UiState.Success -> {
                                val user = pState.data
                                val initial = user.displayName?.firstOrNull()?.toString() ?: "W"
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            initial,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = user.displayName.orEmpty(),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = user.title.orEmpty().ifBlank { "회원" },
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "레벨 ${user.level} · 원장 무결성 검증 완료",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            else -> {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("W", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "월덕 머니버서",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "원장 분석 엔진 가동 중",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 1.5. 📖 3분 머니버스 입문 가이드 배너 ---
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            haptic.click()
                            showGuideDialog = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "📖",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "3분 머니버스 입문 가이드",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "GUIDE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "5단계 로드맵 · 모의 자산 시뮬레이터 · 퀘스트 · 용어사전 →",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- 2. 스마트 핀테크 자산 분석 브리핑 ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "스마트 자산 분석 브리핑",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "건전성 최우수 A+",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "현재 가상 주식 및 사업장 정산 시 포트폴리오 유동성이 강화됩니다. 카지노 등 사행성 리스크가 원천 배제되어 안전한 가상 자산 성장이 유지됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // --- 3. 성장 단계 & EXP 게이지 스트립 ---
            item {
                when (val state = progressionState) {
                    is UiState.Success -> {
                        val p = state.data
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Lv.${p.level} ${p.title.orEmpty().ifBlank { "투자자" }}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${p.currentExp} / ${p.requiredExp} XP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { if (p.requiredExp > 0) (p.currentExp.toFloat() / p.requiredExp.toFloat()).coerceIn(0f, 1f) else 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            // --- 4. 메인 순자산 카드 ---
            item {
                when (val state = walletState) {
                    is UiState.Success -> {
                        val w = state.data
                        BalanceCard(
                            cashBalance = w.cashBalance.orEmpty(),
                            bankBalance = w.bankBalance.orEmpty(),
                            netWorth = w.netWorth.orEmpty(),
                            onTransferClick = {
                                haptic.click()
                                showQuickTransferSheet = true
                            },
                            onBankMoveClick = {
                                haptic.click()
                                onNavigateToTab(1)
                            }
                        )
                    }
                    is UiState.Loading -> SkeletonLoader()
                    is UiState.Error -> ErrorBanner(message = state.message, onRetry = { homeViewModel.loadDashboardData() })
                    else -> {}
                }
            }

            // --- 5. 빠른 원탭 실행 2x2 Bento 그리드 ---
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "빠른 핀테크 실행",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "BENTO HUB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoMetricTile(
                        title = "직업 근무",
                        value = "WLD 급여 수령",
                        subValue = "10초 완료",
                        isPositive = true,
                        onClick = { onNavigateToTab(2) },
                        modifier = Modifier.weight(1f)
                    )
                    BentoMetricTile(
                        title = "간편 송금",
                        value = "원클릭 이체",
                        subValue = "수수료 0 WLD",
                        isPositive = true,
                        onClick = { showQuickTransferSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoMetricTile(
                        title = "주식 거래소",
                        value = "실시간 매매",
                        subValue = "보호환급 적용",
                        isPositive = null,
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f)
                    )
                    BentoMetricTile(
                        title = "일일 출석",
                        value = "보너스 수령",
                        subValue = "자정 리셋",
                        isPositive = true,
                        onClick = { onNavigateToTab(2) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- 6. 머니버스 공지사항 스트립 ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "시스템 공지 & 경제 브리프",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = {
                        haptic.click()
                        onNavigateToTab(3)
                    }) {
                        Text("더보기 →", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            when (val state = announcementsState) {
                is UiState.Success -> {
                    items(state.data.take(3)) { ann ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (ann.isImportant) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "중요",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = ann.title.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                TranslatableText(
                                    text = ann.content.orEmpty(),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 4
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

    // --- 가이드 다이얼로그 ---
    if (showGuideDialog) {
        GuideDialog(
            onDismiss = { showGuideDialog = false },
            onNavigateToTab = { tab ->
                showGuideDialog = false
                onNavigateToTab(tab)
            }
        )
    }

    // --- 간편 송금 모달 바텀시트 ---
    FintechBottomSheetContainer(
        visible = showQuickTransferSheet,
        onDismiss = {
            showQuickTransferSheet = false
            transferResultMsg = null
        },
        title = "⚡ 원클릭 간편 송금",
        subtitle = "수수료 0 WLD · 블록체인 원장 즉시 반영"
    ) {
        OutlinedTextField(
            value = transferRecipient,
            onValueChange = { transferRecipient = it },
            label = { Text("받는 분 이메일 또는 회원 ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = transferAmount,
            onValueChange = { transferAmount = it },
            label = { Text("송금할 금액 (WLD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (transferResultMsg != null) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = transferResultMsg!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        MoneyverseButton(
            text = "즉시 송금하기",
            onClick = {
                haptic.success()
                if (transferRecipient.isNotBlank() && transferAmount.toLongOrNull() != null) {
                    transferResultMsg = "✅ ${transferAmount} WLD 송금이 성공적으로 접수되었습니다."
                } else {
                    transferResultMsg = "⚠️ 수신자 정보와 송금 금액을 올바르게 입력해 주세요."
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showCharacterStatsDialog) {
        CharacterStatsDialog(onDismiss = { showCharacterStatsDialog = false })
    }

    GoldParticleExplosion(
        trigger = showGoldParticle,
        onAnimationEnd = { showGoldParticle = false }
    )
    }
}
