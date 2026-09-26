package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woldeokmoneyverse.data.model.LoanDto
import com.example.woldeokmoneyverse.data.model.SavingPocketDto
import com.example.woldeokmoneyverse.data.model.ShopHoldingDto
import com.example.woldeokmoneyverse.data.model.StockDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.EconomyViewModel
import com.example.woldeokmoneyverse.ui.viewmodel.ShopSearchViewModel
import com.example.woldeokmoneyverse.util.formatWld
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

private fun formatPercent(value: Double): String = String.format(Locale.getDefault(), "%.2f%%", kotlin.math.abs(value))
private fun decimalValue(value: String?): BigDecimal = runCatching { BigDecimal(value?.replace(",", "") ?: "0") }.getOrElse { BigDecimal.ZERO }
private fun wholeAmount(value: String?): String = decimalValue(value).max(BigDecimal.ZERO).setScale(0, RoundingMode.DOWN).toPlainString()
private fun visibleError(message: String): String {
    val trimmed = message.trim()
    if (Regex("[1-5]\\d{2}").matches(trimmed)) return trimmed
    val match = Regex("(?:HTTP\\s*)?([1-5]\\d{2})(?!\\d)").find(trimmed)
    return if (match != null && (trimmed.contains("실패") || trimmed.contains("오류") || trimmed.contains("HTTP"))) match.groupValues[1] else trimmed
}
private fun maxStockQuantity(balance: String, price: String): Int {
    val cash = decimalValue(balance)
    val unit = decimalValue(price)
    if (cash <= BigDecimal.ZERO || unit <= BigDecimal.ZERO) return 0
    return cash.divideToIntegralValue(unit).min(BigDecimal.valueOf(Int.MAX_VALUE.toLong())).toInt()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EconomyScreen(economyViewModel: EconomyViewModel) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("지갑/은행", "주식", "사업", "상점")
    var showTransferDialog by remember { mutableStateOf(false) }
    var showBankDialog by remember { mutableStateOf(false) }
    var showLoanDialog by remember { mutableStateOf(false) }
    var showBondPurchaseDialog by remember { mutableStateOf<String?>(null) }
    var selectedStockForSheet by remember { mutableStateOf<StockDto?>(null) }

    // --- v8: Saving Pockets Dialog States ---
    var showCreatePocketDialog by remember { mutableStateOf(false) }
    var selectedPocketForMovement by remember { mutableStateOf<Pair<SavingPocketDto, String>?>(null) }
    var selectedPocketForTheme by remember { mutableStateOf<SavingPocketDto?>(null) }
    var selectedPocketForArchive by remember { mutableStateOf<SavingPocketDto?>(null) }

    val actionMessage by economyViewModel.actionMessage.collectAsState()
    val walletState by economyViewModel.walletState.collectAsState()
    val loansState by economyViewModel.loansState.collectAsState()
    val portfolioState by economyViewModel.portfolioState.collectAsState()
    val standingState by economyViewModel.standingState.collectAsState()
    val candlesState by economyViewModel.candlesState.collectAsState()
    val watchlistState by economyViewModel.watchlistState.collectAsState()
    val holdingsState by economyViewModel.holdingsState.collectAsState()
    val savingPocketsState by economyViewModel.savingPocketsState.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }

    val cashBalance = (walletState as? UiState.Success)?.data?.cashBalance ?: "0"
    val bankBalance = (walletState as? UiState.Success)?.data?.bankBalance ?: "0"
    val loans = (loansState as? UiState.Success)?.data.orEmpty()

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(visibleError(it))
            economyViewModel.clearActionMessage()
        }
    }

    LaunchedEffect(selectedStockForSheet) {
        selectedStockForSheet?.let { stock ->
            economyViewModel.loadCandles(stock.id, "86400")
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MoneyverseSubTabRow(tabs = subTabs, selectedTabIndex = selectedSubTab, onTabSelected = { selectedSubTab = it })
            when (selectedSubTab) {
                0 -> WalletBankSubTab(
                    economyViewModel = economyViewModel,
                    onOpenTransfer = { showTransferDialog = true },
                    onOpenBank = { showBankDialog = true },
                    onOpenLoan = { showLoanDialog = true },
                    onOpenBondPurchase = { bondCode -> showBondPurchaseDialog = bondCode },
                    onOpenCreatePocket = { showCreatePocketDialog = true },
                    onOpenPocketMovement = { pocket, direction -> selectedPocketForMovement = Pair(pocket, direction) },
                    onOpenPocketTheme = { pocket -> selectedPocketForTheme = pocket },
                    onOpenPocketArchive = { pocket -> selectedPocketForArchive = pocket }
                )
                1 -> StocksSubTab(
                    economyViewModel = economyViewModel,
                    onSelectStock = { selectedStockForSheet = it }
                )
                2 -> BusinessSubTab(economyViewModel)
                3 -> ShopSubTab(economyViewModel)
            }
        }
    }

    if (showTransferDialog) {
        TransferDialog(
            cashBalance = cashBalance,
            onDismiss = { showTransferDialog = false },
            onConfirm = { recipient, amount, memo ->
                economyViewModel.transferMoney(recipient, amount, memo)
                showTransferDialog = false
            }
        )
    }
    if (showBankDialog) {
        BankDialog(
            cashBalance = cashBalance,
            bankBalance = bankBalance,
            onDismiss = { showBankDialog = false },
            onConfirm = { direction, amount ->
                economyViewModel.bankMove(direction, amount)
                showBankDialog = false
            }
        )
    }
    if (showLoanDialog) {
        LoanDialog(
            cashBalance = cashBalance,
            loans = loans,
            onDismiss = { showLoanDialog = false },
            onBorrow = { amount -> economyViewModel.borrowLoan(amount); showLoanDialog = false },
            onRepay = { loanId, amount -> economyViewModel.repayLoan(loanId, amount); showLoanDialog = false },
            onApplySmartLoan = { amount -> economyViewModel.applySmartLoan(amount); showLoanDialog = false },
            onRepaySmartLoan = { amount -> economyViewModel.repaySmartLoan(amount); showLoanDialog = false }
        )
    }
    showBondPurchaseDialog?.let { bondCode ->
        BondPurchaseDialog(
            bondCode = bondCode,
            cashBalance = cashBalance,
            onDismiss = { showBondPurchaseDialog = null },
            onConfirm = { code, amount ->
                economyViewModel.purchaseBond(code, amount)
                showBondPurchaseDialog = null
            }
        )
    }

    // --- v8: Saving Pockets Dialogs ---
    if (showCreatePocketDialog) {
        CreateSavingPocketDialog(
            onDismiss = { showCreatePocketDialog = false },
            onConfirm = { name, targetAmount, targetDate, themeColor ->
                economyViewModel.createSavingPocket(name, targetAmount, targetDate.orEmpty(), themeColor)
                showCreatePocketDialog = false
            }
        )
    }

    selectedPocketForMovement?.let { (pocket, direction) ->
        PocketMovementDialog(
            pocket = pocket,
            direction = direction,
            cashBalance = cashBalance,
            onDismiss = { selectedPocketForMovement = null },
            onConfirm = { amount ->
                economyViewModel.movePocketMoney(pocket.id, direction, amount)
                selectedPocketForMovement = null
            }
        )
    }

    selectedPocketForTheme?.let { pocket ->
        PocketThemeDialog(
            pocket = pocket,
            onDismiss = { selectedPocketForTheme = null },
            onConfirm = { newTheme ->
                economyViewModel.updatePocketTheme(pocket.id, newTheme)
                selectedPocketForTheme = null
            }
        )
    }

    selectedPocketForArchive?.let { pocket ->
        ArchivePocketConfirmDialog(
            pocket = pocket,
            onDismiss = { selectedPocketForArchive = null },
            onConfirm = {
                economyViewModel.archivePocket(pocket.id)
                selectedPocketForArchive = null
            }
        )
    }

    selectedStockForSheet?.let { stock ->

        val holding = (portfolioState as? UiState.Success)?.data?.holdings?.firstOrNull { it.stockId == stock.id }
        val candles = (candlesState as? UiState.Success)?.data.orEmpty()
        val isWatchlist = (watchlistState as? UiState.Success)?.data?.any { it.stockId == stock.id } ?: false

        StockDetailSheet(
            stock = stock,
            candles = candles,
            isWatchlist = isWatchlist,
            maxBuyQuantity = maxStockQuantity(cashBalance, stock.currentPrice),
            maxSellQuantity = holding?.quantity ?: 0,
            onDismiss = { selectedStockForSheet = null },
            onOrder = { orderType, qty -> economyViewModel.orderStock(stock.id, orderType, qty) },
            onToggleWatchlist = { economyViewModel.toggleWatchlist(stock.id) },
            onCreateAlert = { condition, threshold -> economyViewModel.createAlert(stock.id, condition, threshold) }
        )
    }
}

