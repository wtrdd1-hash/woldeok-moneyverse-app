package com.example.woldeokmoneyverse.data.repository

import com.example.woldeokmoneyverse.util.formatMoneyAmount
import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.data.remote.apiProblem
import com.example.woldeokmoneyverse.data.remote.koreanApiProblem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

private fun writeFailure(action: String, code: Int): Nothing {
    when (code) {
        400 -> throw Exception("$action 실패 (400): 요청 입력값을 확인해 주세요.")
        401 -> throw Exception("$action 실패 (401): 인증이 만료되었습니다. 다시 로그인해 주세요.")
        403 -> throw Exception("$action 실패 (403): 권한 또는 약관 동의 상태를 확인해 주세요.")
        409 -> throw Exception("$action 실패 (409): 상태 충돌이 발생했습니다. 다시 시도해 주세요.")
        422 -> throw Exception("$action 실패 (422): 데이터 규격 오류입니다.")
        429 -> throw Exception("$action 실패 (429): 요청이 너무 잦습니다. 잠시 후 시도해 주세요.")
        else -> throw Exception("$action 실패 ($code): 서버 처리 중 오류가 발생했습니다.")
    }
}

class AuthRepository {

    suspend fun getAuthProviders(): Result<List<AuthProviderDto>> = runCatching {
        val res = ApiClient.api.getAuthProviders()
        if (res.isSuccessful && res.body() != null) res.body()!!.providers
        else throw Exception("로그인 제공자 목록 조회 실패 (${res.code()})")
    }

    private suspend fun ensurePreloginSession() {
        if (ApiClient.csrfToken.isNullOrBlank()) {
            val preRes = ApiClient.api.preloginSession()
            if (!preRes.isSuccessful || preRes.body() == null) {
                writeFailure("로그인 준비", preRes.code())
            }
            preRes.body()!!.csrfToken?.let { ApiClient.csrfToken = it }
        }
    }

    private suspend fun ensurePreloginSessionAndConsent() {
        ensurePreloginSession()

        // Registration and OAuth authorization require the pre-login session
        // to carry the currently published mandatory-policy acknowledgements.
        // The UI checkboxes validate user intent; this is the matching server
        // side persistence step.
        val policyRes = ApiClient.api.getPolicy()
        if (!policyRes.isSuccessful || policyRes.body() == null) {
            throw Exception("약관 버전 조회 실패 (${policyRes.code()})")
        }
        val policy = policyRes.body()!!
        val termsVersion = policy["termsVersion"] ?: throw Exception("약관 버전이 없습니다.")
        val privacyVersion = policy["privacyVersion"] ?: throw Exception("개인정보처리방침 버전이 없습니다.")
        val consentRes = ApiClient.api.recordConsent(
            ConsentRequest(termsVersion = termsVersion, privacyVersion = privacyVersion)
        )
        if (!consentRes.isSuccessful) {
            writeFailure("약관 동의 저장", consentRes.code())
        }
    }

    suspend fun getCurrentPolicyVersions(): Result<PolicyVersions> = runCatching {
        val res = ApiClient.api.getPolicy()
        if (!res.isSuccessful || res.body() == null) {
            throw Exception("약관 버전 조회 실패 (${res.code()})")
        }
        val policy = res.body()!!
        PolicyVersions(
            termsVersion = policy["termsVersion"] ?: throw Exception("약관 버전이 없습니다."),
            privacyVersion = policy["privacyVersion"] ?: throw Exception("개인정보처리방침 버전이 없습니다.")
        )
    }

    suspend fun acceptCurrentPolicy(policy: PolicyVersions): Result<Unit> = runCatching {
        val res = ApiClient.api.recordConsent(
            ConsentRequest(termsVersion = policy.termsVersion, privacyVersion = policy.privacyVersion)
        )
        if (!res.isSuccessful) writeFailure("약관 동의 저장", res.code())
    }

    suspend fun refreshAuthenticatedSession(): Result<Unit> = runCatching {
        val res = ApiClient.api.getViewer()
        if (!res.isSuccessful || res.body()?.signedIn != true) {
            throw Exception("인증 세션을 확인하지 못했습니다. 다시 로그인해 주세요.")
        }
        res.body()!!.csrfToken?.let { ApiClient.csrfToken = it }
    }

    suspend fun getOAuthAuthorizeUrl(provider: String): String {
        require(provider == "google" || provider == "discord") { "지원하지 않는 로그인 제공자입니다." }
        ensurePreloginSessionAndConsent()
        val res = if (provider == "google") ApiClient.api.getGoogleAuthorizeUrl() else ApiClient.api.getDiscordAuthorizeUrl()
        if (!res.isSuccessful || res.body() == null) {
            writeFailure("$provider 로그인 시작", res.code())
        }
        return res.body()!!["authorizationUrl"]
            ?: throw Exception("$provider 로그인 응답에 authorizationUrl이 없습니다.")
    }

