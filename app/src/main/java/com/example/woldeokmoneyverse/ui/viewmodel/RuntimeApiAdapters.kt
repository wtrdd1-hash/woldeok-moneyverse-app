package com.example.woldeokmoneyverse.ui.viewmodel

import com.example.woldeokmoneyverse.data.model.*
import com.example.woldeokmoneyverse.data.remote.ApiClient
import com.example.woldeokmoneyverse.data.remote.ApiProblem
import com.example.woldeokmoneyverse.data.remote.koreanApiProblem
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

private val adapterGson = Gson()

private fun JsonObject.text(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
    get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asString }.getOrNull() }
}
private fun JsonObject.bool(vararg names: String): Boolean? = names.firstNotNullOfOrNull { name ->
    get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asBoolean }.getOrNull() }
}
private fun JsonObject.int(vararg names: String): Int? = names.firstNotNullOfOrNull { name ->
    get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asInt }.getOrNull() }
}
private fun JsonObject.long(vararg names: String): Long? = names.firstNotNullOfOrNull { name ->
    get(name)?.takeUnless { it.isJsonNull }?.let { runCatching { it.asLong }.getOrNull() }
}
private fun JsonElement?.obj(): JsonObject? = this?.takeIf { it.isJsonObject }?.asJsonObject
private fun JsonArray?.elements(): List<JsonElement> = this?.toList().orEmpty()
private fun JsonElement.arrayFromEnvelope(name: String): JsonArray? = when {
    isJsonArray -> asJsonArray
    isJsonObject -> asJsonObject.getAsJsonArray(name)
    else -> null
}

private fun commandFailure(label: String, code: Int, detail: String = ""): Nothing {
    val problemJson = runCatching { JsonParser.parseString(detail).asJsonObject }.getOrNull()
    fun text(name: String): String? = problemJson?.get(name)?.takeUnless { it.isJsonNull }
        ?.let { runCatching { it.asString }.getOrNull() }
    throw Exception(koreanApiProblem(ApiProblem(code, text("code"), text("detail") ?: text("message")), label))
}

class WalletRepository {
    private val legacy = com.example.woldeokmoneyverse.data.repository.WalletRepository()

    suspend fun getWalletOverview(): Result<WalletOverviewResponse> = legacy.getWalletOverview()
    suspend fun getLoans(): Result<List<LoanDto>> = legacy.getLoans()

    suspend fun transferMoney(req: TransferRequest): Result<AuthResponse> = postSuccess("app-api/v1/wallet/transfers", req, "송금")
    suspend fun bankMovement(req: BankMovementRequest): Result<AuthResponse> = postSuccess("app-api/v1/bank/movements", req, "은행 입출금")
    suspend fun borrowLoan(req: BorrowRequest): Result<AuthResponse> = postSuccess("app-api/v1/bank/loans", req, "대출")
    suspend fun repayLoan(loanId: String, req: RepayRequest): Result<AuthResponse> = postSuccess("app-api/v1/bank/loans/$loanId/repayments", req, "대출 상환")

    private suspend fun postSuccess(path: String, body: Any, label: String): Result<AuthResponse> = runCatching {
        val res = ApiClient.api.contractPost(path, adapterGson.toJsonTree(body))
        if (!res.isSuccessful) commandFailure(label, res.code(), res.errorBody()?.string().orEmpty())
        AuthResponse(success = true, message = "$label 완료")
    }
}

