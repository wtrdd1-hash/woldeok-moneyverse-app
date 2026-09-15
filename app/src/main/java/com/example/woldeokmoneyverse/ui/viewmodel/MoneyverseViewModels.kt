package com.example.woldeokmoneyverse.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.data.repository.*
import com.example.woldeokmoneyverse.ui.theme.ThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private val walletRepo: WalletRepository = WalletRepository(),
    private val stockRepo: StockRepository = StockRepository(),
    private val businessRepo: BusinessRepository = BusinessRepository(),
    private val shopRepo: ShopRepository = ShopRepository()
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

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun loadAllEconomyData() {
        loadWallet()
        loadStocks()
        loadBusinesses()
        loadShop()
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
        val parsedAmount = amount.replace(",", "").trim().toLongOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
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
        val parsedAmount = amount.replace(",", "").trim().toLongOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
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
        val parsedAmount = amount.replace(",", "").trim().toLongOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
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
        val parsedAmount = amount.replace(",", "").trim().toLongOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
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

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}

class PlayViewModel(
    private val playRepo: PlayRepository = PlayRepository(),
    private val casinoRepo: CasinoRepository = CasinoRepository(),
    private val seasonRepo: SeasonRepository = SeasonRepository()
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

    fun playCoinFlip(req: CasinoPlayRequest) {
        viewModelScope.launch {
            _casinoBusy.value = true
            casinoRepo.playCoinFlip(req).fold(
                onSuccess = { _playMessage.value = it.message },
                onFailure = { _playMessage.value = "카지노 게임 실패: ${it.message}" }
            )
            _casinoBusy.value = false
        }
    }

    fun playDice(req: CasinoDiceRequest) {
        viewModelScope.launch {
            _casinoBusy.value = true
            casinoRepo.playDice(req).fold(
                onSuccess = { _playMessage.value = it.message },
                onFailure = { _playMessage.value = "주사위 게임 실패: ${it.message}" }
            )
            _casinoBusy.value = false
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

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            communityRepo.updateMyProfile(displayName).fold(
                onSuccess = { _profileState.value = UiState.Success(it); _communityMessage.value = "프로필을 저장했습니다." },
                onFailure = { _communityMessage.value = "프로필 저장 실패: ${it.message}" }
            )
        }
    }

    fun createPost(title: String, content: String) {
        viewModelScope.launch {
            communityRepo.createPost(CreatePostRequest(title, content)).fold(
                onSuccess = { _communityMessage.value = "게시글을 등록했습니다."; loadCommunityData() },
                onFailure = { _communityMessage.value = "게시글 등록 실패: ${it.message}" }
            )
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            communityRepo.addComment(postId, AddCommentRequest(content)).fold(
                onSuccess = { _communityMessage.value = "댓글을 등록했습니다."; loadCommunityData() },
                onFailure = { _communityMessage.value = "댓글 등록 실패: ${it.message}" }
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