    suspend fun verifyViewerSession(): Result<ViewerResponse> = runCatching {
        val res = ApiClient.api.getViewer()
        if (res.isSuccessful && res.body() != null) {
            val viewer = res.body()!!
            viewer.csrfToken?.let { ApiClient.csrfToken = it }
            viewer
        } else {
            throw Exception("세션이 만료되었습니다. 다시 로그인해 주세요.")
        }
    }

    suspend fun exchangeMobileHandoff(code: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.mobileHandoff(HandoffRequest(code))
        if (res.isSuccessful && res.body() != null) {
            val body = res.body()!!
            body.csrfToken?.let { ApiClient.csrfToken = it }
            val viewerRes = ApiClient.api.getViewer()
            val viewer = viewerRes.body()
            if (!viewerRes.isSuccessful || viewer?.signedIn != true) {
                throw Exception("OAuth 앱 세션 검증 실패: /auth/viewer 인증에 실패했습니다.")
            }
            viewer.csrfToken?.let { ApiClient.csrfToken = it }
            AuthResponse(
                success = true,
                outcome = body.outcome,
                userId = viewer.user?.userId ?: body.userId,
                email = viewer.user?.email ?: body.email,
                displayName = viewer.user?.displayName ?: body.displayName,
                csrfToken = ApiClient.csrfToken,
                consentCurrent = body.consentCurrent,
                message = body.message
            )
        } else {
            throw Exception("OAuth 세션 교환 실패 (401)")
        }
    }

    suspend fun login(req: LoginRequest): Result<AuthResponse> = runCatching {
        ensurePreloginSession()

        val res = ApiClient.api.login(req)
        if (!res.isSuccessful || res.body() == null || res.body()!!.outcome != "signed-in") {
            val code = res.code()
            writeFailure("로그인", code)
        }

        val body = res.body()!!
        body.csrfToken?.let { ApiClient.csrfToken = it }

        // Verify viewer session
        val viewerRes = ApiClient.api.getViewer()
        if (!viewerRes.isSuccessful || viewerRes.body() == null || !viewerRes.body()!!.signedIn) {
            throw Exception("로그인 인증 세션 실패: /auth/viewer 인증에 실패했습니다.")
        }

        val viewer = viewerRes.body()!!
        AuthResponse(
            success = true,
            userId = viewer.user?.userId ?: body.userId,
            email = viewer.user?.email ?: body.email,
            displayName = viewer.user?.displayName ?: body.displayName,
            csrfToken = body.csrfToken,
            message = "로그인 성공!"
        )
    }

    suspend fun register(req: RegisterRequest): Result<AuthResponse> = runCatching {
        ensurePreloginSessionAndConsent()

        val res = ApiClient.api.register(req)
        val code = res.code()

        if (res.isSuccessful || code == 202) {
            val body = res.body()
            if (body != null && body.accepted != false) {
                body
            } else {
                AuthResponse(success = true, message = "회원가입 요청이 접수되었습니다! 로그인해 주세요.")
            }
        } else {
            writeFailure("회원가입", code)
        }
    }

    suspend fun verifyEmail(token: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.verifyEmail(VerifyEmailRequest(token))
        if (res.isSuccessful && res.body() != null) {
            val body = res.body()!!
            body.csrfToken?.let { ApiClient.csrfToken = it }
            body
        } else {
            writeFailure("이메일 인증", res.code())
        }
    }

    suspend fun deleteAccount(): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.deleteAccount()
        if (res.isSuccessful && res.body() != null) {
            ApiClient.clearSession()
            res.body()!!
        } else {
            writeFailure("계정 삭제", res.code())
        }
    }

    suspend fun logout(): Result<AuthResponse> = runCatching {
        try {
            ApiClient.api.logout()
        } catch (_: Exception) {}
        ApiClient.clearSession()
        AuthResponse(success = true, message = "로그아웃 완료")
    }
}

class WalletRepository {
    suspend fun getWalletOverview(): Result<WalletOverviewResponse> = runCatching {
        val res = ApiClient.api.getWalletOverview()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("지갑 잔액 조회 실패")
    }

    suspend fun transferMoney(req: TransferRequest): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.transferMoney(req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("송금", res.code())
    }

    suspend fun bankMovement(req: BankMovementRequest): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.moveBankMoney(req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("입출금 처리 실패")
    }

    suspend fun getLoans(): Result<List<LoanDto>> = runCatching {
        val res = ApiClient.api.getLoans()
        if (res.isSuccessful && res.body() != null) res.body()!!["loans"] ?: emptyList()
        else throw Exception("대출 내역 조회 실패")
    }

    suspend fun borrowLoan(req: BorrowRequest): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.borrowLoan(req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("대출 실행", res.code())
    }