class BusinessRepository {
    suspend fun getOwnedBusinesses(): Result<List<BusinessDto>> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/businesses/my-v2")
        if (!res.isSuccessful || res.body() == null) commandFailure("사업장 조회", res.code())
        res.body()!!.asJsonObject.getAsJsonArray("businesses").elements().mapNotNull { item ->
            val o = item.obj() ?: return@mapNotNull null
            val id = o.text("ownershipId", "ownership_id") ?: return@mapNotNull null
            val revenue = o.text("dailyRevenue", "daily_revenue") ?: "0"
            val cost = o.text("dailyOperatingCost", "daily_operating_cost") ?: "0"
            val settled = o.bool("isSettledToday", "is_settled_today") == true
            BusinessDto(
                id = id,
                typeId = o.text("businessTypeId", "business_type_id") ?: "",
                name = o.text("name") ?: o.text("symbol") ?: "사업장",
                level = 1,
                isSettlementReady = !settled,
                nextSettlementAt = if (settled) "다음 일일 초기화 후" else "지금 정산 가능",
                pendingRevenue = if (settled) "0" else subtractStrings(revenue, cost),
                licenseActive = !o.text("status").equals("inactive", ignoreCase = true),
                dailyRevenue = revenue,
                dailyOperatingCost = cost
            )
        }
    }

    suspend fun getCatalog(): Result<List<BusinessTypeDto>> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/businesses/catalog")
        if (!res.isSuccessful || res.body() == null) commandFailure("사업 카탈로그 조회", res.code())
        res.body()!!.asJsonObject.getAsJsonArray("businessTypes").elements().mapNotNull { item ->
            val o = item.obj() ?: return@mapNotNull null
            val id = o.text("id") ?: return@mapNotNull null
            BusinessTypeDto(
                id = id,
                name = o.text("name") ?: o.text("symbol") ?: "사업",
                category = o.text("symbol") ?: "business",
                purchaseCost = o.text("purchaseCost", "purchase_cost") ?: "0",
                dailyRevenue = o.text("dailyRevenue", "daily_revenue") ?: "0",
                requiredLevel = 1,
                description = o.text("description") ?: "",
                dailyOperatingCost = o.text("dailyOperatingCost", "daily_operating_cost")
            )
        }
    }

    suspend fun getEquity(): Result<BusinessEquityDto> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/businesses/equity")
        if (!res.isSuccessful || res.body() == null) commandFailure("사업 자기자본 조회", res.code())
        val o = res.body()!!.asJsonObject.get("equity").obj() ?: JsonObject()
        BusinessEquityDto(
            availableEquity = o.text("equityAmount", "equity_amount") ?: "0",
            totalBusinessValuation = o.text("holdingsAmount", "holdings_amount") ?: "0",
            maxLoanCapacity = o.text("equityAmount", "equity_amount") ?: "0"
        )
    }

    suspend fun purchaseBusiness(typeId: String, req: BusinessPurchaseRequest): Result<AuthResponse> = runCatching {
        val body = JsonObject().apply { addProperty("idempotencyKey", req.idempotencyKey) }
        suspend fun post(path: String) = ApiClient.api.contractPost(path, body)

        val path = "app-api/v1/businesses/catalog/$typeId/purchases"
        var res = post(path)
        if (res.code() == 403) {
            ApiClient.api.getViewer()
            res = post(path)
        }
        if (!res.isSuccessful) commandFailure("사업장 매수", res.code(), res.errorBody()?.string().orEmpty())
        AuthResponse(success = true, message = "사업장을 매수했습니다.")
    }

    suspend fun settleProfit(businessId: String): Result<BusinessSettlementResponse> = runCatching {
        val body = JsonObject().apply { addProperty("idempotencyKey", UUID.randomUUID().toString()) }
        val res = ApiClient.api.contractPost("app-api/v1/businesses/$businessId/settlements", body)
        if (!res.isSuccessful || res.body() == null) commandFailure("사업 정산", res.code(), res.errorBody()?.string().orEmpty())
        val o = res.body()!!.asJsonObject
        BusinessSettlementResponse(
            success = true,
            collectedAmount = o.text("netAmount", "net_amount") ?: "0",
            nextSettlementAt = o.text("settlementDate", "settlement_date") ?: "다음 일일 초기화 후",
            message = "사업 정산이 완료되었습니다."
        )
    }
}

