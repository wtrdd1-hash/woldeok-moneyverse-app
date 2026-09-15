package com.example.woldeokmoneyverse.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.woldeokmoneyverse.data.model.LoanDto
import com.example.woldeokmoneyverse.data.model.StockDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.EconomyViewModel
import com.example.woldeokmoneyverse.ui.viewmodel.ShopSearchViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

private fun formatWld(value: String?): String {
    val raw = value?.replace(",", "")?.trim().orEmpty()
    val number = runCatching { BigDecimal(raw) }.getOrNull() ?: return "${value ?: "0"} WLD"
    return "${DecimalFormat("#,##0.##").format(number)} WLD"
}

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
    var selectedStockForSheet by remember { mutableStateOf<StockDto?>(null) }
    val actionMessage by economyViewModel.actionMessage.collectAsState()
    val walletState by economyViewModel.walletState.collectAsState()
    val loansState by economyViewModel.loansState.collectAsState()
    val portfolioState by economyViewModel.portfolioState.collectAsState()
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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MoneyverseSubTabRow(tabs = subTabs, selectedTabIndex = selectedSubTab, onTabSelected = { selectedSubTab = it })
            when (selectedSubTab) {
                0 -> WalletBankSubTab(economyViewModel, { showTransferDialog = true }, { showBankDialog = true }, { showLoanDialog = true })
                1 -> StocksSubTab(economyViewModel, onSelectStock = { selectedStockForSheet = it })
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
            onRepay = { loanId, amount -> economyViewModel.repayLoan(loanId, amount); showLoanDialog = false }
        )
    }
    selectedStockForSheet?.let { stock ->
        val holding = (portfolioState as? UiState.Success)?.data?.holdings?.firstOrNull { it.stockId == stock.id }
        StockDetailSheet(
            stock = stock,
            maxBuyQuantity = maxStockQuantity(cashBalance, stock.currentPrice),
            maxSellQuantity = holding?.quantity ?: 0,
            onDismiss = { selectedStockForSheet = null },
            onOrder = { orderType, qty -> economyViewModel.orderStock(stock.id, orderType, qty) }
        )
    }
}