    suspend fun repayLoan(loanId: String, req: RepayRequest): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.repayLoan(loanId, req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("대출 상환", res.code())
    }

    suspend fun getBankingStanding(): Result<BankingStandingDto> = runCatching {
        val res = ApiClient.api.getBankingStanding()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("은행 스탠딩 정보 조회 실패 (${res.code()})")
    }

    suspend fun claimBankInterest(): Result<Unit> = runCatching {
        val res = ApiClient.api.claimBankInterest()
        if (res.isSuccessful) Unit
        else writeFailure("예금 이자 수령", res.code())
    }

    suspend fun purchaseBond(bondCode: String, amount: String): Result<Unit> = runCatching {
        val res = ApiClient.api.purchaseBond(BondPurchaseRequest(bondCode = bondCode, amount = amount))
        if (res.isSuccessful) Unit
        else writeFailure("국채 매수", res.code())
    }

    suspend fun redeemBond(bondId: String): Result<Unit> = runCatching {
        val res = ApiClient.api.redeemBond(bondId)
        if (res.isSuccessful) Unit
        else writeFailure("국채 환매", res.code())
    }

    suspend fun applySmartLoan(amount: Long, purpose: String = "INVESTMENT"): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.applySmartLoan(SmartLoanApplyRequest(amount, purpose))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("스마트 대출 신청", res.code())
    }

    suspend fun repaySmartLoan(amount: Long): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.repaySmartLoan(SmartLoanRepayRequest(amount))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("대출 상환", res.code())
    }

    // --- v8: Saving Pockets ---
    suspend fun getSavingPockets(): Result<List<SavingPocketDto>> = runCatching {
        val res = ApiClient.api.getSavingPockets()
        if (res.isSuccessful && res.body() != null) res.body()!!.pockets
        else throw Exception("저축 포켓 목록 조회 실패 (${res.code()})")
    }

    suspend fun createSavingPocket(name: String, targetAmount: Long, targetDate: String, themeColor: String = "MINT"): Result<SavingPocketDto> = runCatching {
        val res = ApiClient.api.createSavingPocket(CreatePocketRequest(name, targetAmount, targetDate, themeColor))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("저축 포켓 생성", res.code())
    }

    suspend fun movePocketMoney(pocketId: String, direction: String, amount: Long): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.movePocketMoney(pocketId, PocketMovementRequest(direction, amount))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("포켓 입출금", res.code())
    }

    suspend fun updatePocketTheme(pocketId: String, themeColor: String): Result<SavingPocketDto> = runCatching {
        val res = ApiClient.api.updatePocketTheme(pocketId, UpdatePocketThemeRequest(themeColor))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("포켓 테마 색상 변경", res.code())
    }

    suspend fun archivePocket(pocketId: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.archivePocket(pocketId, ArchivePocketRequest())
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("포켓 아카이브", res.code())
    }
}