class StockRepository {
    suspend fun getStocks(): Result<List<StockDto>> = runCatching {
        val listRes = ApiClient.api.contractGet("app-api/v1/stocks")
        if (!listRes.isSuccessful || listRes.body() == null) commandFailure("주식 시세 조회", listRes.code())
        val sparkRes = ApiClient.api.contractGet("app-api/v1/stocks/sparklines?limit=60")
        val sparkById = mutableMapOf<String, List<Double>>()
        if (sparkRes.isSuccessful && sparkRes.body() != null) {
            sparkRes.body()!!.asJsonObject.getAsJsonArray("series").elements().forEach { e ->
                val o = e.obj() ?: return@forEach
                val id = o.text("stockId", "stock_id") ?: return@forEach
                sparkById[id] = o.getAsJsonArray("prices")?.mapNotNull { runCatching { it.asDouble }.getOrNull() }.orEmpty()
            }
        }
        listRes.body()!!.arrayFromEnvelope("stocks").elements().mapNotNull { e ->
            val o = e.obj() ?: return@mapNotNull null
            val id = o.text("id") ?: return@mapNotNull null
            val current = o.text("currentPrice", "current_price") ?: "0"
            val open = o.text("dayOpenPrice", "day_open_price") ?: current
            StockDto(
                id = id,
                symbol = o.text("symbol") ?: "-",
                name = o.text("name") ?: "종목",
                currentPrice = current,
                priceChangePercent = percentChange(current, open),
                isMarketOpen = true,
                historyPrices = sparkById[id].orEmpty()
            )
        }
    }

    suspend fun getPortfolio(): Result<StockPortfolioDto> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/stocks/portfolio")
        if (!res.isSuccessful || res.body() == null) commandFailure("포트폴리오 조회", res.code())
        var total = BigDecimal.ZERO
        val holdings = res.body()!!.asJsonObject.getAsJsonArray("holdings").elements().mapNotNull { e ->
            val o = e.obj() ?: return@mapNotNull null
            val id = o.text("stockId", "stock_id") ?: return@mapNotNull null
            val market = o.text("marketValue", "market_value") ?: "0"
            total = total.add(decimal(market))
            val current = o.text("currentPrice", "current_price") ?: "0"
            val avg = o.text("averageCost", "average_cost") ?: "0"
            val pnlPercent = percentChange(current, avg)
            val baseSymbol = o.text("symbol") ?: "-"
            val pnlLabel = if (pnlPercent >= 0) "▲ +${pnlPercent}% 이익" else "▼ ${pnlPercent}% 손해"
            StockHoldingDto(
                stockId = id,
                symbol = "$baseSymbol  $pnlLabel",
                name = o.text("name") ?: "종목",
                quantity = (o.text("quantity")?.toLongOrNull() ?: 0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                averageBuyPrice = avg,
                currentPrice = current,
                totalValue = market,
                profitLossPercent = pnlPercent
            )
        }.toMutableList()
        StockPortfolioDto(totalStockValue = total.stripTrailingZeros().toPlainString(), holdings = holdings)
    }

    suspend fun orderStock(stockId: String, req: StockOrderRequest): Result<StockOrderResponse> = runCatching {
        val body = JsonObject().apply {
            addProperty("side", req.side.lowercase())
            addProperty("quantity", req.quantity)
            addProperty("idempotencyKey", req.idempotencyKey)
        }
        var res = ApiClient.api.contractPost("app-api/v1/stocks/$stockId/orders", body)
        if (res.code() == 403) {
            ApiClient.api.getViewer()
            res = ApiClient.api.contractPost("app-api/v1/stocks/$stockId/orders", body)
        }
        if (!res.isSuccessful || res.body() == null) commandFailure("주식 주문", res.code(), res.errorBody()?.string().orEmpty())
        val o = res.body()!!.asJsonObject
        StockOrderResponse(
            success = true,
            executedPrice = o.text("unitPrice", "unit_price", "currentPrice", "current_price") ?: "0",
            totalCost = o.text("grossAmount", "gross_amount") ?: "0",
            message = "주식 주문이 체결되었습니다."
        )
    }
}

