package com.example.woldeokmoneyverse.ui.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideDialog(
    onDismiss: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "머니버스 입문 가이드 허브",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "5단계 로드맵 · 모의 자산 시뮬레이터 · 퀘스트 · 용어사전",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "닫기")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                GuideScreenContent(
                    onNavigateToTab = onNavigateToTab
                )
            }
        }
    }
}

@Composable
fun GuideScreenContent(
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sectionTabs = listOf(
        "🗺️ 5단계 로드맵",
        "🧮 자산 시뮬레이터",
        "✅ 온보딩 퀘스트",
        "📖 금융 용어사전"
    )

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            sectionTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedSection) {
                0 -> OnboardingRoadmapSection(onNavigateToTab = onNavigateToTab)
                1 -> AssetSimulatorSection(onNavigateToTab = onNavigateToTab)
                2 -> OnboardingChecklistSection(onNavigateToTab = onNavigateToTab)
                3 -> GlossarySearchSection(onNavigateToTab = onNavigateToTab)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. 5단계 온보딩 로드맵 (OnboardingRoadmapSection)
// -------------------------------------------------------------

data class RoadmapStepData(
    val stepNumber: Int,
    val badge: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val targetTab: Int,
    val actionLabel: String,
    val checkpoints: List<Pair<String, String>>,
    val previewTitle: String,
    val metricLabel: String,
    val metricValue: String,
    val subLabel: String,
    val subValue: String,
    val highlight: String
)

@Composable
fun OnboardingRoadmapSection(
    onNavigateToTab: (Int) -> Unit
) {
    val steps = remember {
        listOf(
            RoadmapStepData(
                stepNumber = 1,
                badge = "START · 1분 소요",
                title = "계정 로그인 & 첫 출석 체크",
                subtitle = "Discord 또는 Google 간편 로그인 및 초기 시드 WLD 수령",
                description = "별도의 비밀번호 없이 평소 사용하던 계정으로 간편하게 시작할 수 있습니다. 최초 약관 동의 즉시 복식부기 가상 지갑 원장이 개설되며, 매일 출석 체크와 황금 룰렛으로 초기 활동 자금을 안전하게 지원받습니다.",
                targetTab = 2,
                actionLabel = "출석 체크 퀘스트 가기",
                checkpoints = listOf(
                    "간편 로그인 및 1회성 이용약관/개인정보 처리방침 동의" to "동의 즉시 오차 0원의 복식부기 가상 지갑 원장이 개설됩니다.",
                    "퀘스트 탭에서 오늘 출석 완료하고 기본 WLD 수령" to "매일 정기 지원금과 연속 출석 스트릭 보너스가 지급됩니다.",
                    "황금 룰렛 피버로 추가 보너스 티켓 획득" to "연속 출석 달성 시 추가 스타 드롭 보너스 기회가 부여됩니다."
                ),
                previewTitle = "일일 출석 & 피버 현황",
                metricLabel = "출석 완료 기본 보상",
                metricValue = "+1,000 WLD",
                subLabel = "피버 룰렛 보너스",
                subValue = "최대 5,000 WLD",
                highlight = "7일 연속 출석 시 스타 드롭 5연속 탭 기회 제공!"
            ),
            RoadmapStepData(
                stepNumber = 2,
                badge = "CORE · 3분 소요",
                title = "8대 전문 직업 배정 & 첫 일거리",
                subtitle = "광부, 농부, 엔지니어, 트레이더 등 적성 직업 선택 및 급여 수령",
                description = "머니버스 경제의 핵심 생산 엔진인 잡보드에서 내 성향에 맞는 직업을 선택하세요. 작업별 쿨다운 타이머가 적용되며, 숙련도 레벨(Lv.1~5)이 오를수록 일일 최대 4,000만 WLD까지 급여 배수가 대폭 상승합니다.",
                targetTab = 2,
                actionLabel = "잡보드(직업) 바로가기",
                checkpoints = listOf(
                    "잡보드 상단 8대 직업군 중 원하는 분야 자유 선택" to "광부, 농부, 기술자, 트레이더 등 언제든 자유롭게 전직할 수 있습니다.",
                    "작업 수주 후 지정된 쿨다운 시간 충족" to "앱을 닫거나 다른 메뉴를 둘러보아도 백그라운드 타이머가 유지됩니다.",
                    "작업 완료 버튼으로 WLD 급여 및 직업 EXP 즉시 정산" to "숙련도가 오를 때마다 1회 급여와 일일 급여 한도가 함께 상향됩니다."
                ),
                previewTitle = "직업 마스터리 콘솔",
                metricLabel = "1회 작업 기본 급여",
                metricValue = "2,500 ~ 50,000 WLD",
                subLabel = "일일 최대 수령 한도",
                subValue = "40,000,000 WLD",
                highlight = "Lv.5 마스터 달성 시 최대 2.5배 급여 배수 적용!"
            ),
            RoadmapStepData(
                stepNumber = 3,
                badge = "GROWTH · 패시브 수익",
                title = "가상 은행 복리 저축 & 만기 국채",
                subtitle = "수익 WLD를 은행에 예치해 매일 불어나는 일복리 이자와 국채 수익 실현",
                description = "활동으로 번 WLD를 지갑에 그냥 두지 마세요! 가상 은행 복리 정기예금에 넣어두면 매일 0.5% 복리 이자가 원금에 가산되며 언제든 수수료 없이 원클릭 정산할 수 있습니다. 여유 자금은 7일/30일 만기 국채에 안전하게 투자하세요.",
                targetTab = 1,
                actionLabel = "가상 은행 포털 가기",
                checkpoints = listOf(
                    "가상 은행 정기예금에 여유 WLD 예치" to "원금과 누적 이자가 매일 복리로 불어나 자산 증식 속도가 가속됩니다.",
                    "언제든 [누적 이자 정산]으로 지갑에 즉시 반영" to "중도 인출 수수료가 0원이므로 필요할 때 즉시 전액 인출 가능합니다.",
                    "7일 / 30일 만기 가상 국채 가입으로 확정 고수익 확보" to "국고가 원리금을 100% 보증 지급하여 시장 하락장에서도 안전합니다."
                ),
                previewTitle = "은행 복리 예금 & 국채 현황",
                metricLabel = "일일 복리 기준 이율",
                metricValue = "일 0.5% (연환산 약 20%)",
                subLabel = "30일 만기 국채 수익률",
                subValue = "확정 +8.5%",
                highlight = "복리 효과로 1년 예치 시 원금 대비 약 1.3배 이상 자동 증식!"
            ),
            RoadmapStepData(
                stepNumber = 4,
                badge = "EXPANSION · 고수익 투자",
                title = "가상 주식 매매 & 호가창 분석",
                subtitle = "10대 상장사 주식을 실시간 호가로 매매하고 일일 기업 배당금 획득",
                description = "월덕거래소에서는 10대 가상 상장사(월덕테크, 월덱파이낸셜, 칩스엔터 등)의 실시간 틱 차트와 10-Depth 호가창을 통해 전문 트레이딩을 즐길 수 있습니다. AI 경제 신문의 시장 감성 지표를 확인하여 저평가주를 선점하세요.",
                targetTab = 1,
                actionLabel = "월덕거래소 바로가기",
                checkpoints = listOf(
                    "10대 상장 종목(WDT, WFIN, CHIPS, SPACE 등) 차트 분석" to "AI 경제 신문의 호재/악재 기사가 실시간 주가에 직접 반영됩니다.",
                    "10-Depth 호가창에서 지정가 / 시장가 주문 실행" to "매수/매도 잔량 압력 게이지를 통해 지지선과 저항선을 파악할 수 있습니다.",
                    "보유 주식 포트폴리오를 구성하고 일일 기업 배당 수령" to "주식을 보유하기만 해도 매일 자정에 기업 이익의 일부가 배당됩니다."
                ),
                previewTitle = "월덕거래소 라이브 콘솔",
                metricLabel = "시장 평균 연간 배당률",
                metricValue = "8.5% ~ 14.2%",
                subLabel = "실시간 호가 Depth",
                subValue = "10-Depth 틱 스트림",
                highlight = "AI 신문 호재 발생 시 600ms 플래시 펄스 상승 랠리 전개!"
            ),
            RoadmapStepData(
                stepNumber = 5,
                badge = "MOGUL · 최종 도약",
                title = "기업 창업 & 클럽/협동조합 영지",
                subtitle = "가상 사업체를 직접 설립해 대표가 되거나 클럽 협동 펀딩으로 영지 개척",
                description = "축적한 자본으로 나만의 스타트업이나 법인을 설립해 매일 법인 배당금을 챙기는 머니버스의 대표 자본가가 되어보세요. 뜻이 맞는 동료들과 클럽을 창설하여 영지 공성전과 대규모 협동 프로젝트에 도전할 수 있습니다.",
                targetTab = 1,
                actionLabel = "게임 사업 둘러보기",
                checkpoints = listOf(
                    "마이비즈에서 시드 투자 후 나만의 법인 창업" to "소프트웨어, F&B, 물류, 콘텐츠 등 다양한 업종의 사업체를 운영합니다.",
                    "매일 원클릭 [수익 일괄 정산]으로 기업 운영 이익 회수" to "상점에서 사업 부스트 아이템을 적용하면 일일 매출이 20~50% 폭증합니다.",
                    "클럽을 창설하여 길드 영지 쟁탈전 참전" to "클럽원들과 협동 펀딩을 달성하면 특별 클럽 배당 혜택이 주어집니다."
                ),
                previewTitle = "엔터프라이즈 & 클럽 대시보드",
                metricLabel = "사업체 일일 패시브 매출",
                metricValue = "+150,000 WLD/일",
                subLabel = "클럽 협동 펀딩 배당",
                subValue = "추가 +15% 버프",
                highlight = "시즌 랭킹 상위권 진입 시 명예의 전당 등재 및 한정판 트로피 지급!"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "5단계 완성형 온보딩 로드맵",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "초기 가입부터 자본가 설립까지 핵심 성공 경로를 단계별로 안내합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(steps) { step ->
            RoadmapStepCard(step = step, onActionClick = { onNavigateToTab(step.targetTab) })
        }
    }
}

@Composable
fun RoadmapStepCard(
    step: RoadmapStepData,
    onActionClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "STEP ${step.stepNumber} · ${step.badge}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = step.subtitle,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 체크포인트 팁 리스트
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                step.checkpoints.forEach { (label, tip) ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 미리보기 메트릭 카드
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = step.previewTitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = step.metricLabel, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = step.metricValue,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = step.subLabel, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = step.subValue,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💡 ${step.highlight}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onActionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "${step.actionLabel} →", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 2. 1분 모의 자산 형성 시뮬레이터 (AssetSimulatorSection)
// -------------------------------------------------------------

@Composable
fun AssetSimulatorSection(
    onNavigateToTab: (Int) -> Unit
) {
    var dailySalary by remember { mutableLongStateOf(50000L) }
    var bankAllocation by remember { mutableFloatStateOf(50f) }
    var periodDays by remember { mutableIntStateOf(30) }

    val stockAllocation = 100f - bankAllocation

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    // 시뮬레이션 계산
    val simulationResult = remember(dailySalary, bankAllocation, periodDays) {
        val dailyRateBank = 0.005
        val dailyRateStock = 0.12 / 365.0

        var bankTotal = 0.0
        var stockTotal = 0.0
        var totalWages = 0.0

        for (day in 1..periodDays) {
            totalWages += dailySalary
            val bankDeposit = dailySalary * (bankAllocation / 100.0)
            val stockInvest = dailySalary * (stockAllocation / 100.0)

            bankTotal = (bankTotal + bankDeposit) * (1.0 + dailyRateBank)
            stockTotal = (stockTotal + stockInvest) * (1.0 + dailyRateStock)
        }

        val netWorth = Math.round(bankTotal + stockTotal)
        val bankPrincipal = totalWages * (bankAllocation / 100.0)
        val stockPrincipal = totalWages * (stockAllocation / 100.0)
        val bankProfit = Math.max(0L, Math.round(bankTotal - bankPrincipal))
        val stockProfit = Math.max(0L, Math.round(stockTotal - stockPrincipal))
        val totalPassiveProfit = bankProfit + stockProfit
        val dailyPassiveIncome = Math.round((bankTotal * dailyRateBank) + (stockTotal * dailyRateStock))
        val profitPercentage = if (totalWages > 0) ((totalPassiveProfit.toDouble() / totalWages) * 100.0) else 0.0

        SimulationOutput(
            netWorth = netWorth,
            totalWages = totalWages.toLong(),
            bankTotal = bankTotal.toLong(),
            stockTotal = stockTotal.toLong(),
            bankProfit = bankProfit,
            stockProfit = stockProfit,
            totalPassiveProfit = totalPassiveProfit,
            dailyPassiveIncome = dailyPassiveIncome,
            profitPercentage = String.format(Locale.US, "%.1f", profitPercentage)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1분 모의 자산 형성 시뮬레이터",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "직업 급여, 은행 일복리 예금(0.5%), 주식 배당(연 12%)을 조합했을 때 자산이 어떻게 불어나는지 슬라이더로 직접 확인해 보세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 입력 컨트롤 카드
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 1. 일일 급여
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "예상 일일 직업 급여",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${numberFormat.format(dailySalary)} WLD",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = dailySalary.toFloat(),
                        onValueChange = { dailySalary = it.toLong() },
                        valueRange = 10000f..40000000f,
                        steps = 39,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 프리셋 칩
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val presets = listOf(
                            "입문 1만" to 10000L,
                            "일반 5만" to 50000L,
                            "전문 50만" to 500000L,
                            "마스터 500만" to 5000000L,
                            "최대 4,000만" to 40000000L
                        )
                        items(presets) { (label, value) ->
                            FilterChip(
                                selected = dailySalary == value,
                                onClick = { dailySalary = value },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. 자산 배분 (은행 vs 주식)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "자산 배분 비율",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "은행 ${bankAllocation.toInt()}% : 주식 ${stockAllocation.toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = bankAllocation,
                        onValueChange = { bankAllocation = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("안전 일복리 예금", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("고수익 기업 배당", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. 시뮬레이션 기간
                    Text(
                        text = "시뮬레이션 기간",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val periods = listOf(
                            7 to "7일 (1주)",
                            30 to "30일 (1개월)",
                            90 to "90일 (3개월)",
                            365 to "365일 (1년)"
                        )
                        periods.forEach { (days, label) ->
                            FilterChip(
                                selected = periodDays == days,
                                onClick = { periodDays = days },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 결과 리포트 카드
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 ${periodDays}일 후 예상 자산 시뮬레이션 결과",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 총 순자산 하이라이트
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "예상 최종 순자산 (Net Worth)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${numberFormat.format(simulationResult.netWorth)} WLD",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "총 누적 근로 급여: ${numberFormat.format(simulationResult.totalWages)} WLD",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 복리/배당 추가 패시브 수익
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("이자/배당 순수익", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "+${numberFormat.format(simulationResult.totalPassiveProfit)}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text("+${simulationResult.profitPercentage}% 증식", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("일일 패시브 소득", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "+${numberFormat.format(simulationResult.dailyPassiveIncome)}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("WLD / 매일 자동 발생", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 포트폴리오 잔고 분배 바
                    Text(
                        text = "자산 포트폴리오 구성: 은행 ${numberFormat.format(simulationResult.bankTotal)} WLD vs 주식 ${numberFormat.format(simulationResult.stockTotal)} WLD",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (bankAllocation / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToTab(1) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("은행 예치하기")
                        }
                        Button(
                            onClick = { onNavigateToTab(1) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("주식 투자하기")
                        }
                    }
                }
            }
        }
    }
}

data class SimulationOutput(
    val netWorth: Long,
    val totalWages: Long,
    val bankTotal: Long,
    val stockTotal: Long,
    val bankProfit: Long,
    val stockProfit: Long,
    val totalPassiveProfit: Long,
    val dailyPassiveIncome: Long,
    val profitPercentage: String
)

// -------------------------------------------------------------
// 3. 온보딩 퀘스트 체크리스트 (OnboardingChecklistSection)
// -------------------------------------------------------------

data class ChecklistItemData(
    val id: String,
    val title: String,
    val desc: String,
    val rewardText: String,
    val targetTab: Int
)

@Composable
fun OnboardingChecklistSection(
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val pref = remember { context.getSharedPreferences("moneyverse_onboarding_checklist_v1", Context.MODE_PRIVATE) }

    val items = remember {
        listOf(
            ChecklistItemData(
                id = "task_login",
                title = "계정 로그인 & 약관 동의",
                desc = "OAuth 간편 로그인 후 복식부기 가상 지갑 원장을 개설합니다.",
                rewardText = "지갑 원장 개설",
                targetTab = 4
            ),
            ChecklistItemData(
                id = "task_quest",
                title = "첫 출석 체크 & 일일 퀘스트",
                desc = "퀘스트 메뉴에서 오늘 출석을 마치고 시드 WLD를 수령합니다.",
                rewardText = "+1,000 WLD",
                targetTab = 2
            ),
            ChecklistItemData(
                id = "task_work",
                title = "8대 직업 선택 & 첫 일거리 완수",
                desc = "잡보드에서 원하는 직업을 고르고 첫 작업을 마쳐 급여와 EXP를 받습니다.",
                rewardText = "직업 EXP & 급여",
                targetTab = 2
            ),
            ChecklistItemData(
                id = "task_bank",
                title = "가상 은행 복리 예금 1회 예치",
                desc = "수령한 WLD를 은행에 예치해 매일 불어나는 일복리 이자를 시작합니다.",
                rewardText = "일복리 이자 가동",
                targetTab = 1
            ),
            ChecklistItemData(
                id = "task_stocks",
                title = "가상 주식 10-Depth 호가창 조회",
                desc = "월덕거래소에서 10대 가상 상장사의 실시간 차트와 호가 잔량을 확인합니다.",
                rewardText = "시장 분석 지식",
                targetTab = 1
            ),
            ChecklistItemData(
                id = "task_glossary",
                title = "핀테크 핵심 금융 용어사전 열람",
                desc = "복식부기, 멱등성, 스프레드 등 머니버스 핵심 메커니즘을 익힙니다.",
                rewardText = "온보딩 지식 습득",
                targetTab = 0
            )
        )
    }

    var completedMap by remember {
        mutableStateOf(
            items.associate { it.id to pref.getBoolean(it.id, false) }
        )
    }

    val completedCount = completedMap.values.count { it }
    val progress = if (items.isNotEmpty()) completedCount.toFloat() / items.size.toFloat() else 0f
    val isAllCompleted = completedCount == items.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 프로그레스 현황 바
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "온보딩 퀘스트 진행도",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$completedCount / ${items.size} (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (isAllCompleted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🏆 머니버스 마스터! 모든 온보딩 퀘스트를 완료하셨습니다.",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        items(items) { item ->
            val isChecked = completedMap[item.id] == true
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        val newChecked = !isChecked
                        completedMap = completedMap.toMutableMap().apply { put(item.id, newChecked) }
                        pref.edit().putBoolean(item.id, newChecked).apply()
                    },
                shape = RoundedCornerShape(14.dp),
                color = if (isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            completedMap = completedMap.toMutableMap().apply { put(item.id, checked) }
                            pref.edit().putBoolean(item.id, checked).apply()
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.rewardText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(onClick = { onNavigateToTab(item.targetTab) }) {
                        Text("이동 →", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. 핀테크 핵심 용어 사전 (GlossarySearchSection)
// -------------------------------------------------------------

data class GlossaryEntry(
    val id: String,
    val termKo: String,
    val termEn: String,
    val category: String,
    val summary: String,
    val detail: String,
    val targetTab: Int,
    val linkText: String
)

@Composable
fun GlossarySearchSection(
    onNavigateToTab: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }

    val entries = remember {
        listOf(
            GlossaryEntry(
                id = "double-entry",
                termKo = "복식부기 원장 (Double-Entry Ledger)",
                termEn = "Double-Entry Ledger",
                category = "finance",
                summary = "차변(Debit)과 대변(Credit)을 항상 100% 일치시켜 재화 유실을 원천 차단하는 회계 시스템.",
                detail = "머니버스의 모든 재화 변동(직업 급여, 상점 구매, 이자 지급, 주식 매매)은 두 개 이상의 계정 간에 동일한 금액으로 기록되어, 서버 장애나 동시 요청에서도 WLD가 공중에서 증발하거나 중복 생성되지 않습니다.",
                targetTab = 4,
                linkText = "원장 기록 보기"
            ),
            GlossaryEntry(
                id = "idempotency",
                termKo = "멱등성 (Idempotency)",
                termEn = "Idempotent Transactions",
                category = "system",
                summary = "네트워크 재시도로 동일한 요청이 여러 번 전송되어도 정확히 단 1회만 처리되는 통신 안전장치.",
                detail = "모바일 환경에서 인터넷이 끊겨 송금이나 결제 버튼을 여러 번 누르더라도, 클라이언트가 발행한 고유 Idempotency-Key를 통해 단 1건의 트랜잭션만 실행되고 중복 인출이 완벽히 방지됩니다.",
                targetTab = 0,
                linkText = "홈으로 이동"
            ),
            GlossaryEntry(
                id = "orderbook-10d",
                termKo = "10-Depth 실시간 호가창",
                termEn = "10-Depth Orderbook",
                category = "finance",
                summary = "최우선 10단계 매수/매도 주문 잔량과 가격을 실시간으로 시각화한 가상 주식 거래소 콘솔.",
                detail = "월덕거래소에서 10대 가상 상장사의 실시간 매수/매도 압력 비율과 스프레드를 확인하고 지정가 및 시장가 주문을 초고속으로 실행할 수 있습니다.",
                targetTab = 1,
                linkText = "거래소 바로가기"
            ),
            GlossaryEntry(
                id = "spread",
                termKo = "스프레드 (Bid-Ask Spread)",
                termEn = "Bid-Ask Spread",
                category = "finance",
                summary = "최우선 매도 호가(Ask)와 최우선 매수 호가(Bid) 간의 가격 차이.",
                detail = "스프레드가 좁을수록 거래 유동성이 풍부하여 원하는 가격에 즉시 체결하기 유리하며, 스프레드가 넓을 때는 지정가 주문을 활용해 유리한 가격을 선점하는 것이 좋습니다.",
                targetTab = 1,
                linkText = "호가창 확인"
            ),
            GlossaryEntry(
                id = "daily-compound",
                termKo = "일일 복리 이자 (Daily Compounding)",
                termEn = "Daily Compounding Interest",
                category = "finance",
                summary = "매일 발생한 이자가 익일 원금에 자동 합산되어 자산이 기하급수적으로 불어나는 저축 구조.",
                detail = "가상 은행 복리 예금은 일 단위 0.5% 복리 이자가 누적되며, 사용자가 [누적 이자 정산]을 누르면 원장에 즉시 확정 반영됩니다.",
                targetTab = 1,
                linkText = "가상 은행 가기"
            ),
            GlossaryEntry(
                id = "treasury-bonds",
                termKo = "가상 국채 (Treasury Bonds)",
                termEn = "Virtual Treasury Bonds",
                category = "finance",
                summary = "7일 또는 30일 만기 시 국고가 확정 고수익 이자를 100% 보증 지급하는 금융 상품.",
                detail = "시장 주가 변동 리스크를 회피하고 정해진 만기일에 높은 고정 수익률을 안정적으로 거두고자 할 때 최적의 안전 자산입니다.",
                targetTab = 1,
                linkText = "국채 상품 보기"
            ),
            GlossaryEntry(
                id = "mastery-exp",
                termKo = "직업 숙련도 (Career Mastery)",
                termEn = "Career Mastery",
                category = "work",
                summary = "잡보드 작업을 완수할 때마다 누적되는 직업 전문성 레벨.",
                detail = "숙련도 레벨이 오르면 1회 작업당 급여와 일일 최대 수령 한도(최대 4,000만 WLD)가 함께 비례하여 상승합니다.",
                targetTab = 2,
                linkText = "잡보드 가기"
            ),
            GlossaryEntry(
                id = "sentiment-index",
                termKo = "AI 시장 감성 지표 (Market Sentiment)",
                termEn = "Market Sentiment",
                category = "finance",
                summary = "AI 경제 신문 기사의 호재/악재를 분석해 시장 참여자들의 탐욕과 공포를 수치화한 지표.",
                detail = "감성 지표가 극단적 공포(Extreme Fear)일 때는 저평가 분할 매수 기회가, 극단적 탐욕(Extreme Greed)일 때는 차익 실현 기회가 됩니다.",
                targetTab = 1,
                linkText = "신문 & 거래소 보기"
            )
        )
    }

    val filteredEntries = remember(searchQuery, selectedCategory) {
        entries.filter { entry ->
            val matchCategory = selectedCategory == "all" || entry.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                    entry.termKo.contains(searchQuery, ignoreCase = true) ||
                    entry.termEn.contains(searchQuery, ignoreCase = true) ||
                    entry.summary.contains(searchQuery, ignoreCase = true) ||
                    entry.detail.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 검색 필드
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("용어 또는 키워드 검색 (예: 복식부기, 호가, 멱등성)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "지우기")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 카테고리 필터 칩
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = listOf(
                    "all" to "전체",
                    "finance" to "금융·투자",
                    "work" to "직업·경영",
                    "system" to "시스템·보안"
                )
                items(categories) { (catKey, catLabel) ->
                    FilterChip(
                        selected = selectedCategory == catKey,
                        onClick = { selectedCategory = catKey },
                        label = { Text(catLabel, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        item {
            Text(
                text = "총 ${filteredEntries.size}개 핵심 용어",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filteredEntries) { entry ->
            var expanded by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.termKo,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = entry.detail,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onNavigateToTab(entry.targetTab) }) {
                                Text("${entry.linkText} →", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
