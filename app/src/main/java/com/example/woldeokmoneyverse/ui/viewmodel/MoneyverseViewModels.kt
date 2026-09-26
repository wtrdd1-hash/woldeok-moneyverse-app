package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.data.remote.RealtimeMarketClient
import com.example.woldeokmoneyverse.util.formatMoneyAmount
import java.math.BigDecimal
import java.math.RoundingMode
import com.example.woldeokmoneyverse.data.repository.*
import com.example.woldeokmoneyverse.ui.theme.ThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


private fun canonicalPositiveWldInput(value: String): String? {
    val raw = value.replace(",", "").trim()
    if (!Regex("^[1-9][0-9]*$").matches(raw)) return null
    return raw
}

class SettingsViewModel : ViewModel() {
    private val _selectedTheme = MutableStateFlow(ThemePreset.MIDNIGHT)
    val selectedTheme: StateFlow<ThemePreset> = _selectedTheme.asStateFlow()

    private val _customPrimaryColor = MutableStateFlow<Color?>(null)
    val customPrimaryColor: StateFlow<Color?> = _customPrimaryColor.asStateFlow()

    fun setThemePreset(preset: ThemePreset) {
        _selectedTheme.value = preset
    }

    fun setCustomPrimaryColor(color: Color?) {
        _customPrimaryColor.value = color
    }
}

class AuthViewModel(
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _authState = MutableStateFlow<UiState<AuthResponse>>(UiState.Empty)
    val authState: StateFlow<UiState<AuthResponse>> = _authState.asStateFlow()

    private val _isVerificationPending = MutableStateFlow(false)
    val isVerificationPending: StateFlow<Boolean> = _isVerificationPending.asStateFlow()

    private val _registeredEmail = MutableStateFlow("")
    val registeredEmail: StateFlow<String> = _registeredEmail.asStateFlow()

    private val _authProvidersState = MutableStateFlow<UiState<List<AuthProviderDto>>>(UiState.Loading)
    val authProvidersState: StateFlow<UiState<List<AuthProviderDto>>> = _authProvidersState.asStateFlow()

    fun loadAuthProviders() {
        viewModelScope.launch {
            authRepo.getAuthProviders().fold(
                onSuccess = { _authProvidersState.value = UiState.Success(it) },
                onFailure = { _authProvidersState.value = UiState.Error(it.message ?: "로그인 제공자 목록 로드 실패") }
            )
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepo.login(LoginRequest(email, pass))
            _authState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "로그인 실패: 아이디/이메일 또는 비밀번호를 확인하세요.") }
            )
        }
    }

    fun startOAuthLogin(
        provider: String,
        onUrlReady: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching { authRepo.getOAuthAuthorizeUrl(provider) }
                .onSuccess(onUrlReady)
                .onFailure { onFailure(it.message ?: "OAuth를 시작하지 못했습니다.") }
        }
    }

    fun verifyOAuthSessionAndLogin() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepo.verifyViewerSession()
            _authState.value = result.fold(
                onSuccess = { viewer ->
                    UiState.Success(
                        AuthResponse(
                            success = viewer.signedIn,
                            userId = viewer.user?.userId,
                            email = viewer.user?.email,
                            displayName = viewer.user?.displayName,
                            csrfToken = viewer.csrfToken,
                            message = "OAuth 세션 인증 성공!"
                        )
                    )
                },
                onFailure = { UiState.Error(it.message ?: "OAuth 세션 검증 실패") }
            )
        }
    }

    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepo.register(RegisterRequest(email, pass, name))
            _authState.value = result.fold(
                onSuccess = { res ->
                    if (res.verificationRequired) {
                        _registeredEmail.value = email
                        _isVerificationPending.value = true
                    }
                    UiState.Success(res)
                },
                onFailure = { UiState.Error(it.message ?: "회원가입 실패") }
            )
        }
    }

    fun verifyEmail(token: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepo.verifyEmail(token)
            _authState.value = result.fold(
                onSuccess = {
                    _isVerificationPending.value = false
                    UiState.Success(it)
                },
                onFailure = { UiState.Error(it.message ?: "이메일 인증 실패") }
            )
        }
    }

    fun dismissVerification() {
        _isVerificationPending.value = false
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            authRepo.deleteAccount().fold(
                onSuccess = {
                    _authState.value = UiState.Empty
                    onSuccess()
                },
                onFailure = {
                    _authState.value = UiState.Error(it.message ?: "계정 삭제 실패")
                }
            )
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepo.logout()
            _isVerificationPending.value = false
            _authState.value = UiState.Empty
            onComplete()
        }
    }
}

class HomeViewModel(
    private val walletRepo: WalletRepository = WalletRepository(),
    private val playRepo: PlayRepository = PlayRepository(),
    private val communityRepo: CommunityRepository = CommunityRepository()
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<UserProfileDto>>(UiState.Loading)
    val profileState: StateFlow<UiState<UserProfileDto>> = _profileState.asStateFlow()

    private val _walletState = MutableStateFlow<UiState<WalletOverviewResponse>>(UiState.Loading)
    val walletState: StateFlow<UiState<WalletOverviewResponse>> = _walletState.asStateFlow()

    private val _workState = MutableStateFlow<UiState<WorkStatusDto>>(UiState.Loading)
    val workState: StateFlow<UiState<WorkStatusDto>> = _workState.asStateFlow()

    private val _progressionState = MutableStateFlow<UiState<ProgressionDto>>(UiState.Loading)
    val progressionState: StateFlow<UiState<ProgressionDto>> = _progressionState.asStateFlow()

    private val _announcementsState = MutableStateFlow<UiState<List<AnnouncementDto>>>(UiState.Loading)
    val announcementsState: StateFlow<UiState<List<AnnouncementDto>>> = _announcementsState.asStateFlow()

    fun loadDashboardData() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            communityRepo.getMyProfile().fold(
                onSuccess = { _profileState.value = UiState.Success(it) },
                onFailure = { _profileState.value = UiState.Error(it.message ?: "프로필 로드 실패") }
            )
            _walletState.value = UiState.Loading
            walletRepo.getWalletOverview().fold(
                onSuccess = { _walletState.value = UiState.Success(it) },
                onFailure = { _walletState.value = UiState.Error(it.message ?: "지갑 정보 로드 실패") }
            )
            _workState.value = UiState.Loading
            playRepo.getWorkStatus().fold(
                onSuccess = { _workState.value = UiState.Success(it) },
                onFailure = { _workState.value = UiState.Error(it.message ?: "근무 상태 로드 실패") }
            )
            _progressionState.value = UiState.Loading
            playRepo.getProgression().fold(
                onSuccess = { _progressionState.value = UiState.Success(it) },
                onFailure = { _progressionState.value = UiState.Error(it.message ?: "성장 정보 로드 실패") }
            )
            _announcementsState.value = UiState.Loading
            communityRepo.getAnnouncements().fold(
                onSuccess = { _announcementsState.value = UiState.Success(it) },
                onFailure = { _announcementsState.value = UiState.Error(it.message ?: "공지 로드 실패") }
            )
        }
    }
}