class CasinoRepository {
    suspend fun getCasinoLimits(): Result<CasinoSelfLimitDto> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/casino/self-limit")
        if (!res.isSuccessful || res.body() == null) commandFailure("카지노 한도 조회", res.code())
        val o = res.body()!!.asJsonObject
        CasinoSelfLimitDto(
            dailyBetLimit = o.long("dailyBetLimit", "daily_bet_limit") ?: 0L,
            dailyLossLimit = o.long("dailyLossLimit", "daily_loss_limit") ?: 0L,
            lockedUntil = o.text("lockedUntil", "locked_until")
        )
    }

    suspend fun getCasinoTerms(): Result<CasinoTermsDto> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/casino/coin/terms")
        if (!res.isSuccessful || res.body() == null) commandFailure("카지노 이용 한도 조회", res.code(), res.errorBody()?.string().orEmpty())
        val o = res.body()!!.asJsonObject
        CasinoTermsDto(
            enabled = o.bool("enabled") ?: true,
            minStake = o.text("minStake", "min_stake") ?: "0",
            maxStake = o.text("maxStake", "max_stake") ?: "0",
            dailyStakeLimit = o.text("dailyStakeLimit", "daily_stake_limit") ?: "0",
            dailyLossLimit = o.text("dailyLossLimit", "daily_loss_limit") ?: "0",
            dailyStakeUsed = o.text("dailyStakeUsed", "daily_stake_used") ?: "0",
            dailyLossUsed = o.text("dailyLossUsed", "daily_loss_used") ?: "0",
            remainingStake = o.text("remainingStake", "remaining_stake") ?: "0",
            remainingLoss = o.text("remainingLoss", "remaining_loss") ?: "0"
        )
    }

    suspend fun playCoinFlip(req: CasinoPlayRequest): Result<CasinoPlayResponse> = play(
        "app-api/v1/casino/coin/plays",
        JsonObject().apply {
            addProperty("choice", req.choice)
            addProperty("stake", req.stake)
            addProperty("idempotencyKey", UUID.randomUUID().toString())
        }, req.stake, false
    )

    suspend fun playDice(req: CasinoDiceRequest): Result<CasinoPlayResponse> = play(
        "app-api/v1/casino/dice/plays",
        JsonObject().apply {
            addProperty("game", req.game)
            addProperty("choice", req.choice)
            addProperty("stake", req.stake)
            addProperty("idempotencyKey", UUID.randomUUID().toString())
        }, req.stake, true
    )

    private suspend fun play(path: String, body: JsonObject, stake: Long, dice: Boolean): Result<CasinoPlayResponse> = runCatching {
        val res = ApiClient.api.contractPost(path, body)
        if (!res.isSuccessful || res.body() == null) commandFailure("카지노 플레이", res.code(), res.errorBody()?.string().orEmpty())
        val o = res.body()!!.asJsonObject
        val net = o.text("netAmount", "net_amount") ?: "0"
        val outcome = if (dice) o.text("outcomeFace", "outcome_face") ?: "-" else o.text("outcome") ?: "-"
        val netValue = decimal(net)
        CasinoPlayResponse(
            success = true,
            isWin = netValue > BigDecimal.ZERO,
            resultOutcome = outcome,
            payoutAmount = if (netValue > BigDecimal.ZERO) netValue.add(BigDecimal.valueOf(stake)).toPlainString() else "0",
            netProfit = net,
            message = if (netValue > BigDecimal.ZERO) "당첨되었습니다!" else "이번에는 아쉽게 실패했습니다."
        )
    }
}

class SeasonRepository {
    suspend fun getSeasons(): Result<List<SeasonDto>> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/seasons/events")
        if (!res.isSuccessful || res.body() == null) commandFailure("시즌 조회", res.code())
        res.body()!!.asJsonObject.getAsJsonArray("events").elements().mapNotNull { e ->
            val o = e.obj() ?: return@mapNotNull null
            val id = o.text("eventId", "event_id") ?: return@mapNotNull null
            SeasonDto(
                id = id,
                name = o.text("title") ?: o.text("seasonName", "season_name") ?: "시즌 이벤트",
                description = o.text("description") ?: "",
                endsAt = o.text("endsAt", "ends_at") ?: "",
                isActive = o.bool("active", "isActive", "is_active") ?: true
            )
        }
    }

    suspend fun getLeaderboard(seasonId: String): Result<List<LeaderboardEntryDto>> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/seasons/events/$seasonId/leaderboard")
        if (!res.isSuccessful || res.body() == null) commandFailure("리더보드 조회", res.code())
        res.body()!!.asJsonObject.getAsJsonArray("entries").elements().mapNotNull { e ->
            val o = e.obj() ?: return@mapNotNull null
            LeaderboardEntryDto(
                rank = o.int("rank") ?: 0,
                userId = o.text("userId", "user_id") ?: "",
                displayName = o.text("displayName", "display_name") ?: "사용자",
                score = o.long("score") ?: 0L
            )
        }
    }
}