class CasinoRepository {
    suspend fun playCoinFlip(req: CasinoPlayRequest): Result<CasinoPlayResponse> = runCatching {
        val res = ApiClient.api.playCoinFlip(req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception(koreanApiProblem(apiProblem(res), "동전 던지기 게임"))
    }

    suspend fun playDice(req: CasinoDiceRequest): Result<CasinoPlayResponse> = runCatching {
        val res = ApiClient.api.playDice(req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception(koreanApiProblem(apiProblem(res), "주사위 게임"))
    }

    suspend fun playHilo(req: CasinoHiloRequest): Result<CasinoPlayResponse> = runCatching {
        val res = ApiClient.api.playHilo(req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception(koreanApiProblem(apiProblem(res), "하이로우 20 게임"))
    }


    suspend fun getCasinoLimits(): Result<CasinoSelfLimitDto> = runCatching {
        val res = ApiClient.api.getCasinoLimits()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("카지노 한도 조회 실패")
    }

    suspend fun getCasinoTerms(): Result<CasinoTermsDto> = runCatching {
        val res = ApiClient.api.getCasinoTerms()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception(koreanApiProblem(apiProblem(res), "카지노 이용 한도 조회"))
    }

    suspend fun getHistory(): Result<List<CasinoHistoryItemDto>> = runCatching {
        val res = ApiClient.api.getCasinoHistory()
        if (res.isSuccessful && res.body() != null) res.body()!!.history
        else throw Exception("카지노 플레이 기록 조회 실패 (${res.code()})")
    }

    suspend fun getCoinFairness(): Result<FairnessProofDto> = runCatching {
        val res = ApiClient.api.getCoinFairness()
        if (res.isSuccessful && res.body() != null) res.body()!!.fairness
        else throw Exception("동전 던지기 공정성 검증 데이터 조회 실패")
    }

    suspend fun getDiceFairness(): Result<FairnessProofDto> = runCatching {
        val res = ApiClient.api.getDiceFairness()
        if (res.isSuccessful && res.body() != null) res.body()!!.fairness
        else throw Exception("주사위 공정성 검증 데이터 조회 실패")
    }
}

class SeasonRepository {
    suspend fun getSeasons(): Result<List<SeasonDto>> = runCatching {
        val res = ApiClient.api.getSeasons()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("시즌 이벤트 조회 실패")
    }

    suspend fun getLeaderboard(seasonId: String): Result<List<LeaderboardEntryDto>> = runCatching {
        val res = ApiClient.api.getSeasonLeaderboard(seasonId)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("리더보드 조회 실패")
    }
}

class StockRepository {
    suspend fun getStocks(): Result<List<StockDto>> = runCatching {
        val res = ApiClient.api.getStocks()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("주식 시세 조회 실패")
    }

    suspend fun getPortfolio(): Result<StockPortfolioDto> = runCatching {
        val res = ApiClient.api.getStockPortfolio()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("포트폴리오 조회 실패")
    }

    suspend fun orderStock(stockId: String, req: StockOrderRequest): Result<StockOrderResponse> = runCatching {
        val res = ApiClient.api.orderStock(stockId, req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("주식 주문", res.code())
    }

    suspend fun getCandles(stockId: String, interval: String = "86400"): Result<List<StockCandleDto>> = runCatching {
        val res = ApiClient.api.getStockCandles(stockId, interval)
        if (res.isSuccessful && res.body() != null) res.body()!!.candles
        else throw Exception("캔들 차트 조회 실패 (${res.code()})")
    }

    suspend fun getWatchlist(): Result<List<WatchlistStockDto>> = runCatching {
        val res = ApiClient.api.getStockWatchlist()
        if (res.isSuccessful && res.body() != null) res.body()!!.stocks
        else throw Exception("관심종목 목록 조회 실패 (${res.code()})")
    }

    suspend fun toggleWatchlist(stockId: String): Result<Unit> = runCatching {
        val res = ApiClient.api.toggleStockWatchlist(stockId)
        if (res.isSuccessful) Unit
        else writeFailure("관심종목 설정", res.code())
    }

    suspend fun getAlerts(): Result<List<StockAlertDto>> = runCatching {
        val res = ApiClient.api.getStockAlerts()
        if (res.isSuccessful && res.body() != null) res.body()!!.alerts
        else throw Exception("주가 알림 목록 조회 실패 (${res.code()})")
    }

    suspend fun createAlert(stockId: String, conditionKind: String, thresholdAmount: String): Result<StockAlertDto> = runCatching {
        val res = ApiClient.api.createStockAlert(CreateStockAlertRequest(stockId, conditionKind, thresholdAmount))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("주가 알림 등록", res.code())
    }

    suspend fun deleteAlert(alertId: String): Result<Unit> = runCatching {
        val res = ApiClient.api.deleteStockAlert(alertId)
        if (res.isSuccessful) Unit
        else writeFailure("주가 알림 삭제", res.code())
    }

    suspend fun getTradesHistory(): Result<List<StockHistoryItemDto>> = runCatching {
        val res = ApiClient.api.getStockTradesHistory()
        if (res.isSuccessful && res.body() != null) res.body()!!.trades
        else throw Exception("주식 거래 내역 조회 실패 (${res.code()})")
    }

    suspend fun getMarketEvents(): Result<List<MarketEventDto>> = runCatching {
        val res = ApiClient.api.getMarketEvents()
        if (res.isSuccessful && res.body() != null) res.body()!!.events
        else throw Exception("시장 이벤트 조회 실패 (${res.code()})")
    }

    suspend fun getSparklines(): Result<List<StockSparklineDto>> = runCatching {
        val res = ApiClient.api.getStockSparklines()
        if (res.isSuccessful && res.body() != null) res.body()!!.sparklines
        else throw Exception("스파크라인 시세 조회 실패 (${res.code()})")
    }
}


class BusinessRepository {
    suspend fun getOwnedBusinesses(): Result<List<BusinessDto>> = runCatching {
        val res = ApiClient.api.getOwnedBusinesses()
        if (res.isSuccessful && res.body() != null) res.body()!!.businesses
        else throw Exception("사업장 내역 조회 실패")
    }

    suspend fun getCatalog(): Result<List<BusinessTypeDto>> = runCatching {
        val res = ApiClient.api.getBusinessCatalog()
        if (res.isSuccessful && res.body() != null) res.body()!!.businessTypes
        else throw Exception("사업 카탈로그 조회 실패")
    }

    suspend fun getEquity(): Result<BusinessEquityDto> = runCatching {
        val res = ApiClient.api.getBusinessEquity()
        if (res.isSuccessful && res.body() != null) res.body()!!.equity
        else throw Exception("사업 자기자본 조회 실패")
    }

    suspend fun purchaseBusiness(typeId: String, req: BusinessPurchaseRequest): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.purchaseBusiness(typeId, req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("사업장 매수", res.code())
    }

    suspend fun settleProfit(businessId: String): Result<BusinessSettlementResponse> = runCatching {
        val res = ApiClient.api.settleBusinessProfit(businessId)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("사업 수익 정산", res.code())
    }
}

class ShopRepository {
    suspend fun getShopItems(): Result<List<ShopItemDto>> = runCatching {
        val res = ApiClient.api.getShopItems()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("상점 상품 조회 실패")
    }

    suspend fun getPurchasedItems(): Result<List<ShopPurchaseDto>> = runCatching {
        val res = ApiClient.api.getPurchasedItems()
        if (res.isSuccessful && res.body() != null) res.body()!!.purchases
        else throw Exception("구매 내역 조회 실패 (${res.code()})")
    }

    suspend fun purchaseItem(itemId: String, req: ShopPurchaseRequest): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.purchaseShopItem(itemId, req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("상품 구매", res.code())
    }

    suspend fun getHoldings(): Result<List<ShopHoldingDto>> = runCatching {
        val res = ApiClient.api.getShopHoldings()
        if (res.isSuccessful && res.body() != null) res.body()!!.holdings
        else throw Exception("보관함 목록 조회 실패 (${res.code()})")
    }

    suspend fun consumeItem(holdingId: String): Result<ConsumeHoldingResponse> = runCatching {
        val res = ApiClient.api.consumeHoldingItem(holdingId)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("아이템 사용", res.code())
    }

    suspend fun equipItem(holdingId: String): Result<EquipHoldingResponse> = runCatching {
        val res = ApiClient.api.equipHoldingItem(holdingId)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("코스메틱 장착/해제", res.code())
    }

    suspend fun settleUpkeep(holdingId: String): Result<UpkeepSettlementResponse> = runCatching {
        val res = ApiClient.api.settleHoldingUpkeep(holdingId)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("유지비 정산", res.code())
    }
}


class PlayRepository {
    suspend fun claimDailyReward(): Result<DailyClaimResponse> = runCatching {
        // The BFF validates an idempotencyKey for reward payouts.  Generate it
        // per user action so a retry of a separately tapped claim is never
        // mistaken for an empty/invalid request.
        val res = ApiClient.api.claimDailyReward(DailyClaimRequest())
        if (res.isSuccessful && res.body() != null) {
            val body = res.body()!!
            DailyClaimResponse(
                success = true,
                claimedAmount = body.amount ?: "0",
                nextEligibleAt = "다음 일일 초기화 후",
                message = if (body.replayed) "이미 처리된 일일 보상입니다." else "일일 보상을 받았습니다."
            )
        }
        else writeFailure("일일 출석 보상 수령", res.code())
    }

    suspend fun getWorkStatus(): Result<WorkStatusDto> = runCatching {
        val res = ApiClient.api.getWorkStatus()
        if (res.isSuccessful && res.body() != null) {
            val body = res.body()!!
            WorkStatusDto(
                jobTitle = "근무 현황",
                canWork = body.activeAssignments != "0",
                cooldownSeconds = 0,
                estimatedReward = "오늘 ${formatMoneyAmount(body.dailyPaid ?: "0")} / ${formatMoneyAmount(body.dailyCap ?: "0")} WLD",
                lastWorkedAt = "주간 ${formatMoneyAmount(body.weeklyPaid ?: "0")} / ${formatMoneyAmount(body.weeklyCap ?: "0")} WLD"
            )
        }
        else throw Exception("근무 상태 조회 실패")
    }

    suspend fun getProgression(): Result<ProgressionDto> = runCatching {
        val res = ApiClient.api.getProgression()
        if (!res.isSuccessful || res.body() == null) throw Exception("성장 레벨 조회 실패 (${res.code()})")
        val workRes = ApiClient.api.getWorkProfile()
        val active = workRes.takeIf { it.isSuccessful }?.body()?.activeJob
        val progression = res.body()!!.progression
        val stage = progression?.stageCode?.replace('_', ' ')?.lowercase()
            ?.replaceFirstChar { it.uppercase() } ?: "성장 단계 준비 중"
        val level = active?.level?.coerceAtLeast(1) ?: 1
        val current = active?.experience?.toLongOrNull()?.coerceIn(0L, Int.MAX_VALUE.toLong())?.toInt() ?: 0
        val required = active?.nextLevelExp?.toLongOrNull()?.coerceIn(1L, Int.MAX_VALUE.toLong())?.toInt()
            ?: (100 + 25 * (level - 1) + 10 * (level - 1) * (level - 1)).coerceAtLeast(1)
        ProgressionDto(
            level = level,
            currentExp = current,
            requiredExp = required,
            title = active?.jobType?.replace('_', ' ')?.uppercase() ?: stage,
            unlockedFeatures = progression?.nextRequirements?.keys?.toList().orEmpty()
        )
    }

    suspend fun getEarlyGameTasks(): Result<List<EarlyGameTaskDto>> = runCatching {
        val res = ApiClient.api.getTodayEarlyGame()
        if (!res.isSuccessful || res.body() == null) {
            throw Exception("초반 진행 미션 조회 실패 (${res.code()})")
        }
        res.body()!!.event?.let { event ->
            listOf(
                EarlyGameTaskDto(
                    id = event.event_code,
                    title = event.event_label,
                    description = event.event_detail,
                    isCompleted = event.claimed,
                    rewardAmount = event.reward_amount
                )
            )
        } ?: emptyList()
    }

    suspend fun getWorkTasks(): Result<List<WorkTaskDto>> = runCatching {
        val res = ApiClient.api.getWorkTasks()
        if (res.isSuccessful && res.body() != null) res.body()!!.tasks
        else throw Exception("근무 작업 목록 조회 실패 (${res.code()})")
    }

    suspend fun completeWorkTask(taskId: String): Result<WorkCompleteTaskResponse> = runCatching {
        val res = ApiClient.api.completeWorkTask(taskId, WorkCompleteTaskRequest())
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("근무 작업 완료", res.code())
    }

    suspend fun setActiveJob(jobType: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.setActiveJob(WorkActiveJobRequest(jobType))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("직업 변경", res.code())
    }

    suspend fun getProfileTitles(): Result<List<ProfileTitleDto>> = runCatching {
        val res = ApiClient.api.getProfileTitles()
        if (res.isSuccessful && res.body() != null) res.body()!!.titles
        else throw Exception("칭호 목록 조회 실패 (${res.code()})")
    }

    suspend fun getProgressionCredit(): Result<CreditGradeDetailsDto> = runCatching {
        val res = ApiClient.api.getProgressionCredit()
        if (res.isSuccessful && res.body() != null) res.body()!!.credit
        else throw Exception("신용 등급 조회 실패 (${res.code()})")
    }
}

class CommunityRepository {
    private fun ProfileResponse.toUiModel(work: WorkProfileResponse? = null): UserProfileDto {
        val nested = profile
        val active = work?.activeJob
        val name = displayName ?: nested?.displayName ?: "사용자"
        val actualLevel = active?.level ?: jobLevel ?: nested?.jobLevel ?: 1
        val actualJob = active?.jobType ?: jobType ?: nested?.jobType
        return UserProfileDto(
            userId = email ?: nested?.email ?: "self",
            displayName = name,
            email = email ?: nested?.email ?: "등록된 이메일 없음",
            level = actualLevel.coerceAtLeast(1),
            experience = active?.experience ?: "0",
            nextLevelExperience = active?.nextLevelExp ?: (actualLevel * actualLevel * 100).toString(),
            jobType = actualJob,
            title = featuredTitle ?: nested?.featuredTitle ?: actualJob?.uppercase() ?: "NEWBIE",
            bio = null,
            joinedAt = joinedAt ?: nested?.joinedAt ?: "가입일 정보 없음"
        )
    }

    suspend fun getBoardPosts(): Result<List<BoardPostDto>> = runCatching {
        val res = ApiClient.api.getBoardPosts()
        if (res.isSuccessful && res.body() != null) res.body()!!.posts
        else throw Exception("게시판 글 조회 실패")
    }

    suspend fun getPostDetail(postId: String): Result<BoardPostDto> = runCatching {
        val res = ApiClient.api.getBoardPostDetail(postId)
        if (res.isSuccessful && res.body() != null) res.body()!!.post
        else throw Exception("게시글 상세 조회 실패 (${res.code()})")
    }

    suspend fun createPost(req: CreatePostRequest): Result<BoardPostDto> = runCatching {
        val res = ApiClient.api.createBoardPost(req)
        if (res.isSuccessful && res.body() != null) res.body()!!.post
        else writeFailure("게시글 등록", res.code())
    }

    suspend fun updatePost(postId: String, req: UpdatePostRequest): Result<BoardPostDto> = runCatching {
        val res = ApiClient.api.updateBoardPost(postId, req)
        if (res.isSuccessful && res.body() != null) res.body()!!.post
        else writeFailure("게시글 수정", res.code())
    }

    suspend fun deletePost(postId: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.deleteBoardPost(postId)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("게시글 삭제", res.code())
    }

    suspend fun getComments(postId: String): Result<List<BoardCommentDto>> = runCatching {
        val res = ApiClient.api.getBoardPostComments(postId)
        if (res.isSuccessful && res.body() != null) res.body()!!.comments
        else throw Exception("댓글 목록 조회 실패 (${res.code()})")
    }

    suspend fun addComment(postId: String, req: AddCommentRequest): Result<CommentDto> = runCatching {
        val res = ApiClient.api.addPostComment(postId, req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("댓글 작성", res.code())
    }

    suspend fun deleteComment(postId: String, commentId: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.deleteBoardPostComment(postId, commentId)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("댓글 삭제", res.code())
    }

    suspend fun submitMemberPhoto(imageBytes: ByteArray, mimeType: String, altText: String): Result<AuthResponse> = runCatching {
        val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val uploadRes = ApiClient.api.uploadPhotoBytes(requestBody)
        if (!uploadRes.isSuccessful || uploadRes.body() == null) {
            writeFailure("사진 업로드", uploadRes.code())
        }
        val storageKey = uploadRes.body()!!["storageKey"]
            ?: throw Exception("사진 업로드 응답에 storageKey가 없습니다.")

        val submissionBody = mapOf(
            "storageKey" to storageKey,
            "altText" to altText,
            "idempotencyKey" to UUID.randomUUID().toString()
        )
        val submitRes = ApiClient.api.submitPhoto(submissionBody)
        if (submitRes.isSuccessful && submitRes.body() != null) submitRes.body()!!
        else writeFailure("갤러리 사진 등록", submitRes.code())
    }

    suspend fun getMyProfile(): Result<UserProfileDto> = runCatching {
        val res = ApiClient.api.getMyProfile()
        if (!res.isSuccessful || res.body() == null) throw Exception("프로필 조회 실패 (${res.code()})")
        val work = ApiClient.api.getWorkProfile().takeIf { it.isSuccessful }?.body()
        res.body()!!.toUiModel(work)
    }

    suspend fun updateMyProfile(displayName: String): Result<UserProfileDto> = runCatching {
        // The API replaces the full profile.  Read first so fields the current
        // UI does not edit are never accidentally cleared.
        val current = ApiClient.api.getMyProfile()
        if (!current.isSuccessful || current.body() == null) writeFailure("프로필 조회", current.code())
        val profile = current.body()!!
        val request = UpdateProfileRequest(
            visibility = profile.visibility ?: "members",
            displayName = displayName,
            imageUrl = profile.imageUrl,
            fieldVisibility = profile.fieldVisibility ?: mapOf(
                "imageUrl" to "public",
                "jobType" to "members",
                "workCompletions" to "members",
                "featuredTitle" to "public"
            ),
            featuredTitle = profile.featuredTitle
        )
        val res = ApiClient.api.updateMyProfile(request)
        if (!res.isSuccessful) writeFailure("프로필 수정", res.code())
        // Always replace the UI model with the authoritative GET result.
        val refreshed = ApiClient.api.getMyProfile()
        val work = ApiClient.api.getWorkProfile().takeIf { it.isSuccessful }?.body()
        if (refreshed.isSuccessful && refreshed.body() != null) refreshed.body()!!.toUiModel(work)
        else writeFailure("프로필 재조회", refreshed.code())
    }

    suspend fun getMyPhotos(): Result<List<PhotoSubmissionDto>> = runCatching {
        val res = ApiClient.api.getMyPhotos()
        if (res.isSuccessful && res.body() != null) res.body()!!.submissions
        else throw Exception("내 사진 조회 실패 (${res.code()})")
    }

    suspend fun requestPrivacyData(type: String): Result<PrivacyRequestDto> = runCatching {
        val res = ApiClient.api.requestPrivacyData(type)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("개인정보 요청 처리", res.code())
    }

    suspend fun getGalleryPhotos(): Result<List<PhotoDto>> = runCatching {
        val res = ApiClient.api.getGalleryPhotos()
        if (res.isSuccessful && res.body() != null) res.body()!!.photos
        else throw Exception("갤러리 사진 조회 실패")
    }

    suspend fun getAnnouncements(): Result<List<AnnouncementDto>> = runCatching {
        val res = ApiClient.api.getAnnouncements()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("공지사항 조회 실패")
    }

    suspend fun getServiceStatus(): Result<ServiceStatusDto> = runCatching {
        val res = ApiClient.api.getServiceStatus()
        if (res.isSuccessful && res.body() != null) {
            val services = res.body()!!.status
            val unhealthy = services.filter { it.state !in setOf("ok", "healthy", "operational") }
            ServiceStatusDto(
                status = if (unhealthy.isEmpty()) "operational" else "degraded",
                notice = unhealthy.firstOrNull()?.let { "${it.displayName}: ${it.detail ?: it.state}" },
                updatedAt = services.mapNotNull { it.observedAt }.maxOrNull().orEmpty()
            )
        }
        else throw Exception("서비스 상태 조회 실패")
    }
}

class AccountRepository {
    suspend fun getSessions(): Result<List<AccountSessionDto>> = runCatching {
        val res = ApiClient.api.getAccountSessions()
        if (res.isSuccessful && res.body() != null) res.body()!!.sessions
        else throw Exception("세션 목록 조회 실패 (${res.code()})")
    }

    suspend fun revokeSession(sessionId: String): Result<RevokeSessionResponse> = runCatching {
        val res = ApiClient.api.revokeAccountSession(sessionId)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("세션 로그아웃", res.code())
    }

    suspend fun revokeOtherSessions(): Result<RevokeOtherSessionsResponse> = runCatching {
        val res = ApiClient.api.revokeOtherSessions()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("타 기기 일괄 로그아웃", res.code())
    }

    suspend fun getSecurityLogs(): Result<List<SecurityLogDto>> = runCatching {
        val res = ApiClient.api.getSecurityLogs()
        if (res.isSuccessful && res.body() != null) res.body()!!.logs
        else throw Exception("보안 로그 조회 실패 (${res.code()})")
    }

    suspend fun changePassword(current: String, new: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.changePassword(ChangePasswordRequest(current, new))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("비밀번호 변경", res.code())
    }

    suspend fun setupTwoFactor(): Result<TwoFactorSetupResponse> = runCatching {
        val res = ApiClient.api.setupTwoFactor()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("2FA 설정 생성 실패 (${res.code()})")
    }

    suspend fun verifyTwoFactor(code: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.verifyTwoFactor(TwoFactorVerifyRequest(code))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("2FA 인증", res.code())
    }

    // --- v8: Notifications Governance ---
    suspend fun getNotifications(): Result<List<NotificationDto>> = runCatching {
        val res = ApiClient.api.getNotifications()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("알림 목록 조회 실패 (${res.code()})")
    }

    suspend fun markAllNotificationsRead(): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.markAllNotificationsRead()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("알림 모두 읽음 처리", res.code())
    }

    suspend fun getNotificationPreferences(): Result<NotificationPreferencesDto> = runCatching {
        val res = ApiClient.api.getNotificationPreferences()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("알림 수신 설정 조회 실패 (${res.code()})")
    }

    suspend fun updateNotificationPreferences(req: UpdateNotificationPreferencesRequest): Result<NotificationPreferencesDto> = runCatching {
        val res = ApiClient.api.updateNotificationPreferences(req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("알림 수신 설정 변경", res.code())
    }

    // --- v8: Safety Center & Urgent Takedowns ---
    suspend fun submitTakedown(req: TakedownRequest): Result<TakedownResponse> = runCatching {
        val res = ApiClient.api.submitTakedown(req)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("긴급 삭제 요청 접수", res.code())
    }

    suspend fun getTakedownStatus(id: String, password: String): Result<TakedownStatusResponse> = runCatching {
        val res = ApiClient.api.getTakedownStatus(id, password)
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("긴급 삭제 상태 조회 실패 (${res.code()})")
    }

    suspend fun getAccountSafety(): Result<AccountSafetyDto> = runCatching {
        val res = ApiClient.api.getAccountSafety()
        if (res.isSuccessful && res.body() != null) res.body()!!
        else throw Exception("계정 안전 상태 조회 실패 (${res.code()})")
    }
}


class AdminRepository {
    suspend fun getOverview(): Result<AdminOverviewDto> = runCatching {
        val res = ApiClient.api.getAdminOverview()
        if (res.isSuccessful && res.body() != null) res.body()!!.overview
        else throw Exception("관리자 지표 조회 실패 (${res.code()})")
    }

    suspend fun getFeatureSwitches(): Result<List<AdminFeatureSwitchDto>> = runCatching {
        val res = ApiClient.api.getAdminFeatureSwitches()
        if (res.isSuccessful && res.body() != null) res.body()!!.switches
        else throw Exception("피처 스위치 조회 실패 (${res.code()})")
    }

    suspend fun updateFeatureSwitch(key: String, enabled: Boolean): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.updateAdminFeatureSwitch(UpdateFeatureSwitchRequest(key, enabled))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("피처 스위치 변경", res.code())
    }

    suspend fun getUsers(): Result<List<AdminUserDto>> = runCatching {
        val res = ApiClient.api.getAdminUsers()
        if (res.isSuccessful && res.body() != null) res.body()!!.users
        else throw Exception("관리자 유저 목록 조회 실패 (${res.code()})")
    }

    suspend fun freezeUser(userId: String, freeze: Boolean): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.freezeUser(userId, FreezeUserRequest(freeze))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("유저 상태 변경", res.code())
    }
}

class EngagementRepository {
    suspend fun getEngagement(): Result<EngagementOverviewDto> = runCatching {
        val res = ApiClient.api.getEngagement()
        if (res.isSuccessful && res.body() != null) res.body()!!.engagement
        else throw Exception("인게이지먼트 정보 조회 실패 (${res.code()})")
    }

    suspend fun takeNpcOrder(code: String): Result<NpcOrderResponse> = runCatching {
        val res = ApiClient.api.takeNpcOrder(code, NpcOrderRequest(code))
        if (res.isSuccessful && res.body() != null) res.body()!!
        else writeFailure("NPC 오더 수락", res.code())
    }
}