@Composable
fun WalletBankSubTab(
    economyViewModel: EconomyViewModel,
    onOpenTransfer: () -> Unit,
    onOpenBank: () -> Unit,
    onOpenLoan: () -> Unit
) {
    val walletState by economyViewModel.walletState.collectAsState()
    val loansState by economyViewModel.loansState.collectAsState()

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
                    Spacer(modifier = Modifier.height(8.dp))
                    MoneyverseSecondaryButton(text = "🏛️ 가상 은행 대출 신청 및 상환 관리", onClick = onOpenLoan, modifier = Modifier.fillMaxWidth())

                    when (val loans = loansState) {
                        is UiState.Success -> if (loans.data.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("🏛️ 내 대출", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
    val realtimeConnected by economyViewModel.marketRealtimeConnected.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
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
            Text("🛒 주식 시장 종목", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("아래 %는 오늘 시가 대비 등락률입니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val state = stocksState) {
            is UiState.Success -> items(state.data) { stock ->
                val isUp = stock.priceChangePercent >= 0
                MoneyverseCard(onClick = { onSelectStock(stock) }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stock.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(stock.symbol, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatWld(stock.currentPrice), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(text = "${if (isUp) "▲ +" else "▼ -"}${formatPercent(stock.priceChangePercent)} ${if (isUp) "상승" else "하락"}", color = if (isUp) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        MoneyverseButton(text = "상세/주문", onClick = { onSelectStock(stock) })
                    }
                }
            }
            is UiState.Loading -> item { SkeletonLoader() }
            is UiState.Error -> item { ErrorBanner(message = visibleError(state.message), onRetry = { economyViewModel.loadStocks() }) }
            else -> Unit
        }
    }
}

@Composable
fun BusinessSubTab(economyViewModel: EconomyViewModel) {
    val businessesState by economyViewModel.businessesState.collectAsState()
    val catalogState by economyViewModel.businessCatalogState.collectAsState()
    val equityState by economyViewModel.businessEquityState.collectAsState()
    val ownedTypeIds = (businessesState as? UiState.Success)?.data?.map { it.typeId }?.toSet().orEmpty()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Text("🏢 내가 보유한 사업장", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            if (equityState is UiState.Success) {
                val equity = (equityState as UiState.Success).data
                MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("사업 자기자본 ${formatWld(equity.availableEquity)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("총 사업 가치 ${formatWld(equity.totalBusinessValuation)} · 추가 대출 한도 ${formatWld(equity.maxLoanCapacity)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        when (val bState = businessesState) {
            is UiState.Success -> items(bState.data) { biz ->
                MoneyverseCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(biz.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("미정산 수익 ${formatWld(biz.pendingRevenue)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        MoneyverseButton(text = if (biz.isSettlementReady) "⚡ 수익 정산" else "정산 대기중", onClick = { economyViewModel.settleBusiness(biz.id) }, enabled = biz.isSettlementReady)
                    }
                }
            }
            is UiState.Loading -> item { SkeletonLoader() }
            is UiState.Error -> item { ErrorBanner(message = visibleError(bState.message), onRetry = { economyViewModel.loadBusinesses() }) }
            else -> Unit
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("🏬 신규 사업 카탈로그 매수", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("이미 보유한 사업 유형은 목록에서 자동으로 제외됩니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val cState = catalogState) {
            is UiState.Success -> {
                val available = cState.data.filterNot { it.id in ownedTypeIds }
                if (available.isEmpty()) {
                    item { Text("현재 추가로 구매할 수 있는 신규 사업이 없습니다.", style = MaterialTheme.typography.bodyMedium) }
                } else {
                    items(available) { item ->
                        MoneyverseCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("매입가 ${formatWld(item.purchaseCost)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("예상 일 수익 ${formatWld(item.dailyRevenue)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    item.dailyOperatingCost?.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }?.let { cost ->
                                        Text("일 운영비 ${formatWld(cost)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                MoneyverseButton(text = "매수", onClick = { economyViewModel.purchaseBusiness(item.id) })
                            }
                        }
                    }
                }
            }
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
    val shopItemsState by economyViewModel.shopItemsState.collectAsState()
    val purchasedItemsState by economyViewModel.purchasedItemsState.collectAsState()
    val searchState by shopSearchViewModel.results.collectAsState()
    val activeQuery by shopSearchViewModel.query.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val displayState = if (activeQuery.isBlank()) shopItemsState else searchState

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Text("🛒 월덕 상점 아이템", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("상품명·설명·카테고리 검색") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { shopSearchViewModel.search(searchText) }) { Text("검색") }
            }
            if (activeQuery.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("‘$activeQuery’ 검색 결과", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { searchText = ""; shopSearchViewModel.clear() }) { Text("초기화") }
                }
            }
            when (val purchases = purchasedItemsState) {
                is UiState.Success -> if (purchases.data.isNotEmpty()) {
                    Text("내 구매 내역 (${purchases.data.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    purchases.data.forEach { purchase -> Text("• ${purchase.itemName} · ${purchase.purchasedAt}", style = MaterialTheme.typography.bodySmall) }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                else -> Unit
            }
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
            is UiState.Error -> item { ErrorBanner(message = visibleError(state.message), onRetry = { if (activeQuery.isBlank()) economyViewModel.loadShop() else shopSearchViewModel.search(activeQuery) }) }
            else -> Unit
        }
    }
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
fun LoanDialog(cashBalance: String, loans: List<LoanDto>, onDismiss: () -> Unit, onBorrow: (String) -> Unit, onRepay: (String, String) -> Unit) {
    var isBorrowMode by remember { mutableStateOf(true) }
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
                Spacer(modifier = Modifier.height(12.dp))
                if (!isBorrowMode) {
                    OutlinedTextField(value = loanIdInput, onValueChange = { loanIdInput = it }, label = { Text("상환할 대출 ID") }, modifier = Modifier.fillMaxWidth())
                    if (loans.isNotEmpty()) {
                        Text("보유 대출: ${loans.joinToString { "${it.id} (${formatWld(it.remainingBalance)})" }}", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("금액 (WLD)") },
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
                text = if (isBorrowMode) "대출 실행" else "상환 실행",
                onClick = {
                    if (isBorrowMode) onBorrow(amount)
                    else {
                        val targetId = loanIdInput.ifBlank { loans.firstOrNull()?.id.orEmpty() }
                        onRepay(targetId, amount)
                    }
                }
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