@Composable
fun WalletBankSubTab(
    economyViewModel: EconomyViewModel,
    onOpenTransfer: () -> Unit,
    onOpenBank: () -> Unit,
    onOpenLoan: () -> Unit,
    onOpenBondPurchase: (String) -> Unit,
    onOpenCreatePocket: () -> Unit,
    onOpenPocketMovement: (SavingPocketDto, String) -> Unit,
    onOpenPocketTheme: (SavingPocketDto) -> Unit,
    onOpenPocketArchive: (SavingPocketDto) -> Unit
) {
    val walletState by economyViewModel.walletState.collectAsState()
    val loansState by economyViewModel.loansState.collectAsState()
    val standingState by economyViewModel.standingState.collectAsState()
    val savingPocketsState by economyViewModel.savingPocketsState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            when (val state = walletState) {
                is UiState.Success -> {
                    val w = state.data
                    BalanceCard(
                        cashBalance = w.cashBalance,
                        bankBalance = w.bankBalance,
                        netWorth = w.netWorth,
                        onTransferClick = onOpenTransfer,
                        onBankMoveClick = onOpenBank
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 🎁 복리 예금 이자 수령 카드
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("🏦 복리 예금 이자", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("주간 발생 복리 이자를 즉시 유동 지갑으로 수령합니다.", style = MaterialTheme.typography.bodySmall)
                            }
                            Button(
                                onClick = { economyViewModel.claimInterest() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("이자 받기")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🎯 저축·목표 포켓 (Saving Pockets) 카드 섹션
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🎯 저축·목표 포켓 (Saving Pockets)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("수수료 0원 입출금 · 테마 꾸미기 · 목표 달성 아카이브", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = onOpenCreatePocket,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ 새 포켓")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    when (val pState = savingPocketsState) {
                        is UiState.Success -> {
                            val pockets = pState.data
                            if (pockets.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("아직 생성된 저축 포켓이 없습니다.", style = MaterialTheme.typography.bodyMedium)
                                        Text("목표(예: 내 집 마련, 비상금)를 정하고 자금을 안전하게 모아보세요!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else {
                                pockets.forEach { pocket ->
                                    val currentD = decimalValue(pocket.currentAmount)
                                    val targetD = decimalValue(pocket.targetAmount)
                                    val progressPct = if (targetD.signum() <= 0) 100 else currentD.multiply(BigDecimal(100)).divide(targetD, 0, RoundingMode.DOWN).toInt().coerceIn(0, 100)
                                    val themeBgColor = when (pocket.themeColor.uppercase()) {
                                        "SKY" -> Color(0xFFE0F2FE)
                                        "GOLD" -> Color(0xFFFEF3C7)
                                        "EMERALD" -> Color(0xFFD1FAE5)
                                        else -> Color(0xFFCCFBF1) // MINT
                                    }
                                    val themeBorderColor = when (pocket.themeColor.uppercase()) {
                                        "SKY" -> Color(0xFF0284C7)
                                        "GOLD" -> Color(0xFFD97706)
                                        "EMERALD" -> Color(0xFF059669)
                                        else -> Color(0xFF0D9488) // MINT
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = themeBgColor),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, themeBorderColor)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("🎯", style = MaterialTheme.typography.titleMedium)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(pocket.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF0F172A))
                                                    if (pocket.isArchived) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(color = Color(0xFF475569), shape = RoundedCornerShape(4.dp)) {
                                                            Text("아카이브 완료", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
                                                        }
                                                    }
                                                }
                                                Text("$progressPct%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = themeBorderColor))
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { progressPct / 100f },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                color = themeBorderColor,
                                                trackColor = Color.White.copy(alpha = 0.6f)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("현재: ${formatWld(pocket.currentAmount)} WLD", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)))
                                                Text("목표: ${formatWld(pocket.targetAmount)} WLD", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                                            }

                                            if (!pocket.isArchived) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = { onOpenPocketMovement(pocket, "DEPOSIT") },
                                                        modifier = Modifier.weight(1f).height(38.dp),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("입금 (0원)", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { onOpenPocketMovement(pocket, "WITHDRAW") },
                                                        modifier = Modifier.weight(1f).height(38.dp),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("출금 (0원)", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    IconButton(
                                                        onClick = { onOpenPocketTheme(pocket) },
                                                        modifier = Modifier.size(38.dp)
                                                    ) {
                                                        Text("🎨", style = MaterialTheme.typography.bodySmall)
                                                    }
                                                    if (progressPct >= 100) {
                                                        Button(
                                                            onClick = { onOpenPocketArchive(pocket) },
                                                            modifier = Modifier.height(38.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                                                        ) {
                                                            Text("아카이브", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(8.dp))
                        else -> Unit
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("📜 가상 국채 카탈로그 (Bonds)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("만기 확정 연이율(APR)을 지급하며, 만기 전 중도 환매도 가능합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(8.dp))

                    val bondCatalog = listOf(
                        Triple("BOND_7D", "단기 7일 국채", "확정 연이율 8.5% · 만기 7일"),
                        Triple("BOND_30D", "중기 30일 국채", "확정 연이율 12.0% · 만기 30일"),
                        Triple("BOND_90D", "장기 90일 국채", "확정 연이율 18.0% · 만기 90일")
                    )

                    bondCatalog.forEach { (code, title, desc) ->
                        MoneyverseCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Button(
                                    onClick = { onOpenBondPurchase(code) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("매수하기")
                                }
                            }
                        }
                    }

                    // 보유 채권 목록 (Standing State)
                    val activeBonds = (standingState as? UiState.Success)?.data?.activeBonds.orEmpty()
                    if (activeBonds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("📜 내 보유 국채 목록 (${activeBonds.size}건)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        activeBonds.forEach { bond ->
                            MoneyverseCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${bond.bondCode} (${formatWld(bond.amount)})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("만기일: ${bond.maturityAt ?: "정상 운용 중"}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(
                                        onClick = { economyViewModel.redeemBond(bond.id) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("상환/환매")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    MoneyverseSecondaryButton(text = "🏛️ 스마트 대출 신청 및 상환 관리", onClick = onOpenLoan, modifier = Modifier.fillMaxWidth())

                    when (val loans = loansState) {
                        is UiState.Success -> if (loans.data.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("🏛️ 내 대출 현황", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            loans.data.forEach { loan ->
                                MoneyverseCard {
                                    Text("대출 #${loan.id}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("잔액 ${formatWld(loan.remainingBalance)} · 이율 ${loan.interestRate}%", style = MaterialTheme.typography.bodySmall)
                                    Text("상환일 ${loan.dueDate} · ${loan.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        is UiState.Error -> ErrorBanner(message = visibleError(loans.message), onRetry = { economyViewModel.loadWallet() })
                        else -> Unit
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("📜 최근 원장 거래 기록", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    w.recentLedger.forEach { tx ->
                        MoneyverseCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(tx.description, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(tx.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = formatWld(tx.amount),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (tx.amount.startsWith("+")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                is UiState.Loading -> SkeletonLoader()
                is UiState.Error -> ErrorBanner(message = visibleError(state.message), onRetry = { economyViewModel.loadWallet() })
                else -> Unit
            }
        }
    }
}

@Composable
fun StocksSubTab(economyViewModel: EconomyViewModel, onSelectStock: (StockDto) -> Unit) {
    val stocksState by economyViewModel.stocksState.collectAsState()
    val portfolioState by economyViewModel.portfolioState.collectAsState()
    val watchlistState by economyViewModel.watchlistState.collectAsState()
    val marketEventsState by economyViewModel.marketEventsState.collectAsState()
    val stockTradesHistoryState by economyViewModel.stockTradesHistoryState.collectAsState()
    val realtimeConnected by economyViewModel.marketRealtimeConnected.collectAsState()

    var filterWatchlistOnly by remember { mutableStateOf(false) }
    val watchlistIds = (watchlistState as? UiState.Success)?.data?.map { it.stockId }?.toSet() ?: emptySet()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛡️", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "거래정지(HALTED) 투자자 보호 제도",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "거래정지 종목 발생 시 보유 주식은 매수원가(Cost Basis) 100%로 자동 전액 환급되며 거래 수수료 및 거래세는 0% 전액 면제됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            when (val eventsState = marketEventsState) {
                is UiState.Success -> {
                    val events = eventsState.data
                    if (events.isNotEmpty()) {
                        events.forEach { event ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📢 시장 이벤트: ${event.title}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (event.impactMultiplier != 1.0) {
                                            val isBull = event.impactMultiplier > 1.0
                                            Text("${if (isBull) "호재" else "악재"} ${event.impactMultiplier}x", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (isBull) Color(0xFF10B981) else MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    Text(event.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                else -> Unit
            }

            Text("📈 내 주식 포트폴리오", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("수익률은 내 평균 매수가 대비입니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (realtimeConnected) "● 실시간 서버 시세 연결됨" else "○ 실시간 재연결 중 · 마지막 REST 시세 유지",
                style = MaterialTheme.typography.labelSmall,
                color = if (realtimeConnected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            when (val pState = portfolioState) {
                is UiState.Success -> {
                    MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text("총 주식 평가금액", style = MaterialTheme.typography.labelMedium)
                        Text(formatWld(pState.data.totalStockValue), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        pState.data.holdings.forEach { holding ->
                            val current = decimalValue(holding.currentPrice)
                            val average = decimalValue(holding.averageBuyPrice)
                            val quantity = BigDecimal.valueOf(holding.quantity.toLong())
                            val profitLoss = current.subtract(average).multiply(quantity)
                            val isProfit = holding.profitLossPercent >= 0
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${holding.name} (${holding.symbol.substringBefore("  ")})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("보유 ${holding.quantity}주", style = MaterialTheme.typography.bodySmall)
                                    Text("평균 매수가 ${formatWld(holding.averageBuyPrice)}", style = MaterialTheme.typography.bodySmall)
                                    Text("현재가 ${formatWld(holding.currentPrice)}", style = MaterialTheme.typography.bodySmall)
                                    Text("평가금액 ${formatWld(holding.totalValue)}", style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isProfit) "▲ 이익" else "▼ 손해", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = if (isProfit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
                                    Text("${if (isProfit) "+" else "-"}${formatPercent(holding.profitLossPercent)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = if (isProfit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
                                    Text("${if (profitLoss.signum() >= 0) "+" else ""}${formatWld(profitLoss.toPlainString())}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (profitLoss.signum() >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> ErrorBanner(message = visibleError(pState.message), onRetry = { economyViewModel.loadStocks() })
                else -> Unit
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🛒 주식 시장 종목", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("아래 %는 오늘 시가 대비 등락률입니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilterChip(
                    selected = filterWatchlistOnly,
                    onClick = { filterWatchlistOnly = !filterWatchlistOnly },
                    label = { Text("관심종목만") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (filterWatchlistOnly) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val state = stocksState) {
            is UiState.Success -> {
                val filteredList = if (filterWatchlistOnly) state.data.filter { it.id in watchlistIds } else state.data
                items(filteredList) { stock ->
                    val isUp = stock.priceChangePercent >= 0
                    val isWatch = stock.id in watchlistIds
                    val statusText = if (stock.isMarketOpen) "거래 가능" else "장마감"
                    MoneyverseCard(onClick = { onSelectStock(stock) }) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isWatch) {
                                        Text("⭐ ", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(stock.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                                Text("${stock.symbol} · $statusText", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatWld(stock.currentPrice), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    "${if (isUp) "+" else "-"}${formatPercent(stock.priceChangePercent)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isUp) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            is UiState.Loading -> item { SkeletonLoader() }
            is UiState.Error -> item { ErrorBanner(message = visibleError(state.message), onRetry = { economyViewModel.loadStocks() }) }
            else -> Unit
        }

        when (val tradesState = stockTradesHistoryState) {
            is UiState.Success -> {
                val trades = tradesState.data
                if (trades.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("📜 최근 주식 체결 내역", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(trades.take(10)) { trade ->
                        val isBuy = trade.side.uppercase() == "BUY"
                        MoneyverseCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("${trade.stockName.ifBlank { trade.symbol }} · ${if (isBuy) "매수" else "매도"} ${trade.quantity}주", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("체결 단가 ${formatWld(trade.price)} · 총 ${formatWld(trade.totalAmount)}", style = MaterialTheme.typography.bodySmall)
                                }
                                Surface(
                                    color = if (isBuy) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (isBuy) "매수" else "매도",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isBuy) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
fun BusinessSubTab(economyViewModel: EconomyViewModel) {
    val businessesState by economyViewModel.businessesState.collectAsState()
    val catalogState by economyViewModel.businessCatalogState.collectAsState()
    val equityState by economyViewModel.businessEquityState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Text("🏢 내 사업체 현황", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("인수한 사업장은 매일 주기적으로 운영 수익을 정산할 수 있습니다.", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            when (val eState = equityState) {
                is UiState.Success -> MoneyverseCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("사업 투자 가능 자기자본: ${formatWld(eState.data.availableEquity)}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
                else -> Unit
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val bState = businessesState) {
            is UiState.Success -> {
                if (bState.data.isEmpty()) item { Text("현재 운영 중인 사업체가 없습니다. 아래 카탈로그에서 인수해 보세요.", style = MaterialTheme.typography.bodyMedium) }
                else items(bState.data) { biz ->
                    MoneyverseCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(biz.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("레벨 ${biz.level} · 일일 예상 수익 ${formatWld(biz.dailyRevenue)}", style = MaterialTheme.typography.bodySmall)
                                Text("정산 가능 금액: ${formatWld(biz.pendingRevenue)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            MoneyverseButton(
                                text = "수익 정산",
                                onClick = { economyViewModel.settleBusiness(biz.id) },
                                enabled = biz.pendingRevenue != "0" && biz.pendingRevenue.isNotEmpty()
                            )
                        }
                    }
                }
            }
            is UiState.Loading -> item { SkeletonLoader() }
            is UiState.Error -> item { ErrorBanner(message = visibleError(bState.message), onRetry = { economyViewModel.loadBusinesses() }) }
            else -> Unit
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("🛒 신규 사업장 인수 카탈로그", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val cState = catalogState) {
            is UiState.Success -> items(cState.data) { type ->
                MoneyverseCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(type.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(type.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("인수 비용 ${formatWld(type.purchasePrice)} · 일일 수익 ${formatWld(type.dailyRevenue)}", style = MaterialTheme.typography.labelSmall)
                        }
                        MoneyverseButton(text = "인수하기", onClick = { economyViewModel.purchaseBusiness(type.id) })
                    }
                }
            }
            is UiState.Loading -> item { SkeletonLoader() }
            is UiState.Error -> item { ErrorBanner(message = visibleError(cState.message), onRetry = { economyViewModel.loadBusinesses() }) }
            else -> Unit
        }
    }
}

@Composable
fun ShopSubTab(
    economyViewModel: EconomyViewModel,
    shopSearchViewModel: ShopSearchViewModel = viewModel()
) {
    var shopSegment by remember { mutableIntStateOf(0) } // 0: 카탈로그, 1: 내 보관함
    val itemsState by economyViewModel.shopItemsState.collectAsState()
    val holdingsState by economyViewModel.holdingsState.collectAsState()
    val searchUiState by shopSearchViewModel.results.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val activeQuery by shopSearchViewModel.query.collectAsState()

    val displayState = if (activeQuery.isBlank()) itemsState else searchUiState

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            // Segmented Button [카탈로그 구매 | 내 보관함]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { shopSegment = 0 },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (shopSegment == 0) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("🛍️ 카탈로그 구매")
                }
                Button(
                    onClick = {
                        shopSegment = 1
                        economyViewModel.loadHoldings()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (shopSegment == 1) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("🎒 내 보관함 (인벤토리)")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (shopSegment == 0) {
            // 카탈로그 뷰
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        shopSearchViewModel.search(it)
                    },
                    label = { Text("상점 아이템 검색") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (val state = displayState) {
                is UiState.Success -> {
                    if (state.data.isEmpty()) item { Text("검색 결과가 없습니다.", style = MaterialTheme.typography.bodyMedium) }
                    else items(state.data) { item ->
                        MoneyverseCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("가격 ${formatWld(item.price)}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                MoneyverseButton(text = if (item.isOwned) "보유 중" else "구매", onClick = { economyViewModel.purchaseShopItem(item.id) }, enabled = !item.isOwned)
                            }
                        }
                    }
                }
                is UiState.Loading -> item { SkeletonLoader() }
                is UiState.Error -> item { ErrorBanner(message = visibleError(state.message), onRetry = { economyViewModel.loadShop() }) }
                else -> Unit
            }
        } else {
            // 내 보관함 (인벤토리) 뷰
            item {
                Text("🎒 내 보유 아이템 및 장착 관리", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("소모품 사용, 코스메틱 장착, 내구도 유지비 정산을 처리합니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (val hState = holdingsState) {
                is UiState.Success -> {
                    if (hState.data.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Text("보유 중인 아이템이 없습니다. 카탈로그에서 필요한 아이템을 구매해 보세요.", modifier = Modifier.padding(16.dp))
                            }
                        }
                    } else {
                        items(hState.data) { holding ->
                            HoldingItemCard(
                                holding = holding,
                                onConsume = { economyViewModel.consumeItem(holding.catalogId) },
                                onEquip = { economyViewModel.equipItem(holding.catalogId) },
                                onSettleUpkeep = { economyViewModel.settleUpkeep(holding.catalogId) }
                            )
                        }
                    }
                }
                is UiState.Loading -> item { SkeletonLoader() }
                is UiState.Error -> item { ErrorBanner(message = visibleError(hState.message), onRetry = { economyViewModel.loadHoldings() }) }
                else -> Unit
            }
        }
    }
}

@Composable
fun HoldingItemCard(
    holding: ShopHoldingDto,
    onConsume: () -> Unit,
    onEquip: () -> Unit,
    onSettleUpkeep: () -> Unit
) {
    MoneyverseCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(holding.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = when (holding.rarity) {
                            "LEGENDARY" -> Color(0xFFF59E0B)
                            "EPIC" -> Color(0xFFA855F7)
                            "RARE" -> Color(0xFF3B82F6)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = holding.rarity,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White
                        )
                    }
                }
                Text("수량: ${holding.quantity}개", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(holding.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (holding.isEquipped) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("✨ 현재 장착 중 (슬롯: ${holding.equippedSlot ?: "기본"})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            if (holding.unpaidWeeks > 0 && holding.weeklyCost != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("⚠️ 미납 유지비: ${holding.unpaidWeeks}주 (${formatWld(holding.weeklyCost)})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actions
                if (holding.category.equals("consumable", ignoreCase = true) || holding.effectKind.isNotBlank() && holding.effectKind != "cosmetic") {
                    Button(onClick = onConsume, shape = RoundedCornerShape(8.dp)) {
                        Text("사용하기")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                OutlinedButton(onClick = onEquip, shape = RoundedCornerShape(8.dp)) {
                    Text(if (holding.isEquipped) "장착 해제" else "장착하기")
                }

                if (holding.unpaidWeeks > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSettleUpkeep,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("유지비 정산")
                    }
                }
            }
        }
    }
}

@Composable
fun BondPurchaseDialog(
    bondCode: String,
    cashBalance: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var amount by remember { mutableStateOf("10000") }
    val title = when (bondCode) {
        "BOND_7D" -> "단기 7일 가상 국채"
        "BOND_30D" -> "중기 30일 가상 국채"
        else -> "장기 90일 가상 국채"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📜 $title 매수") },
        text = {
            Column {
                Text("보유 현금: ${formatWld(cashBalance)}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("투자 금액 (WLD)") },
                    trailingIcon = { TextButton(onClick = { amount = wholeAmount(cashBalance) }) { Text("전액") } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "※ 국채는 만기 시 원금과 확정 이자가 합산 정산되며, 만기 전 중도 환매가 가능합니다.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(bondCode, amount) }) {
                Text("국채 매수 확정")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
fun TransferDialog(cashBalance: String, onDismiss: () -> Unit, onConfirm: (String, String, String?) -> Unit) {
    var recipient by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("💸 WLD 송금하기") },
        text = {
            Column {
                OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("수취인 ID/이메일") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("송금 금액 (WLD)") },
                    trailingIcon = { TextButton(onClick = { amount = wholeAmount(cashBalance) }) { Text("전액") } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모 (선택)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { MoneyverseButton(text = "송금 확정", onClick = { onConfirm(recipient, amount, memo.ifEmpty { null }) }) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun BankDialog(cashBalance: String, bankBalance: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var isDeposit by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏦 은행 입출금") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { isDeposit = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = if (isDeposit) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()) { Text("입금") }
                    Button(onClick = { isDeposit = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = if (!isDeposit) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()) { Text("출금") }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("이동할 금액 (WLD)") },
                    trailingIcon = { TextButton(onClick = { amount = wholeAmount(if (isDeposit) cashBalance else bankBalance) }) { Text("전액") } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { MoneyverseButton(text = "확인", onClick = { onConfirm(if (isDeposit) "deposit" else "withdraw", amount) }) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun LoanDialog(
    cashBalance: String,
    loans: List<LoanDto>,
    onDismiss: () -> Unit,
    onBorrow: (String) -> Unit,
    onRepay: (String, String) -> Unit,
    onApplySmartLoan: (Long) -> Unit = {},
    onRepaySmartLoan: (Long) -> Unit = {}
) {
    var isBorrowMode by remember { mutableStateOf(true) }
    var useSmartLoan by remember { mutableStateOf(false) }
    var loanIdInput by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏛️ 가상 은행 대출 관리") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { isBorrowMode = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = if (isBorrowMode) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()) { Text("대출 받기") }
                    Button(onClick = { isBorrowMode = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = if (!isBorrowMode) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()) { Text("대출 상환") }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (useSmartLoan) "⚡ 스마트 간편 심사 모드" else "일반 대출 모드", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (useSmartLoan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    Switch(checked = useSmartLoan, onCheckedChange = { useSmartLoan = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!isBorrowMode && !useSmartLoan) {
                    OutlinedTextField(value = loanIdInput, onValueChange = { loanIdInput = it }, label = { Text("상환할 대출 ID") }, modifier = Modifier.fillMaxWidth())
                    if (loans.isNotEmpty()) {
                        Text("보유 대출: ${loans.joinToString { "${it.id} (${formatWld(it.remainingBalance)})" }}", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(if (useSmartLoan) "스마트 신청/상환 금액 (WLD)" else "금액 (WLD)") },
                    trailingIcon = if (!isBorrowMode) {
                        {
                            TextButton(onClick = {
                                val target = loans.firstOrNull { it.id == loanIdInput.trim() } ?: loans.firstOrNull()
                                if (loanIdInput.isBlank() && target != null) loanIdInput = target.id
                                val cash = decimalValue(cashBalance)
                                val debt = decimalValue(target?.remainingBalance)
                                val full = if (target == null) cash else if (cash <= debt) cash else debt
                                amount = full.max(BigDecimal.ZERO).setScale(0, RoundingMode.DOWN).toPlainString()
                            }) { Text("전액") }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            MoneyverseButton(
                text = if (useSmartLoan) (if (isBorrowMode) "스마트 대출 신청" else "스마트 즉시 상환") else (if (isBorrowMode) "대출 실행" else "상환 실행"),
                onClick = {
                    if (useSmartLoan) {
                        val parsed = amount.replace(",", "").trim().toLongOrNull() ?: 0L
                        if (isBorrowMode) onApplySmartLoan(parsed)
                        else onRepaySmartLoan(parsed)
                    } else {
                        if (isBorrowMode) onBorrow(amount)
                        else {
                            val targetId = loanIdInput.ifBlank { loans.firstOrNull()?.id.orEmpty() }
                            onRepay(targetId, amount)
                        }
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun CreateSavingPocketDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Long, targetDate: String?, themeColor: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmountInput by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf("BLUE") }
    val themes = listOf("BLUE" to "파랑", "GREEN" to "초록", "AMBER" to "호박", "PURPLE" to "보라")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("✨ 신규 저축 포켓 개설") },
        text = {
            Column {
                Text("목표를 설정하고 별도의 안전 금고에 저축하세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("💡 본인 계좌 간 입출금 수수료 0원 무료", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("포켓 이름 (예: 비상금, 해외여행)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmountInput,
                    onValueChange = { targetAmountInput = it },
                    label = { Text("목표 금액 (WLD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("목표 달성일 (선택, YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("테마 색상 선택", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { (themeCode, themeLabel) ->
                        FilterChip(
                            selected = selectedTheme == themeCode,
                            onClick = { selectedTheme = themeCode },
                            label = { Text(themeLabel) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            MoneyverseButton(
                text = "개설하기",
                onClick = {
                    val amount = targetAmountInput.replace(",", "").trim().toLongOrNull() ?: 0L
                    val date = targetDate.trim().ifBlank { null }
                    if (name.isNotBlank() && amount > 0L) {
                        onConfirm(name.trim(), amount, date, selectedTheme)
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun PocketMovementDialog(
    pocket: SavingPocketDto,
    direction: String,
    cashBalance: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long) -> Unit
) {
    val isDeposit = direction == "DEPOSIT"
    var amountInput by remember { mutableStateOf("") }
    val maxAvailable = if (isDeposit) decimalValue(cashBalance).toLong() else pocket.balance

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isDeposit) "📥 '${pocket.name}' 저축하기" else "📤 '${pocket.name}' 인출하기") },
        text = {
            Column {
                Text(
                    if (isDeposit) "지갑 현금에서 포켓으로 안전하게 입금합니다." else "포켓에 모인 돈을 지갑 현금으로 인출합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isDeposit) "가용 현금 잔액" else "현재 포켓 잔액", style = MaterialTheme.typography.bodySmall)
                    Text("${formatWld(maxAvailable)} WLD", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("이동할 금액 (WLD)") },
                    trailingIcon = {
                        TextButton(onClick = {
                            amountInput = maxAvailable.coerceAtLeast(0L).toString()
                        }) { Text("전액") }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("💡 본인 계좌 간 이동으로 수수료 0원 무료", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        },
        confirmButton = {
            MoneyverseButton(
                text = if (isDeposit) "입금 완료" else "인출 완료",
                onClick = {
                    val amount = amountInput.replace(",", "").trim().toLongOrNull() ?: 0L
                    if (amount > 0L) {
                        onConfirm(amount)
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun PocketThemeDialog(
    pocket: SavingPocketDto,
    onDismiss: () -> Unit,
    onConfirm: (newTheme: String) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(pocket.themeColor) }
    val themes = listOf("BLUE" to "파랑", "GREEN" to "초록", "AMBER" to "호박", "PURPLE" to "보라")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎨 '${pocket.name}' 테마 변경") },
        text = {
            Column {
                Text("포켓 카드 색상을 변경할 수 있습니다.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text("⚠️ 테마 변경 시 100 WLD가 즉시 소각(HARD_SINK)됩니다.", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { (themeCode, themeLabel) ->
                        FilterChip(
                            selected = selectedTheme == themeCode,
                            onClick = { selectedTheme = themeCode },
                            label = { Text(themeLabel) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            MoneyverseButton(
                text = "100 WLD 소각 및 변경",
                onClick = { onConfirm(selectedTheme) }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
fun ArchivePocketConfirmDialog(
    pocket: SavingPocketDto,
    onDismiss: () -> Unit,
    onConfirm: (archiveMemo: String?) -> Unit
) {
    var memo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏆 '${pocket.name}' 명예의 전당 보관") },
        text = {
            Column {
                Text("목표를 달성한 저축 포켓을 아카이브합니다.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("목표 금액: ${formatWld(pocket.targetAmount)} WLD", style = MaterialTheme.typography.labelMedium)
                        Text("현재 모인 금액: ${formatWld(pocket.balance)} WLD", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("⚠️ 아카이브 수수료 500 WLD가 영구 소각되며, 남은 포켓 잔액은 전액 지갑 현금으로 즉시 자동 환급됩니다.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("보관 기념 메모 (선택)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            MoneyverseButton(
                text = "500 WLD 소각 및 보관",
                onClick = { onConfirm(memo.trim().ifBlank { null }) }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