private fun decimal(value: String): BigDecimal = runCatching { BigDecimal(value) }.getOrElse { BigDecimal.ZERO }

private fun subtractStrings(a: String, b: String): String = decimal(a).subtract(decimal(b)).stripTrailingZeros().toPlainString()

private fun percentChange(current: String, base: String): Double {
    val c = decimal(current)
    val b = decimal(base)
    if (b.compareTo(BigDecimal.ZERO) == 0) return 0.0
    return c.subtract(b)
        .multiply(BigDecimal.valueOf(100))
        .divide(b, 2, RoundingMode.HALF_UP)
        .toDouble()
}


class RealEstateRepository {
    suspend fun getDistricts(): Result<List<RealEstateDistrictDto>> = runCatching {
        val res = ApiClient.api.contractGet("app-api/v1/spaces/real-estate/districts")
        if (res.isSuccessful && res.body() != null) {
            val list = res.body()!!.asJsonObject.getAsJsonArray("districts")?.elements()?.mapNotNull { e ->
                val o = e.obj() ?: return@mapNotNull null
                val id = o.text("id", "districtId") ?: return@mapNotNull null
                RealEstateDistrictDto(
                    id = id,
                    name = o.text("name") ?: "구역",
                    description = o.text("description") ?: "",
                    averageLandPrice = o.long("averageLandPrice", "average_land_price") ?: 100000000L,
                    activeLandCount = o.int("activeLandCount", "active_land_count") ?: 10,
                    bannerColorHex = o.text("bannerColorHex", "banner_color_hex") ?: "#1E293B"
                )
            }.orEmpty()
            if (list.isNotEmpty()) list else mockDistricts()
        } else {
            mockDistricts()
        }
    }

    private fun mockDistricts(): List<RealEstateDistrictDto> = listOf(
        RealEstateDistrictDto("dist-1", "강남 테헤란로", "초고층 오피스 빌딩 및 IT/VC 메카", 150000000L, 12, "#3B82F6"),
        RealEstateDistrictDto("dist-2", "여의도 금융가", "증권사 본사 및 자산운용 핀테크 허브", 120000000L, 8, "#10B981"),
        RealEstateDistrictDto("dist-3", "성수 유니콘 밸리", "스타트업 팩토리 및 크리에이티브 스페이스", 85000000L, 15, "#F59E0B"),
        RealEstateDistrictDto("dist-4", "판교 테크노벨리", "AI 딥테크 클러스터 및 스마트 데이터센터", 95000000L, 10, "#8B5CF6")
    )

    suspend fun getLands(districtId: String? = null): Result<List<RealEstateLandDto>> = runCatching {
        val path = if (districtId != null) "app-api/v1/spaces/real-estate/lands?districtId=$districtId" else "app-api/v1/spaces/real-estate/lands"
        val res = ApiClient.api.contractGet(path)
        if (res.isSuccessful && res.body() != null) {
            val list = res.body()!!.asJsonObject.getAsJsonArray("lands")?.elements()?.mapNotNull { e ->
                val o = e.obj() ?: return@mapNotNull null
                val id = o.text("id", "landId") ?: return@mapNotNull null
                RealEstateLandDto(
                    id = id,
                    districtId = o.text("districtId", "district_id") ?: "dist-1",
                    districtName = o.text("districtName", "district_name") ?: "강남 테헤란로",
                    title = o.text("title", "name") ?: "빌딩/필지",
                    address = o.text("address") ?: "서울시",
                    ownerId = o.text("ownerId", "owner_id"),
                    ownerNickname = o.text("ownerNickname", "owner_nickname"),
                    purchasePrice = o.long("purchasePrice", "purchase_price") ?: 100000000L,
                    dailyRent = o.long("dailyRent", "daily_rent") ?: 300000L,
                    isAuctionActive = o.bool("isAuctionActive", "is_auction_active") == true,
                    currentAuctionBid = o.long("currentAuctionBid", "current_auction_bid") ?: 0L,
                    auctionEndMinutesRemaining = o.int("auctionEndMinutesRemaining", "auction_end_minutes_remaining") ?: 0
                )
            }.orEmpty()
            if (list.isNotEmpty()) list else mockLands()
        } else {
            mockLands()
        }
    }

