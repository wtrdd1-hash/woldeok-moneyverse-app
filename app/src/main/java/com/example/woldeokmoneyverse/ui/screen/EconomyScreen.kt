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
import com.example.woldeokmoneyverse.data.model.StockDto
import com.example.woldeokmoneyverse.data.model.UiState
import com.example.woldeokmoneyverse.ui.component.*
import com.example.woldeokmoneyverse.ui.viewmodel.EconomyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EconomyScreen(
    economyViewModel: EconomyViewModel
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("지갑/은행", "주식", "사업", "상점")

    var showTransferDialog by remember { mutableStateOf(false) }
    var showBankDialog by remember { mutableStateOf(false) }
    var showLoanDialog by remember { mutableStateOf(false) }

    var selectedStockForSheet by remember { mutableStateOf<StockDto?>(null) }

    val actionMessage by economyViewModel.actionMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            economyViewModel.clearActionMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MoneyverseSubTabRow(
                tabs = subTabs,
                selectedTabIndex = selectedSubTab,
                onTabSelected = { selectedSubTab = it }
            )

            when (selectedSubTab) {
                0 -> WalletBankSubTab(
                    economyViewModel = economyViewModel,
                    onOpenTransfer = { showTransferDialog = true },
                    onOpenBank = { showBankDialog = true },
                    onOpenLoan = { showLoanDialog = true }
                )
                1 -> StocksSubTab(economyViewModel, onSelectStock = { selectedStockForSheet = it })
                2 -> BusinessSubTab(economyViewModel)
                3 -> ShopSubTab(economyViewModel)
            }
        }
    }

    if (showTransferDialog) {
        TransferDialog(
            onDismiss = { showTransferDialog = false },
            onConfirm = { recipient, amount, memo ->
                economyViewModel.transferMoney(recipient, amount, memo)
                showTransferDialog = false
            }
        )
    }

    if (showBankDialog) {
        BankDialog(
            onDismiss = { showBankDialog = false },
            onConfirm = { direction, amount ->
                economyViewModel.bankMove(direction, amount)
                showBankDialog = false
            }
        )
    }

    if (showLoanDialog) {
        LoanDialog(
            onDismiss = { showLoanDialog = false },
            onBorrow = { amount ->
                economyViewModel.borrowLoan(amount)
                showLoanDialog = false
            },
            onRepay = { loanId, amount ->
                economyViewModel.repayLoan(loanId, amount)
                showLoanDialog = false
            }
        )
    }

    selectedStockForSheet?.let { stock ->
        StockDetailSheet(
            stock = stock,
            onDismiss = { selectedStockForSheet = null },
            onOrder = { orderType, qty ->
                economyViewModel.orderStock(stock.id, orderType, qty)
            }
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
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

                    MoneyverseSecondaryButton(
                        text = "🏛️ 가상 은행 대출 신청 및 상환 관리",
                        onClick = onOpenLoan,
                        modifier = Modifier.fillMaxWidth()
                    )

                    when (val loans = loansState) {
                        is UiState.Success -> if (loans.data.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("🏛️ 내 대출", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            loans.data.forEach { loan ->
                                MoneyverseCard {
                                    Text("대출 #${loan.id}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("잔액 ${loan.remainingBalance} WLD · 이율 ${loan.interestRate}%", style = MaterialTheme.typography.bodySmall)
                                    Text("상환일 ${loan.dueDate} · ${loan.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        is UiState.Error -> ErrorBanner(message = loans.message, onRetry = { economyViewModel.loadWallet() })
                        else -> Unit
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("📜 최근 원장 거래 기록", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    w.recentLedger.forEach { tx ->
                        MoneyverseCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(tx.description, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(tx.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "${tx.amount} WLD",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (tx.amount.startsWith("+")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                is UiState.Loading -> SkeletonLoader()
                is UiState.Error -> ErrorBanner(message = state.message, onRetry = { economyViewModel.loadWallet() })
                else -> {}
            }
        }
    }
}

@Composable
fun StocksSubTab(
    economyViewModel: EconomyViewModel,
    onSelectStock: (StockDto) -> Unit
) {
    val stocksState by economyViewModel.stocksState.collectAsState()
    val portfolioState by economyViewModel.portfolioState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        item {
            Text("📈 내 주식 포트폴리오", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            when (val pState = portfolioState) {
                is UiState.Success -> {
                    MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text("총 주식 평가금액", style = MaterialTheme.typography.labelMedium)
                        Text("${pState.data.totalStockValue} WLD", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        pState.data.holdings.forEach { holding ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${holding.symbol} ${holding.quantity}주 · ${holding.totalValue} WLD", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("🛒 주식 시장 종목 (클릭 시 차트 & 매수/매도)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val state = stocksState) {
            is UiState.Success -> {
                items(state.data) { stock ->
                    MoneyverseCard(onClick = { onSelectStock(stock) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stock.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(stock.symbol, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${stock.currentPrice} WLD", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    text = "${if (stock.priceChangePercent >= 0) "▲ +" else "▼ "}${stock.priceChangePercent}%",
                                    color = if (stock.priceChangePercent >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            MoneyverseButton(
                                text = "상세/주문",
                                onClick = { onSelectStock(stock) }
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

@Composable
fun BusinessSubTab(
    economyViewModel: EconomyViewModel
) {
    val businessesState by economyViewModel.businessesState.collectAsState()
    val catalogState by economyViewModel.businessCatalogState.collectAsState()
    val equityState by economyViewModel.businessEquityState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        item {
            Text("🏢 내가 보유한 사업장", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            if (equityState is UiState.Success) {
                val equity = (equityState as UiState.Success).data
                MoneyverseCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text("사업 자기자본 ${equity.availableEquity} WLD", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("총 사업 가치 ${equity.totalBusinessValuation} WLD · 추가 대출 한도 ${equity.maxLoanCapacity} WLD", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        when (val bState = businessesState) {
            is UiState.Success -> {
                items(bState.data) { biz ->
                    MoneyverseCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(biz.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("미정산 수익: ${biz.pendingRevenue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                            MoneyverseButton(
                                text = if (biz.isSettlementReady) "⚡ 수익 정산" else "정산 대기중",
                                onClick = { economyViewModel.settleBusiness(biz.id) },
                                enabled = biz.isSettlementReady
                            )
                        }
                    }
                }
            }
            is UiState.Loading -> item { SkeletonLoader() }
            else -> {}
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text("🏬 신규 사업 카탈로그 매수", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val cState = catalogState) {
            is UiState.Success -> {
                items(cState.data) { item ->
                    MoneyverseCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("매입가: ${item.purchaseCost} WLD | 일 수익: ${item.dailyRevenue} WLD", style = MaterialTheme.typography.bodySmall)
                                item.dailyOperatingCost?.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }?.let { cost ->
                                    Text("일 운영비: $cost WLD", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            MoneyverseButton(
                                text = "매수",
                                onClick = { economyViewModel.purchaseBusiness(item.id) }
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ShopSubTab(
    economyViewModel: EconomyViewModel
) {
    val shopItemsState by economyViewModel.shopItemsState.collectAsState()
    val purchasedItemsState by economyViewModel.purchasedItemsState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        item {
            Text("🛒 월덕 상점 아이템", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))

            when (val purchases = purchasedItemsState) {
                is UiState.Success -> if (purchases.data.isNotEmpty()) {
                    Text("내 구매 내역 (${purchases.data.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    purchases.data.forEach { purchase ->
                        Text("• ${purchase.itemName} · ${purchase.purchasedAt}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                else -> Unit
            }
        }

        when (val state = shopItemsState) {
            is UiState.Success -> {
                items(state.data) { item ->
                    MoneyverseCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("가격: ${item.price} WLD", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            MoneyverseButton(
                                text = if (item.isOwned) "보유 중" else "구매",
                                onClick = { economyViewModel.purchaseShopItem(item.id) },
                                enabled = !item.isOwned
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

@Composable
fun TransferDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
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
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("송금 수량 (WLD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모 (선택)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            MoneyverseButton(text = "송금 확정", onClick = { onConfirm(recipient, amount, memo.ifEmpty { null }) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
fun BankDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
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
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("이동할 금액 (WLD)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            MoneyverseButton(text = "확인", onClick = { onConfirm(if (isDeposit) "deposit" else "withdraw", amount) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
fun LoanDialog(
    onDismiss: () -> Unit,
    onBorrow: (String) -> Unit,
    onRepay: (String, String) -> Unit
) {
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
                    OutlinedTextField(
                        value = loanIdInput,
                        onValueChange = { loanIdInput = it },
                        label = { Text("상환할 대출 ID (미입력 시 loan_01)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("금액 (WLD)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            MoneyverseButton(
                text = if (isBorrowMode) "대출 실행" else "상환 실행",
                onClick = {
                    if (isBorrowMode) {
                        onBorrow(amount)
                    } else {
                        onRepay(loanIdInput.ifBlank { "loan_01" }, amount)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