class EconomyViewModel(
    private val walletRepo: com.example.woldeokmoneyverse.data.repository.WalletRepository = com.example.woldeokmoneyverse.data.repository.WalletRepository(),
    private val stockRepo: com.example.woldeokmoneyverse.data.repository.StockRepository = com.example.woldeokmoneyverse.data.repository.StockRepository(),
    private val businessRepo: BusinessRepository = BusinessRepository(),
    private val shopRepo: com.example.woldeokmoneyverse.data.repository.ShopRepository = com.example.woldeokmoneyverse.data.repository.ShopRepository(),
    private val realtimeMarket: RealtimeMarketClient = RealtimeMarketClient()
) : ViewModel() {


    private val _walletState = MutableStateFlow<UiState<WalletOverviewResponse>>(UiState.Loading)
    val walletState: StateFlow<UiState<WalletOverviewResponse>> = _walletState.asStateFlow()

    private val _stocksState = MutableStateFlow<UiState<List<StockDto>>>(UiState.Loading)
    val stocksState: StateFlow<UiState<List<StockDto>>> = _stocksState.asStateFlow()

    private val _portfolioState = MutableStateFlow<UiState<StockPortfolioDto>>(UiState.Loading)
    val portfolioState: StateFlow<UiState<StockPortfolioDto>> = _portfolioState.asStateFlow()

    private val _loansState = MutableStateFlow<UiState<List<LoanDto>>>(UiState.Loading)
    val loansState: StateFlow<UiState<List<LoanDto>>> = _loansState.asStateFlow()

    private val _businessesState = MutableStateFlow<UiState<List<BusinessDto>>>(UiState.Loading)
    val businessesState: StateFlow<UiState<List<BusinessDto>>> = _businessesState.asStateFlow()

    private val _businessCatalogState = MutableStateFlow<UiState<List<BusinessTypeDto>>>(UiState.Loading)
    val businessCatalogState: StateFlow<UiState<List<BusinessTypeDto>>> = _businessCatalogState.asStateFlow()

    private val _businessEquityState = MutableStateFlow<UiState<BusinessEquityDto>>(UiState.Loading)
    val businessEquityState: StateFlow<UiState<BusinessEquityDto>> = _businessEquityState.asStateFlow()

    private val _shopItemsState = MutableStateFlow<UiState<List<ShopItemDto>>>(UiState.Loading)
    val shopItemsState: StateFlow<UiState<List<ShopItemDto>>> = _shopItemsState.asStateFlow()

    private val _purchasedItemsState = MutableStateFlow<UiState<List<ShopPurchaseDto>>>(UiState.Loading)
    val purchasedItemsState: StateFlow<UiState<List<ShopPurchaseDto>>> = _purchasedItemsState.asStateFlow()

    private val _standingState = MutableStateFlow<UiState<BankingStandingDto>>(UiState.Loading)
    val standingState: StateFlow<UiState<BankingStandingDto>> = _standingState.asStateFlow()

    private val _holdingsState = MutableStateFlow<UiState<List<ShopHoldingDto>>>(UiState.Loading)
    val holdingsState: StateFlow<UiState<List<ShopHoldingDto>>> = _holdingsState.asStateFlow()

    private val _candlesState = MutableStateFlow<UiState<List<StockCandleDto>>>(UiState.Loading)
    val candlesState: StateFlow<UiState<List<StockCandleDto>>> = _candlesState.asStateFlow()

    private val _watchlistState = MutableStateFlow<UiState<List<WatchlistStockDto>>>(UiState.Loading)
    val watchlistState: StateFlow<UiState<List<WatchlistStockDto>>> = _watchlistState.asStateFlow()

    private val _alertsState = MutableStateFlow<UiState<List<StockAlertDto>>>(UiState.Loading)
    val alertsState: StateFlow<UiState<List<StockAlertDto>>> = _alertsState.asStateFlow()

    private val _marketEventsState = MutableStateFlow<UiState<List<MarketEventDto>>>(UiState.Loading)
    val marketEventsState: StateFlow<UiState<List<MarketEventDto>>> = _marketEventsState.asStateFlow()

    private val _stockSparklinesState = MutableStateFlow<UiState<List<StockSparklineDto>>>(UiState.Loading)
    val stockSparklinesState: StateFlow<UiState<List<StockSparklineDto>>> = _stockSparklinesState.asStateFlow()

    private val _stockTradesHistoryState = MutableStateFlow<UiState<List<StockHistoryItemDto>>>(UiState.Loading)
    val stockTradesHistoryState: StateFlow<UiState<List<StockHistoryItemDto>>> = _stockTradesHistoryState.asStateFlow()

    // --- v8: Saving Pockets State ---
    private val _savingPocketsState = MutableStateFlow<UiState<List<SavingPocketDto>>>(UiState.Loading)
    val savingPocketsState: StateFlow<UiState<List<SavingPocketDto>>> = _savingPocketsState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _marketRealtimeConnected = MutableStateFlow(false)
    val marketRealtimeConnected: StateFlow<Boolean> = _marketRealtimeConnected.asStateFlow()


    init {
        realtimeMarket.connect(
            onPrices = { quotes ->
                val current = (_stocksState.value as? UiState.Success)?.data ?: return@connect
                _stocksState.value = UiState.Success(current.map { stock ->
                    val quote = quotes[stock.id] ?: return@map stock
                    val price = quote.price.toBigDecimalOrNull() ?: return@map stock
                    val open = quote.open.toBigDecimalOrNull() ?: return@map stock
                    val pct = if (open.signum() == 0) 0.0 else
                        price.subtract(open).multiply(BigDecimal(100)).divide(open, 4, RoundingMode.HALF_UP).toDouble()
                    stock.copy(currentPrice = quote.price, priceChangePercent = pct)
                })
                val portfolio = (_portfolioState.value as? UiState.Success)?.data
                if (portfolio != null) {
                    var total = BigDecimal.ZERO
                    val holdings = portfolio.holdings.map { holding ->
                        val quote = quotes[holding.stockId]
                        val livePrice = quote?.price?.toBigDecimalOrNull()
                            ?: holding.currentPrice.toBigDecimalOrNull()
                            ?: BigDecimal.ZERO
                        val value = livePrice.multiply(BigDecimal.valueOf(holding.quantity.toLong()))
                        total = total.add(value)
                        val average = holding.averageBuyPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val pct = if (average.signum() == 0) 0.0 else
                            livePrice.subtract(average).multiply(BigDecimal(100)).divide(average, 4, RoundingMode.HALF_UP).toDouble()
                        holding.copy(
                            currentPrice = livePrice.toPlainString(),
                            totalValue = value.toPlainString(),
                            profitLossPercent = pct
                        )
                    }.toMutableList()
                    _portfolioState.value = UiState.Success(StockPortfolioDto(total.toPlainString(), holdings))
                }
            },
            onConnected = { _marketRealtimeConnected.value = it }
        )
    }

    override fun onCleared() {
        realtimeMarket.close()
        super.onCleared()
    }

    fun loadAllEconomyData() {
        loadWallet()
        loadSavingPockets()
        loadBankingStanding()
        loadStocks()
        loadBusinesses()
        loadShop()
        loadHoldings()
        loadWatchlist()
        loadAlerts()
        loadMarketEvents()
        loadStockSparklines()
        loadStockTradesHistory()
    }


    fun loadWallet() {
        viewModelScope.launch {
            _walletState.value = UiState.Loading
            walletRepo.getWalletOverview().fold(
                onSuccess = { _walletState.value = UiState.Success(it) },
                onFailure = { _walletState.value = UiState.Error(it.message ?: "지갑 정보 오류") }
            )
            walletRepo.getLoans().fold(
                onSuccess = { _loansState.value = UiState.Success(it) },
                onFailure = { _loansState.value = UiState.Error(it.message ?: "대출 내역 로드 실패") }
            )
        }
    }

    fun transferMoney(recipientId: String, amount: String, memo: String?) {
        val parsedAmount = canonicalPositiveWldInput(amount)
        if (parsedAmount == null) {
            _actionMessage.value = "올바른 송금 금액(양수)을 입력해 주세요."
            return
        }
        viewModelScope.launch {
            walletRepo.transferMoney(TransferRequest(recipientUserId = recipientId.trim(), amount = parsedAmount, memo = memo)).fold(
                onSuccess = {
                    _actionMessage.value = "송금이 성공적으로 완료되었습니다!"
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "송금 실패: ${it.message}" }
            )
        }
    }

    fun bankMove(direction: String, amount: String) {
        val parsedAmount = canonicalPositiveWldInput(amount)
        if (parsedAmount == null) {
            _actionMessage.value = "올바른 금액(양수)을 입력해 주세요."
            return
        }
        val canonicalDirection = direction.lowercase().trim()
        viewModelScope.launch {
            walletRepo.bankMovement(BankMovementRequest(direction = canonicalDirection, amount = parsedAmount)).fold(
                onSuccess = {
                    _actionMessage.value = "은행 입출금이 완료되었습니다!"
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "입출금 실패: ${it.message}" }
            )
        }
    }

    fun borrowLoan(amount: String) {
        val parsedAmount = canonicalPositiveWldInput(amount)
        if (parsedAmount == null) {
            _actionMessage.value = "올바른 대출 금액을 입력해 주세요."
            return
        }
        viewModelScope.launch {
            walletRepo.borrowLoan(BorrowRequest(amount = parsedAmount)).fold(
                onSuccess = {
                    _actionMessage.value = "대출이 성공적으로 승인되었습니다!"
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "대출 신청 실패: ${it.message}" }
            )
        }
    }

    fun repayLoan(loanId: String, amount: String) {
        val parsedAmount = canonicalPositiveWldInput(amount)
        if (parsedAmount == null) {
            _actionMessage.value = "올바른 상환 금액을 입력해 주세요."
            return
        }
        viewModelScope.launch {
            walletRepo.repayLoan(loanId, RepayRequest(amount = parsedAmount)).fold(
                onSuccess = {
                    _actionMessage.value = "대출 상환이 완료되었습니다!"
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "대출 상환 실패: ${it.message}" }
            )
        }
    }

    fun loadStocks() {
        viewModelScope.launch {
            stockRepo.getStocks().fold(
                onSuccess = { _stocksState.value = UiState.Success(it) },
                onFailure = { _stocksState.value = UiState.Error(it.message ?: "주식 로드 실패") }
            )
            stockRepo.getPortfolio().fold(
                onSuccess = { _portfolioState.value = UiState.Success(it) },
                onFailure = { _portfolioState.value = UiState.Error(it.message ?: "포트폴리오 로드 실패") }
            )
        }
    }

    fun orderStock(stockId: String, orderType: String, quantity: Int) {
        viewModelScope.launch {
            stockRepo.orderStock(stockId, StockOrderRequest(orderType.lowercase(), quantity)).fold(
                onSuccess = {
                    _actionMessage.value = it.message ?: "주문이 체결되었습니다!"
                    loadStocks()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "주식 주문 실패: ${it.message}" }
            )
        }
    }

    fun loadBusinesses() {
        viewModelScope.launch {
            businessRepo.getOwnedBusinesses().fold(
                onSuccess = { _businessesState.value = UiState.Success(it) },
                onFailure = { _businessesState.value = UiState.Error(it.message ?: "사업장 로드 실패") }
            )
            businessRepo.getCatalog().fold(
                onSuccess = { _businessCatalogState.value = UiState.Success(it) },
                onFailure = { _businessCatalogState.value = UiState.Error(it.message ?: "카탈로그 로드 실패") }
            )
            businessRepo.getEquity().fold(
                onSuccess = { _businessEquityState.value = UiState.Success(it) },
                onFailure = { _businessEquityState.value = UiState.Error(it.message ?: "자기자본 로드 실패") }
            )
        }
    }

    fun settleBusiness(businessId: String) {
        viewModelScope.launch {
            businessRepo.settleProfit(businessId).fold(
                onSuccess = {
                    _actionMessage.value = it.message ?: "정산 완료!"
                    loadBusinesses()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "정산 실패: ${it.message}" }
            )
        }
    }

    fun purchaseBusiness(typeId: String) {
        viewModelScope.launch {
            businessRepo.purchaseBusiness(typeId, BusinessPurchaseRequest(typeId)).fold(
                onSuccess = {
                    _actionMessage.value = "사업장을 성공적으로 매수했습니다!"
                    loadBusinesses()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "사업장 매수 실패: ${it.message}" }
            )
        }
    }

    fun loadShop() {
        viewModelScope.launch {
            shopRepo.getShopItems().fold(
                onSuccess = { _shopItemsState.value = UiState.Success(it) },
                onFailure = { _shopItemsState.value = UiState.Error(it.message ?: "상점 로드 실패") }
            )
            shopRepo.getPurchasedItems().fold(
                onSuccess = { _purchasedItemsState.value = UiState.Success(it) },
                onFailure = { _purchasedItemsState.value = UiState.Error(it.message ?: "구매 내역 로드 실패") }
            )
        }
    }

    fun purchaseShopItem(itemId: String) {
        viewModelScope.launch {
            shopRepo.purchaseItem(itemId, ShopPurchaseRequest(quantity = 1)).fold(
                onSuccess = {
                    _actionMessage.value = "상품을 구매했습니다!"
                    loadShop()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "구매 실패: ${it.message}" }
            )
        }
    }

    fun loadBankingStanding() {
        viewModelScope.launch {
            _standingState.value = UiState.Loading
            walletRepo.getBankingStanding().fold(
                onSuccess = { _standingState.value = UiState.Success(it) },
                onFailure = { _standingState.value = UiState.Error(it.message ?: "은행 스탠딩 조회 실패") }
            )
        }
    }

    fun claimInterest() {
        viewModelScope.launch {
            walletRepo.claimBankInterest().fold(
                onSuccess = {
                    _actionMessage.value = "예금 복리 이자를 수령했습니다!"
                    loadWallet()
                    loadBankingStanding()
                },
                onFailure = { _actionMessage.value = "이자 수령 실패: ${it.message}" }
            )
        }
    }

    fun purchaseBond(bondCode: String, amount: String) {
        val parsedAmount = canonicalPositiveWldInput(amount)
        if (parsedAmount == null) {
            _actionMessage.value = "올바른 채권 매수 금액을 입력해 주세요."
            return
        }
        viewModelScope.launch {
            walletRepo.purchaseBond(bondCode, parsedAmount).fold(
                onSuccess = {
                    _actionMessage.value = "국채 매수가 완료되었습니다!"
                    loadWallet()
                    loadBankingStanding()
                },
                onFailure = { _actionMessage.value = "국채 매수 실패: ${it.message}" }
            )
        }
    }

    fun redeemBond(bondId: String) {
        viewModelScope.launch {
            walletRepo.redeemBond(bondId).fold(
                onSuccess = {
                    _actionMessage.value = "국채가 상환/환매되었습니다!"
                    loadWallet()
                    loadBankingStanding()
                },
                onFailure = { _actionMessage.value = "국채 환매 실패: ${it.message}" }
            )
        }
    }

    fun loadHoldings() {
        viewModelScope.launch {
            _holdingsState.value = UiState.Loading
            shopRepo.getHoldings().fold(
                onSuccess = { _holdingsState.value = UiState.Success(it) },
                onFailure = { _holdingsState.value = UiState.Error(it.message ?: "보관함 조회 실패") }
            )
        }
    }

    fun consumeItem(holdingId: String) {
        viewModelScope.launch {
            shopRepo.consumeItem(holdingId).fold(
                onSuccess = {
                    _actionMessage.value = "아이템을 사용했습니다!"
                    loadHoldings()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "아이템 사용 실패: ${it.message}" }
            )
        }
    }

    fun equipItem(holdingId: String) {
        viewModelScope.launch {
            shopRepo.equipItem(holdingId).fold(
                onSuccess = {
                    _actionMessage.value = if (it.isEquipped) "코스메틱을 장착했습니다." else "코스메틱을 해제했습니다."
                    loadHoldings()
                },
                onFailure = { _actionMessage.value = "장착/해제 실패: ${it.message}" }
            )
        }
    }

    fun settleUpkeep(holdingId: String) {
        viewModelScope.launch {
            shopRepo.settleUpkeep(holdingId).fold(
                onSuccess = {
                    _actionMessage.value = "아이템 유지비가 정산되었습니다!"
                    loadHoldings()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "유지비 정산 실패: ${it.message}" }
            )
        }
    }

    fun loadCandles(stockId: String, interval: String = "86400") {
        viewModelScope.launch {
            _candlesState.value = UiState.Loading
            stockRepo.getCandles(stockId, interval).fold(
                onSuccess = { _candlesState.value = UiState.Success(it) },
                onFailure = { _candlesState.value = UiState.Error(it.message ?: "캔들 차트 로드 실패") }
            )
        }
    }

    fun loadWatchlist() {
        viewModelScope.launch {
            _watchlistState.value = UiState.Loading
            stockRepo.getWatchlist().fold(
                onSuccess = { _watchlistState.value = UiState.Success(it) },
                onFailure = { _watchlistState.value = UiState.Error(it.message ?: "관심종목 로드 실패") }
            )
        }
    }

    fun toggleWatchlist(stockId: String) {
        viewModelScope.launch {
            stockRepo.toggleWatchlist(stockId).fold(
                onSuccess = {
                    _actionMessage.value = "관심종목 설정이 변경되었습니다."
                    loadWatchlist()
                },
                onFailure = { _actionMessage.value = "관심종목 변경 실패: ${it.message}" }
            )
        }
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _alertsState.value = UiState.Loading
            stockRepo.getAlerts().fold(
                onSuccess = { _alertsState.value = UiState.Success(it) },
                onFailure = { _alertsState.value = UiState.Error(it.message ?: "주가 알림 로드 실패") }
            )
        }
    }

    fun createAlert(stockId: String, conditionKind: String, thresholdAmount: String) {
        val parsedAmount = canonicalPositiveWldInput(thresholdAmount)
        if (parsedAmount == null) {
            _actionMessage.value = "올바른 목표가를 입력해 주세요."
            return
        }
        viewModelScope.launch {
            stockRepo.createAlert(stockId, conditionKind, parsedAmount).fold(
                onSuccess = {
                    _actionMessage.value = "주가 목표가 알림이 등록되었습니다!"
                    loadAlerts()
                },
                onFailure = { _actionMessage.value = "알림 등록 실패: ${it.message}" }
            )
        }
    }

    fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            stockRepo.deleteAlert(alertId).fold(
                onSuccess = {
                    _actionMessage.value = "주가 알림이 삭제되었습니다."
                    loadAlerts()
                },
                onFailure = { _actionMessage.value = "알림 삭제 실패: ${it.message}" }
            )
        }
    }

    fun loadMarketEvents() {
        viewModelScope.launch {
            _marketEventsState.value = UiState.Loading
            stockRepo.getMarketEvents().fold(
                onSuccess = { _marketEventsState.value = UiState.Success(it) },
                onFailure = { _marketEventsState.value = UiState.Error(it.message ?: "시장 이벤트 조회 실패") }
            )
        }
    }

    fun loadStockSparklines() {
        viewModelScope.launch {
            _stockSparklinesState.value = UiState.Loading
            stockRepo.getSparklines().fold(
                onSuccess = { _stockSparklinesState.value = UiState.Success(it) },
                onFailure = { _stockSparklinesState.value = UiState.Error(it.message ?: "스파크라인 시세 조회 실패") }
            )
        }
    }

    fun loadStockTradesHistory() {
        viewModelScope.launch {
            _stockTradesHistoryState.value = UiState.Loading
            stockRepo.getTradesHistory().fold(
                onSuccess = { _stockTradesHistoryState.value = UiState.Success(it) },
                onFailure = { _stockTradesHistoryState.value = UiState.Error(it.message ?: "주식 거래 내역 조회 실패") }
            )
        }
    }

    fun applySmartLoan(amount: Long, purpose: String = "INVESTMENT") {
        viewModelScope.launch {
            walletRepo.applySmartLoan(amount, purpose).fold(
                onSuccess = {
                    _actionMessage.value = it.message ?: "스마트 대출이 실행되었습니다."
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "대출 신청 실패: ${it.message}" }
            )
        }
    }

    fun repaySmartLoan(amount: Long) {
        viewModelScope.launch {
            walletRepo.repaySmartLoan(amount).fold(
                onSuccess = {
                    _actionMessage.value = it.message ?: "스마트 대출이 상환되었습니다."
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "대출 상환 실패: ${it.message}" }
            )
        }
    }

    // --- v8: Saving Pockets Actions ---
    fun loadSavingPockets() {
        viewModelScope.launch {
            _savingPocketsState.value = UiState.Loading
            walletRepo.getSavingPockets().fold(
                onSuccess = { _savingPocketsState.value = UiState.Success(it) },
                onFailure = { _savingPocketsState.value = UiState.Error(it.message ?: "저축 포켓 목록 로드 실패") }
            )
        }
    }

    fun createSavingPocket(name: String, targetAmount: Long, targetDate: String, themeColor: String = "MINT") {
        viewModelScope.launch {
            walletRepo.createSavingPocket(name, targetAmount, targetDate, themeColor).fold(
                onSuccess = {
                    _actionMessage.value = "새 저축 포켓 [${it.name}]이 생성되었습니다!"
                    loadSavingPockets()
                },
                onFailure = { _actionMessage.value = "포켓 생성 실패: ${it.message}" }
            )
        }
    }

    fun movePocketMoney(pocketId: String, direction: String, amount: Long) {
        viewModelScope.launch {
            walletRepo.movePocketMoney(pocketId, direction, amount).fold(
                onSuccess = {
                    _actionMessage.value = if (direction.equals("DEPOSIT", true)) "포켓에 ${formatMoneyAmount(amount)} WLD를 입금했습니다 (수수료 0원)" else "포켓에서 ${formatMoneyAmount(amount)} WLD를 출금했습니다 (수수료 0원)"
                    loadSavingPockets()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "포켓 입출금 실패: ${it.message}" }
            )
        }
    }

    fun updatePocketTheme(pocketId: String, themeColor: String) {
        viewModelScope.launch {
            walletRepo.updatePocketTheme(pocketId, themeColor).fold(
                onSuccess = {
                    _actionMessage.value = "포켓 테마 색상을 변경했습니다 (100 WLD 소각 완료)"
                    loadSavingPockets()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "테마 변경 실패: ${it.message}" }
            )
        }
    }

    fun archivePocket(pocketId: String) {
        viewModelScope.launch {
            walletRepo.archivePocket(pocketId).fold(
                onSuccess = {
                    _actionMessage.value = "목표를 달성하여 명예의 전당에 보관되었습니다 (500 WLD 소각 및 잔액 환급)"
                    loadSavingPockets()
                    loadWallet()
                },
                onFailure = { _actionMessage.value = "아카이브 실패: ${it.message}" }
            )
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}



class PlayViewModel(
    private val playRepo: PlayRepository = PlayRepository(),
    private val casinoRepo: com.example.woldeokmoneyverse.data.repository.CasinoRepository = com.example.woldeokmoneyverse.data.repository.CasinoRepository(),
    private val seasonRepo: SeasonRepository = SeasonRepository(),
    private val engagementRepo: EngagementRepository = EngagementRepository()
) : ViewModel() {

    private val _workState = MutableStateFlow<UiState<WorkStatusDto>>(UiState.Loading)
    val workState: StateFlow<UiState<WorkStatusDto>> = _workState.asStateFlow()

    private val _progressionState = MutableStateFlow<UiState<ProgressionDto>>(UiState.Loading)
    val progressionState: StateFlow<UiState<ProgressionDto>> = _progressionState.asStateFlow()

    private val _tasksState = MutableStateFlow<UiState<List<EarlyGameTaskDto>>>(UiState.Loading)
    val tasksState: StateFlow<UiState<List<EarlyGameTaskDto>>> = _tasksState.asStateFlow()

    private val _seasonsState = MutableStateFlow<UiState<List<SeasonDto>>>(UiState.Loading)
    val seasonsState: StateFlow<UiState<List<SeasonDto>>> = _seasonsState.asStateFlow()

    private val _leaderboardState = MutableStateFlow<UiState<List<LeaderboardEntryDto>>>(UiState.Loading)
    val leaderboardState: StateFlow<UiState<List<LeaderboardEntryDto>>> = _leaderboardState.asStateFlow()

    private val _casinoLimitsState = MutableStateFlow<UiState<CasinoSelfLimitDto>>(UiState.Loading)
    val casinoLimitsState: StateFlow<UiState<CasinoSelfLimitDto>> = _casinoLimitsState.asStateFlow()
    private val _casinoTermsState = MutableStateFlow<UiState<CasinoTermsDto>>(UiState.Loading)
    val casinoTermsState: StateFlow<UiState<CasinoTermsDto>> = _casinoTermsState.asStateFlow()

    private val _engagementState = MutableStateFlow<UiState<EngagementOverviewDto>>(UiState.Loading)
    val engagementState: StateFlow<UiState<EngagementOverviewDto>> = _engagementState.asStateFlow()

    private val _casinoHistoryState = MutableStateFlow<UiState<List<CasinoHistoryItemDto>>>(UiState.Loading)
    val casinoHistoryState: StateFlow<UiState<List<CasinoHistoryItemDto>>> = _casinoHistoryState.asStateFlow()

    private val _fairnessProofState = MutableStateFlow<UiState<FairnessProofDto>>(UiState.Loading)
    val fairnessProofState: StateFlow<UiState<FairnessProofDto>> = _fairnessProofState.asStateFlow()

    private val _profileTitlesState = MutableStateFlow<UiState<List<ProfileTitleDto>>>(UiState.Loading)
    val profileTitlesState: StateFlow<UiState<List<ProfileTitleDto>>> = _profileTitlesState.asStateFlow()

    private val _creditGradeState = MutableStateFlow<UiState<CreditGradeDetailsDto>>(UiState.Loading)
    val creditGradeState: StateFlow<UiState<CreditGradeDetailsDto>> = _creditGradeState.asStateFlow()

    private val _gameClockState = MutableStateFlow<UiState<GameClockDto>>(UiState.Loading)
    val gameClockState: StateFlow<UiState<GameClockDto>> = _gameClockState.asStateFlow()

    private val _lastDiceFace = MutableStateFlow<Int?>(null)
    val lastDiceFace: StateFlow<Int?> = _lastDiceFace.asStateFlow()

    private val _lastCoinResult = MutableStateFlow<CasinoPlayResponse?>(null)
    val lastCoinResult: StateFlow<CasinoPlayResponse?> = _lastCoinResult.asStateFlow()

    private val _lastDiceResult = MutableStateFlow<CasinoPlayResponse?>(null)
    val lastDiceResult: StateFlow<CasinoPlayResponse?> = _lastDiceResult.asStateFlow()

    private val _lastSlotResult = MutableStateFlow<CasinoPlayResponse?>(null)
    val lastSlotResult: StateFlow<CasinoPlayResponse?> = _lastSlotResult.asStateFlow()

    private val _lastHiloResult = MutableStateFlow<CasinoPlayResponse?>(null)
    val lastHiloResult: StateFlow<CasinoPlayResponse?> = _lastHiloResult.asStateFlow()

    private val _lastHiloNumber = MutableStateFlow<Int?>(null)
    val lastHiloNumber: StateFlow<Int?> = _lastHiloNumber.asStateFlow()

    private val _diceHistory = MutableStateFlow<List<Int>>(listOf(3, 5, 2, 6, 4))
    val diceHistory: StateFlow<List<Int>> = _diceHistory.asStateFlow()


    private val _playMessage = MutableStateFlow<String?>(null)
    val playMessage: StateFlow<String?> = _playMessage.asStateFlow()

    private val _casinoBusy = MutableStateFlow(false)
    val casinoBusy: StateFlow<Boolean> = _casinoBusy.asStateFlow()

    fun loadPlayData() {
        viewModelScope.launch {
            playRepo.getWorkStatus().fold(
                onSuccess = { _workState.value = UiState.Success(it) },
                onFailure = { _workState.value = UiState.Error(it.message ?: "근무 상태 로드 실패") }
            )
            playRepo.getProgression().fold(
                onSuccess = { _progressionState.value = UiState.Success(it) },
                onFailure = { _progressionState.value = UiState.Error(it.message ?: "성장 로드 실패") }
            )
            playRepo.getEarlyGameTasks().fold(
                onSuccess = { _tasksState.value = UiState.Success(it) },
                onFailure = { _tasksState.value = UiState.Error(it.message ?: "미션 로드 실패") }
            )
            seasonRepo.getSeasons().fold(
                onSuccess = {
                    _seasonsState.value = UiState.Success(it)
                    if (it.isNotEmpty()) {
                        seasonRepo.getLeaderboard(it.first().id).fold(
                            onSuccess = { lb -> _leaderboardState.value = UiState.Success(lb) },
                            onFailure = {}
                        )
                    }
                },
                onFailure = { _seasonsState.value = UiState.Error(it.message ?: "시즌 로드 실패") }
            )
            casinoRepo.getCasinoLimits().fold(
                onSuccess = { _casinoLimitsState.value = UiState.Success(it) },
                onFailure = { _casinoLimitsState.value = UiState.Error(it.message ?: "카지노 한도 로드 실패") }
            )
            refreshGameClock()
            refreshCasinoTerms()
            loadEngagement()
            loadCasinoHistory()
            loadFairnessProof()
            loadProfileTitles()
            loadCreditGrade()
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            playRepo.claimDailyReward().fold(
                onSuccess = { _playMessage.value = it.message ?: "보상 수령 완료!" },
                onFailure = { _playMessage.value = "보상 수령 실패: ${it.message}" }
            )
        }
    }

    private suspend fun refreshGameClock() {
        runCatching { ApiClient.api.getGameClock() }
            .onSuccess { response ->
                _gameClockState.value = if (response.isSuccessful && response.body() != null) {
                    UiState.Success(response.body()!!)
                } else UiState.Error("서버 게임 시간 조회 실패 (${response.code()})")
            }
            .onFailure { _gameClockState.value = UiState.Error(it.message ?: "서버 게임 시간 조회 실패") }
    }

    private suspend fun refreshCasinoTerms() {
        casinoRepo.getCasinoTerms().fold(
            onSuccess = { _casinoTermsState.value = UiState.Success(it) },
            onFailure = { _casinoTermsState.value = UiState.Error(it.message ?: "카지노 이용 한도 로드 실패") }
        )
    }

    private fun casinoLimitBlockMessage(): String? {
        val terms = (_casinoTermsState.value as? UiState.Success)?.data ?: return null
        val remainingStake = terms.remainingStake.toBigIntegerOrNull()
        val remainingLoss = terms.remainingLoss.toBigIntegerOrNull()
        return when {
            remainingLoss != null && remainingLoss.signum() <= 0 -> "오늘 카지노 손실 한도 ${formatMoneyAmount(terms.dailyLossLimit)} WLD에 도달했습니다."
            remainingStake != null && remainingStake.signum() <= 0 -> "오늘 카지노 배팅 한도 ${formatMoneyAmount(terms.dailyStakeLimit)} WLD에 도달했습니다."
            else -> null
        }
    }

    fun playCoinFlip(req: CasinoPlayRequest) {
        viewModelScope.launch {
            casinoLimitBlockMessage()?.let {
                _playMessage.value = it
                return@launch
            }
            _casinoBusy.value = true
            casinoRepo.playCoinFlip(req).fold(
                onSuccess = {
                    _lastCoinResult.value = it
                    _playMessage.value = it.message
                },
                onFailure = { _playMessage.value = it.message ?: "카지노 게임을 처리할 수 없습니다." }
            )
            refreshCasinoTerms()
            _casinoBusy.value = false
        }
    }

    fun playDice(req: CasinoDiceRequest) {
        viewModelScope.launch {
            casinoLimitBlockMessage()?.let {
                _playMessage.value = it
                return@launch
            }
            _casinoBusy.value = true
            casinoRepo.playDice(req).fold(
                onSuccess = { result ->
                    _lastDiceResult.value = result
                    val face = result.resultOutcome.toIntOrNull()?.takeIf { it in 1..6 }
                    _lastDiceFace.value = face
                    if (face != null) {
                        _diceHistory.value = (listOf(face) + _diceHistory.value).take(8)
                    }
                    _playMessage.value = result.message
                },
                onFailure = { _playMessage.value = it.message ?: "주사위 게임을 처리할 수 없습니다." }
            )
            refreshGameClock()
            refreshCasinoTerms()
            _casinoBusy.value = false
        }
    }

    fun playSlot(stake: Long, choice: String = "odd") {
        viewModelScope.launch {
            casinoLimitBlockMessage()?.let {
                _playMessage.value = it
                return@launch
            }
            _casinoBusy.value = true
            casinoRepo.playDice(CasinoDiceRequest("dice_parity", choice, stake)).fold(
                onSuccess = { result ->
                    _lastSlotResult.value = result
                    _playMessage.value = result.message
                },
                onFailure = { _playMessage.value = it.message ?: "슬롯 게임을 처리할 수 없습니다." }
            )
            refreshCasinoTerms()
            _casinoBusy.value = false
        }
    }

    fun playHilo(choice: String, stake: Long) {
        viewModelScope.launch {
            casinoLimitBlockMessage()?.let {
                _playMessage.value = it
                return@launch
            }
            _casinoBusy.value = true
            casinoRepo.playHilo(CasinoHiloRequest(choice, stake)).fold(
                onSuccess = { result ->
                    _lastHiloResult.value = result
                    val num = result.resultOutcome.toIntOrNull() ?: (1..20).random()
                    _lastHiloNumber.value = num
                    _playMessage.value = result.message
                },
                onFailure = { _playMessage.value = it.message ?: "하이로우 게임을 처리할 수 없습니다." }
            )
            refreshCasinoTerms()
            _casinoBusy.value = false
        }
    }


    private val _workTasksState = MutableStateFlow<UiState<List<WorkTaskDto>>>(UiState.Loading)
    val workTasksState: StateFlow<UiState<List<WorkTaskDto>>> = _workTasksState.asStateFlow()

    fun loadWorkTasks() {
        viewModelScope.launch {
            _workTasksState.value = UiState.Loading
            playRepo.getWorkTasks().fold(
                onSuccess = { _workTasksState.value = UiState.Success(it) },
                onFailure = { _workTasksState.value = UiState.Error(it.message ?: "근무 과제 로드 실패") }
            )
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            _casinoBusy.value = true
            playRepo.completeWorkTask(taskId).fold(
                onSuccess = { res ->
                    _playMessage.value = "근무 완료! +${res.rewardAmount ?: "0"} WLD / +${res.experienceGained ?: "0"} EXP 획득"
                    loadWorkTasks()
                    loadPlayData()
                },
                onFailure = { _playMessage.value = it.message ?: "근무 작업 완료 실패" }
            )
            _casinoBusy.value = false
        }
    }

    fun changeActiveJob(jobType: String) {
        viewModelScope.launch {
            playRepo.setActiveJob(jobType).fold(
                onSuccess = {
                    _playMessage.value = "직업이 변경되었습니다."
                    loadPlayData()
                    loadWorkTasks()
                },
                onFailure = { _playMessage.value = it.message ?: "직업 변경 실패" }
            )
        }
    }

    fun loadEngagement() {
        viewModelScope.launch {
            _engagementState.value = UiState.Loading
            engagementRepo.getEngagement().fold(
                onSuccess = { _engagementState.value = UiState.Success(it) },
                onFailure = { _engagementState.value = UiState.Error(it.message ?: "퀘스트 정보 로드 실패") }
            )
        }
    }

    fun takeNpcOrder(code: String) {
        viewModelScope.launch {
            engagementRepo.takeNpcOrder(code).fold(
                onSuccess = {
                    _playMessage.value = it.message ?: "NPC 오더를 완료했습니다! (+${it.rewardAmount ?: "0"} WLD)"
                    loadEngagement()
                    loadPlayData()
                },
                onFailure = { _playMessage.value = "NPC 오더 실패: ${it.message}" }
            )
        }
    }

    fun loadCasinoHistory() {
        viewModelScope.launch {
            _casinoHistoryState.value = UiState.Loading
            casinoRepo.getHistory().fold(
                onSuccess = { _casinoHistoryState.value = UiState.Success(it) },
                onFailure = { _casinoHistoryState.value = UiState.Error(it.message ?: "카지노 기록 로드 실패") }
            )
        }
    }

    fun loadFairnessProof(game: String = "coin") {
        viewModelScope.launch {
            _fairnessProofState.value = UiState.Loading
            val result = if (game == "dice") casinoRepo.getDiceFairness() else casinoRepo.getCoinFairness()
            result.fold(
                onSuccess = { _fairnessProofState.value = UiState.Success(it) },
                onFailure = { _fairnessProofState.value = UiState.Error(it.message ?: "공정성 검증 데이터 로드 실패") }
            )
        }
    }

    fun loadProfileTitles() {
        viewModelScope.launch {
            _profileTitlesState.value = UiState.Loading
            playRepo.getProfileTitles().fold(
                onSuccess = { _profileTitlesState.value = UiState.Success(it) },
                onFailure = { _profileTitlesState.value = UiState.Error(it.message ?: "칭호 목록 로드 실패") }
            )
        }
    }

    fun loadCreditGrade() {
        viewModelScope.launch {
            _creditGradeState.value = UiState.Loading
            playRepo.getProgressionCredit().fold(
                onSuccess = { _creditGradeState.value = UiState.Success(it) },
                onFailure = { _creditGradeState.value = UiState.Error(it.message ?: "신용 등급 로드 실패") }
            )
        }
    }

    fun clearPlayMessage() {
        _playMessage.value = null
    }
}

class CommunityViewModel(
    private val communityRepo: CommunityRepository = CommunityRepository()
) : ViewModel() {

    private val _postsState = MutableStateFlow<UiState<List<BoardPostDto>>>(UiState.Loading)
    val postsState: StateFlow<UiState<List<BoardPostDto>>> = _postsState.asStateFlow()

    private val _profileState = MutableStateFlow<UiState<UserProfileDto>>(UiState.Loading)
    val profileState: StateFlow<UiState<UserProfileDto>> = _profileState.asStateFlow()

    private val _photosState = MutableStateFlow<UiState<List<PhotoDto>>>(UiState.Loading)
    val photosState: StateFlow<UiState<List<PhotoDto>>> = _photosState.asStateFlow()

    private val _myPhotosState = MutableStateFlow<UiState<List<PhotoSubmissionDto>>>(UiState.Loading)
    val myPhotosState: StateFlow<UiState<List<PhotoSubmissionDto>>> = _myPhotosState.asStateFlow()

    private val _statusState = MutableStateFlow<UiState<ServiceStatusDto>>(UiState.Loading)
    val statusState: StateFlow<UiState<ServiceStatusDto>> = _statusState.asStateFlow()

    private val _communityMessage = MutableStateFlow<String?>(null)
    val communityMessage: StateFlow<String?> = _communityMessage.asStateFlow()

    fun loadCommunityData() {
        viewModelScope.launch {
            communityRepo.getBoardPosts().fold(
                onSuccess = { _postsState.value = UiState.Success(it) },
                onFailure = { _postsState.value = UiState.Error(it.message ?: "게시판 로드 실패") }
            )
            communityRepo.getMyProfile().fold(
                onSuccess = { _profileState.value = UiState.Success(it) },
                onFailure = { _profileState.value = UiState.Error(it.message ?: "프로필 로드 실패") }
            )
            communityRepo.getGalleryPhotos().fold(
                onSuccess = { _photosState.value = UiState.Success(it) },
                onFailure = { _photosState.value = UiState.Error(it.message ?: "갤러리 로드 실패") }
            )
            communityRepo.getMyPhotos().fold(
                onSuccess = { _myPhotosState.value = UiState.Success(it) },
                onFailure = { _myPhotosState.value = UiState.Error(it.message ?: "내 사진 로드 실패") }
            )
            communityRepo.getServiceStatus().fold(
                onSuccess = { _statusState.value = UiState.Success(it) },
                onFailure = { _statusState.value = UiState.Error(it.message ?: "서비스 상태 로드 실패") }
            )
        }
    }

    private val _selectedPostState = MutableStateFlow<UiState<BoardPostDto>>(UiState.Empty)
    val selectedPostState: StateFlow<UiState<BoardPostDto>> = _selectedPostState.asStateFlow()

    private val _commentsState = MutableStateFlow<UiState<List<BoardCommentDto>>>(UiState.Empty)
    val commentsState: StateFlow<UiState<List<BoardCommentDto>>> = _commentsState.asStateFlow()

    fun loadPostDetail(postId: String) {
        viewModelScope.launch {
            _selectedPostState.value = UiState.Loading
            communityRepo.getPostDetail(postId).fold(
                onSuccess = { _selectedPostState.value = UiState.Success(it) },
                onFailure = { _selectedPostState.value = UiState.Error(it.message ?: "게시글 상세 로드 실패") }
            )
            loadComments(postId)
        }
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _commentsState.value = UiState.Loading
            communityRepo.getComments(postId).fold(
                onSuccess = { _commentsState.value = UiState.Success(it) },
                onFailure = { _commentsState.value = UiState.Error(it.message ?: "댓글 목록 로드 실패") }
            )
        }
    }

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            communityRepo.updateMyProfile(displayName).fold(
                onSuccess = { _profileState.value = UiState.Success(it); _communityMessage.value = "프로필을 저장했습니다." },
                onFailure = { _communityMessage.value = "프로필 저장 실패: ${it.message}" }
            )
        }
    }

    fun createPost(title: String, content: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            communityRepo.createPost(CreatePostRequest(title, content)).fold(
                onSuccess = {
                    _communityMessage.value = "게시글을 등록했습니다."
                    loadCommunityData()
                    onDone()
                },
                onFailure = { _communityMessage.value = "게시글 등록 실패: ${it.message}" }
            )
        }
    }

    fun updatePost(postId: String, title: String, content: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            communityRepo.updatePost(postId, UpdatePostRequest(title, content)).fold(
                onSuccess = {
                    _communityMessage.value = "게시글을 수정했습니다."
                    loadPostDetail(postId)
                    loadCommunityData()
                    onDone()
                },
                onFailure = { _communityMessage.value = "게시글 수정 실패: ${it.message}" }
            )
        }
    }

    fun deletePost(postId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            communityRepo.deletePost(postId).fold(
                onSuccess = {
                    _communityMessage.value = "게시글을 삭제했습니다."
                    loadCommunityData()
                    onDone()
                },
                onFailure = { _communityMessage.value = "게시글 삭제 실패: ${it.message}" }
            )
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            communityRepo.addComment(postId, AddCommentRequest(content)).fold(
                onSuccess = {
                    _communityMessage.value = "댓글을 등록했습니다."
                    loadComments(postId)
                    loadCommunityData()
                },
                onFailure = { _communityMessage.value = "댓글 등록 실패: ${it.message}" }
            )
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            communityRepo.deleteComment(postId, commentId).fold(
                onSuccess = {
                    _communityMessage.value = "댓글을 삭제했습니다."
                    loadComments(postId)
                    loadCommunityData()
                },
                onFailure = { _communityMessage.value = "댓글 삭제 실패: ${it.message}" }
            )
        }
    }

    fun uploadPhoto(imageBytes: ByteArray, mimeType: String, caption: String) {
        viewModelScope.launch {
            communityRepo.submitMemberPhoto(imageBytes, mimeType, caption).fold(
                onSuccess = { _communityMessage.value = "사진을 등록했습니다."; loadCommunityData() },
                onFailure = { _communityMessage.value = "사진 등록 실패: ${it.message}" }
            )
        }
    }

    fun requestPrivacyData(type: String) {
        viewModelScope.launch {
            communityRepo.requestPrivacyData(type).fold(
                onSuccess = { _communityMessage.value = "개인정보 ${if (type == "EXPORT") "내보내기" else "삭제"} 요청이 접수되었습니다. (${it.status})" },
                onFailure = { _communityMessage.value = "개인정보 요청 실패: ${it.message}" }
            )
        }
    }

    fun clearCommunityMessage() { _communityMessage.value = null }
}