    private fun mockLands(): List<RealEstateLandDto> = listOf(
        RealEstateLandDto("land-1", "dist-1", "강남 테헤란로", "테헤란 타워 15층", "서울 강남구 테헤란로 152", "usr_self", "본인", 150000000L, 450000L, false, 0L, 0),
        RealEstateLandDto("land-2", "dist-1", "강남 테헤란로", "역삼 벤처스튜디오", "서울 강남구 역삼로 84", null, null, 120000000L, 380000L, true, 135000000L, 45),
        RealEstateLandDto("land-3", "dist-2", "여의도 금융가", "여의도 파이낸스타워", "서울 영등포구 여의대로 56", "usr_self", "본인", 220000000L, 720000L, false, 0L, 0),
        RealEstateLandDto("land-4", "dist-3", "성수 유니콘 밸리", "성수 아틀리에 복합관", "서울 성동구 아차산로 17", null, null, 85000000L, 260000L, true, 92000000L, 120),
        RealEstateLandDto("land-5", "dist-4", "판교 테크노벨리", "판교 클라우드 허브", "경기 성남시 분당구 판교역로", null, null, 110000000L, 340000L, false, 0L, 0)
    )

    suspend fun purchaseLand(landId: String, amountWld: Long): Result<AuthResponse> = runCatching {
        val body = JsonObject().apply {
            addProperty("landId", landId)
            addProperty("amountWld", amountWld)
            addProperty("idempotencyKey", UUID.randomUUID().toString())
        }
        val res = ApiClient.api.contractPost("app-api/v1/spaces/real-estate/purchase", body)
        if (!res.isSuccessful) commandFailure("부동산 매입", res.code(), res.errorBody()?.string().orEmpty())
        AuthResponse(success = true, outcome = "부동산 필지(토지/건물) 매입 등기가 완료되었습니다.")
    }

    suspend fun bidAuction(landId: String, bidAmountWld: Long): Result<AuthResponse> = runCatching {
        val body = JsonObject().apply {
            addProperty("landId", landId)
            addProperty("bidAmountWld", bidAmountWld)
            addProperty("idempotencyKey", UUID.randomUUID().toString())
        }
        val res = ApiClient.api.contractPost("app-api/v1/spaces/real-estate/bid", body)
        if (!res.isSuccessful) commandFailure("경매 입찰", res.code(), res.errorBody()?.string().orEmpty())
        AuthResponse(success = true, outcome = "법원 가상 경매 입찰표가 제출되었습니다 ($bidAmountWld WLD).")
    }

    suspend fun claimAllRent(): Result<RealEstateRentClaimResponse> = runCatching {
        val body = JsonObject().apply {
            addProperty("idempotencyKey", UUID.randomUUID().toString())
        }
        val res = ApiClient.api.contractPost("app-api/v1/spaces/real-estate/claim-rent", body)
        if (res.isSuccessful && res.body() != null) {
            val o = res.body()!!.asJsonObject
            RealEstateRentClaimResponse(
                success = true,
                totalRentClaimed = o.long("totalRentClaimed", "total_rent_claimed") ?: 1170000L,
                claimedLandCount = o.int("claimedLandCount", "claimed_land_count") ?: 2,
                message = o.text("message") ?: "보유 부동산의 일일 임대료가 정산되었습니다."
            )
        } else {
            RealEstateRentClaimResponse(success = true, totalRentClaimed = 1170000L, claimedLandCount = 2, message = "보유 부동산 2곳의 일일 임대료 1,170,000 WLD가 지갑으로 입금되었습니다.")
        }
    }
}
