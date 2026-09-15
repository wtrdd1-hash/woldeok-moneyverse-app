package com.example.woldeokmoneyverse.data.model

import com.google.gson.annotations.SerializedName

// --- Auth DTOs ---
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class VerifyEmailRequest(
    val token: String
)

data class ConsentRequest(
    val termsCompleted: Boolean = true,
    val privacyCompleted: Boolean = true,
    val ageConfirmed: Boolean = true,
    val termsVersion: String = "2026-09-02",
    val privacyVersion: String = "2026-09-02"
)

data class PolicyVersions(
    val termsVersion: String,
    val privacyVersion: String
)

data class HandoffRequest(
    val code: String
)

data class ViewerResponse(
    val signedIn: Boolean = false,
    val csrfToken: String? = null,
    val user: UserProfileDto? = null
)

data class AuthResponse(
    val success: Boolean = true,
    /** Contract value for successful sign-in and mobile handoff responses. */
    val outcome: String? = null,
    /** Present on local-registration responses; false means no email was sent. */
    val accepted: Boolean? = null,
    val userId: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val csrfToken: String? = null,
    val consentCurrent: Boolean? = null,
    val verificationToken: String? = null,
    val verificationRequired: Boolean = false,
    val message: String? = null
)

data class AuthProviderDto(
    @SerializedName("id") val provider: String,
    val enabled: Boolean
)

data class AuthProvidersResponse(
    val providers: List<AuthProviderDto> = emptyList()
)

// --- Wallet & Banking DTOs ---
data class WalletOverviewResponse(
    val userId: String,
    val balances: WalletBalancesDto,
    val recentTransactions: List<WalletTransactionDto> = emptyList()
) {
    // UI-facing compatibility values derived from the authoritative response.
    val cashBalance: String get() = balances.cash.availableAmount
    val bankBalance: String get() = balances.bank.availableAmount
    val netWorth: String get() = balances.totalAvailableAmount
    val recentLedger: List<LedgerEntry>
        get() = recentTransactions.map {
            LedgerEntry(
                id = it.transactionId,
                type = it.type,
                amount = it.netAmount,
                description = it.label,
                createdAt = it.occurredAt
            )
        }
}

data class WalletBalancesDto(
    val currency: String,
    val cash: WalletBalanceDto,
    val bank: WalletBalanceDto,
    val totalAvailableAmount: String
)

data class WalletBalanceDto(
    val availableAmount: String,
    val updatedAt: String
)

data class WalletTransactionDto(
    val transactionId: String,
    val type: String,
    val label: String,
    val netAmount: String,
    val direction: String,
    val occurredAt: String
)

data class LedgerEntry(
    val id: String,
    val type: String,
    val amount: String,
    val description: String,
    val createdAt: String
)

data class TransferRequest(
    val recipientUserId: String,
    val amount: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString(),
    val memo: String? = null
)

data class BankMovementRequest(
    val direction: String, // "deposit" or "withdraw" (lowercase)
    val amount: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class LoanDto(
    val id: String,
    val principal: String,
    var remainingBalance: String,
    val interestRate: Double,
    val dueDate: String,
    val status: String
)

data class BorrowRequest(
    val amount: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class RepayRequest(
    val amount: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

// --- Profile & Activity DTOs ---
/** `/profile` is a full replacement, not a patch. */
data class UpdateProfileRequest(
    val visibility: String,
    val displayName: String,
    val imageUrl: String? = null,
    val fieldVisibility: Map<String, String>,
    val featuredTitle: String? = null
)

data class ActivityLogDto(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val createdAt: String
)

data class PrivacyRequestDto(
    val type: String, // "EXPORT" or "DELETE"
    val status: String,
    val requestedAt: String
)

// --- Rewards & Work DTOs ---
data class DailyClaimResponse(
    val success: Boolean,
    val claimedAmount: String,
    val nextEligibleAt: String,
    val message: String? = null
)

data class DailyClaimApiResponse(
    val transactionId: String? = null,
    val amount: String? = null,
    val replayed: Boolean = false
)

/**
 * State-changing reward claims use the same request-level idempotency
 * contract as the other economy operations.  Without this payload the BFF
 * rejects the request during input validation with HTTP 400.
 */
data class DailyClaimRequest(
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class WorkProfileResponse(
    @SerializedName("active_job") val activeJob: WorkJobProgressDto? = null,
    @SerializedName("all_jobs") val allJobs: List<WorkJobProgressDto> = emptyList()
)

data class WorkJobProgressDto(
    @SerializedName("job_type") val jobType: String? = null,
    val level: Int = 1,
    val experience: String = "0",
    @SerializedName("next_level_exp") val nextLevelExp: String = "100",
    @SerializedName("is_active") val isActive: Boolean = false
)

data class WorkDashboardResponse(
    @SerializedName("daily_paid") val dailyPaid: String? = null,
    @SerializedName("daily_cap") val dailyCap: String? = null,
    @SerializedName("weekly_paid") val weeklyPaid: String? = null,
    @SerializedName("weekly_cap") val weeklyCap: String? = null,
    @SerializedName("active_assignments") val activeAssignments: String? = null
)

data class WorkStatusDto(
    val jobTitle: String,
    val canWork: Boolean,
    val cooldownSeconds: Int,
    val estimatedReward: String,
    val lastWorkedAt: String? = null
)

data class WorkExecuteResponse(
    val success: Boolean,
    val earnedAmount: String,
    val expGained: Int,
    val nextWorkAvailableAt: String,
    val message: String? = null
)

data class ProgressionDto(
    val level: Int,
    val currentExp: Int,
    val requiredExp: Int,
    val title: String,
    // The production API may send this optional field as JSON null for a new user.
    // Gson passes that null through even when a Kotlin default value is declared.
    val unlockedFeatures: List<String>? = emptyList()
)

data class EarlyGameTaskDto(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val rewardAmount: String,
    val targetDeepLink: String? = null
)

/** `GET /early-game/today` wraps a nullable event in an object. */
data class TodayEarlyGameResponse(
    val event: TodayEarlyGameEvent? = null
)

data class TodayEarlyGameEvent(
    val event_date: String,
    val event_code: String,
    val event_label: String,
    val event_detail: String,
    val reward_amount: String,
    val reward_experience: String,
    val claimed: Boolean,
    val claim_block: String? = null
)


// --- Persistent member <-> administrator support chat ---
data class SupportThreadDto(
    @SerializedName(value = "threadId", alternate = ["thread_id"]) val threadId: String,
    val subject: String,
    val status: String,
    @SerializedName(value = "createdAt", alternate = ["created_at"]) val createdAt: String? = null,
    @SerializedName(value = "lastMessageAt", alternate = ["last_message_at"]) val lastMessageAt: String? = null,
    @SerializedName(value = "userId", alternate = ["user_id"]) val userId: String? = null,
    @SerializedName(value = "displayName", alternate = ["display_name"]) val displayName: String? = null
)

data class SupportMessageDto(
    @SerializedName(value = "messageId", alternate = ["message_id"]) val messageId: String,
    @SerializedName(value = "senderKind", alternate = ["sender_kind"]) val senderKind: String,
    val body: String,
    @SerializedName(value = "createdAt", alternate = ["created_at"]) val createdAt: String? = null
)

data class SupportThreadsResponse(val threads: List<SupportThreadDto> = emptyList())
data class SupportMessagesResponse(val messages: List<SupportMessageDto> = emptyList())
data class SupportThreadResponse(val thread: SupportThreadDto)
data class SupportMessageResponse(val message: SupportMessageDto)
data class CreateSupportThreadRequest(val subject: String, val body: String, val idempotencyKey: String = java.util.UUID.randomUUID().toString())
data class CreateSupportMessageRequest(val body: String, val idempotencyKey: String = java.util.UUID.randomUUID().toString())
data class SupportStatusRequest(val status: String)

// --- Stocks DTOs ---
data class StockDto(
    val id: String,
    val symbol: String,
    val name: String,
    val currentPrice: String,
    val priceChangePercent: Double,
    val isMarketOpen: Boolean,
    val historyPrices: List<Double> = emptyList()
)

data class StockPortfolioDto(
    var totalStockValue: String,
    val holdings: MutableList<StockHoldingDto> = mutableListOf()
)

data class StockHoldingDto(
    val stockId: String,
    val symbol: String,
    val name: String,
    var quantity: Int,
    val averageBuyPrice: String,
    var currentPrice: String,
    var totalValue: String,
    val profitLossPercent: Double
)

data class StockOrderRequest(
    val side: String, // "buy" or "sell"
    val quantity: Int,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class StockOrderResponse(
    val success: Boolean,
    val executedPrice: String,
    val totalCost: String,
    val message: String? = null
)

// --- Business DTOs ---
data class BusinessListResponse(val businesses: List<BusinessDto> = emptyList())
data class BusinessCatalogResponse(val businessTypes: List<BusinessTypeDto> = emptyList())
data class BusinessEquityResponse(val equity: BusinessEquityDto)

data class BusinessTypeDto(
    val id: String,
    val name: String,
    val category: String,
    /** Canonical mobile-contract field. `purchasePrice` is accepted from older servers. */
    @SerializedName(value = "purchaseCost", alternate = ["purchasePrice"])
    val purchaseCost: String,
    /** Canonical mobile-contract field. `baseRevenuePerSettlement` is legacy. */
    @SerializedName(value = "dailyRevenue", alternate = ["baseRevenuePerSettlement"])
    val dailyRevenue: String,
    val requiredLevel: Int,
    val description: String,
    /** May be omitted by older servers; never render a literal null in the UI. */
    @SerializedName(value = "dailyOperatingCost", alternate = ["operatingCost"])
    val dailyOperatingCost: String? = null
) {
    // UI compatibility for already released app views.
    val purchasePrice: String get() = purchaseCost
    val baseRevenuePerSettlement: String get() = dailyRevenue
}

data class BusinessDto(
    val id: String,
    val typeId: String,
    val name: String,
    var level: Int,
    var isSettlementReady: Boolean,
    val nextSettlementAt: String,
    @SerializedName(value = "pendingRevenue", alternate = ["unsettledRevenue"])
    var pendingRevenue: String,
    val licenseActive: Boolean,
    @SerializedName(value = "dailyRevenue", alternate = ["revenuePerDay"])
    val dailyRevenue: String? = null,
    @SerializedName(value = "dailyOperatingCost", alternate = ["operatingCost"])
    val dailyOperatingCost: String? = null
)

data class BusinessEquityDto(
    val availableEquity: String,
    val totalBusinessValuation: String,
    val maxLoanCapacity: String
)

data class BusinessPurchaseRequest(
    val catalogCode: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class BusinessSettlementResponse(
    val success: Boolean,
    val collectedAmount: String,
    val nextSettlementAt: String,
    val message: String? = null
)

// --- Casino DTOs (`CAS-001`) ---
data class CasinoPlayRequest(
    val choice: String, // "heads" or "tails"
    val stake: Long
)

data class CasinoDiceRequest(
    val game: String, // "dice_parity" or "dice_number"
    val choice: String, // "odd", "even", "1".."6"
    val stake: Long
)

data class CasinoPlayResponse(
    val success: Boolean,
    val isWin: Boolean,
    val resultOutcome: String,
    val payoutAmount: String,
    val netProfit: String,
    val message: String
)

data class CasinoSelfLimitDto(
    val dailyBetLimit: Long,
    val dailyLossLimit: Long,
    val lockedUntil: String? = null
)

// --- Seasons DTOs (`SEA-001`) ---
data class SeasonDto(
    val id: String,
    val name: String,
    val description: String,
    val endsAt: String,
    val currentProgress: Int,
    val totalMilestone: Int
)

data class LeaderboardEntryDto(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val score: String,
    val title: String
)

// --- Shop DTOs ---
data class ShopItemDto(
    val id: String,
    val name: String,
    val category: String,
    val price: String,
    val description: String,
    var isOwned: Boolean,
    val iconUrl: String? = null
)

data class ShopPurchaseDto(
    val purchaseId: String,
    val itemId: String,
    val itemName: String,
    val transactionId: String,
    val amount: String,
    val purchasedAt: String
)

data class ShopPurchasesResponse(
    val purchases: List<ShopPurchaseDto> = emptyList()
)

data class ShopPurchaseRequest(
    val quantity: Int = 1,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

// --- Community & Content DTOs ---
data class BoardPostDto(
    @SerializedName(value = "postId", alternate = ["id"])
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "알 수 없음",
    val authorAvatarUrl: String? = null,
    val title: String = "제목 없음",
    /** The list contract intentionally omits content; it is available from detail. */
    val content: String = "내용은 상세에서 확인할 수 있습니다.",
    var commentCount: Int = 0,
    val createdAt: String = "",
    val comments: MutableList<CommentDto> = mutableListOf()
)

data class BoardPostsResponse(val posts: List<BoardPostDto> = emptyList())

data class CommentDto(
    val id: String,
    val authorName: String,
    val content: String,
    val createdAt: String
)

data class AddCommentRequest(
    val content: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class CreatePostRequest(
    val title: String,
    val content: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class UserProfileDto(
    val userId: String,
    var displayName: String,
    val email: String,
    val level: Int,
    val experience: String = "0",
    val nextLevelExperience: String = "100",
    val jobType: String? = null,
    val title: String,
    var bio: String? = "월덕 머니버서 주식 및 사업 투자자",
    val joinedAt: String
)

/** Raw `/profile` contract.  The server may include the same values in the
 * nested `profile` object for backward compatibility. */
data class ProfileResponse(
    @SerializedName(value = "displayName", alternate = ["display_name"])
    val displayName: String? = null,
    val email: String? = null,
    @SerializedName(value = "joinedAt", alternate = ["joined_at"])
    val joinedAt: String? = null,
    @SerializedName(value = "jobType", alternate = ["job_type"])
    val jobType: String? = null,
    @SerializedName(value = "jobLevel", alternate = ["job_level"])
    val jobLevel: Int? = null,
    @SerializedName(value = "featuredTitle", alternate = ["featured_title"])
    val featuredTitle: String? = null,
    val imageUrl: String? = null,
    val visibility: String? = null,
    val fieldVisibility: Map<String, String>? = null,
    val profile: ProfilePayload? = null
)

data class ProfilePayload(
    @SerializedName(value = "displayName", alternate = ["display_name"])
    val displayName: String? = null,
    val email: String? = null,
    @SerializedName(value = "joinedAt", alternate = ["joined_at"])
    val joinedAt: String? = null,
    @SerializedName(value = "jobType", alternate = ["job_type"])
    val jobType: String? = null,
    @SerializedName(value = "jobLevel", alternate = ["job_level"])
    val jobLevel: Int? = null,
    @SerializedName(value = "featuredTitle", alternate = ["featured_title"])
    val featuredTitle: String? = null
)

data class ProgressionResponse(val progression: ProgressionPayload? = null)

data class ProgressionPayload(
    @SerializedName(value = "stageCode", alternate = ["stage_code"])
    val stageCode: String? = null,
    @SerializedName(value = "reachedAt", alternate = ["reached_at"])
    val reachedAt: String? = null,
    @SerializedName(value = "nextStageCode", alternate = ["next_stage_code"])
    val nextStageCode: String? = null,
    @SerializedName(value = "nextRequirements", alternate = ["next_requirements"])
    val nextRequirements: Map<String, Int>? = null
)

data class PhotoDto(
    @SerializedName(value = "photoId", alternate = ["id"])
    val id: String = "",
    @SerializedName(value = "altText", alternate = ["title"])
    val title: String = "설명 없음",
    val imageUrl: String = "",
    val category: String = "갤러리",
    val likes: Int = 0,
    val publishedAt: String? = null
)

data class GalleryPhotosResponse(val photos: List<PhotoDto> = emptyList())

data class PhotoSubmissionDto(
    @SerializedName("photo_id") val photoId: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("alt_text") val altText: String,
    val published: Boolean,
    @SerializedName("submitted_at") val submittedAt: String,
    @SerializedName("published_at") val publishedAt: String? = null
)

data class MyPhotosResponse(
    val submissions: List<PhotoSubmissionDto> = emptyList()
)

data class AnnouncementDto(
    val id: String,
    val title: String,
    val content: String,
    val isImportant: Boolean,
    val createdAt: String
)

data class ServiceStatusDto(
    val status: String,
    val notice: String? = null,
    val updatedAt: String = ""
)

data class ServiceStatusResponse(val status: List<ServiceStatusItem> = emptyList())
data class ServiceStatusItem(
    val sourceKey: String = "",
    val displayName: String = "서비스",
    val state: String = "unknown",
    val detail: String? = null,
    val observedAt: String? = null
)
