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

data class WorkTaskDto(
    @SerializedName(value = "taskId", alternate = ["task_id", "id"]) val taskId: String,
    val code: String = "",
    val name: String,
    val description: String,
    @SerializedName(value = "jobType", alternate = ["job_type"]) val jobType: String = "general",
    val difficulty: Int = 1,
    @SerializedName(value = "baseReward", alternate = ["base_reward"]) val baseReward: String,
    @SerializedName(value = "baseExperience", alternate = ["base_experience"]) val baseExperience: String,
    @SerializedName(value = "minimumDurationSeconds", alternate = ["minimum_duration_seconds"]) val minimumDurationSeconds: Int = 0,
    @SerializedName(value = "dailyLimit", alternate = ["daily_limit"]) val dailyLimit: Int = 10,
    @SerializedName(value = "takenToday", alternate = ["taken_today"]) val takenToday: Int = 0,
    val recommended: Boolean = false
)

data class WorkTasksResponse(
    val featureState: String = "enabled",
    val tasks: List<WorkTaskDto> = emptyList()
)

data class WorkCompleteTaskRequest(
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class WorkCompleteTaskResponse(
    @SerializedName(value = "rewardAmount", alternate = ["reward_amount"]) val rewardAmount: String? = null,
    @SerializedName(value = "experienceGained", alternate = ["experience_gained"]) val experienceGained: String? = null,
    @SerializedName(value = "currentLevel", alternate = ["current_level"]) val currentLevel: Int = 1,
    @SerializedName(value = "currentExperience", alternate = ["current_experience"]) val currentExperience: String? = null,
    @SerializedName(value = "levelUp", alternate = ["level_up"]) val levelUp: Boolean = false,
    @SerializedName(value = "transactionId", alternate = ["transaction_id"]) val transactionId: String? = null
)

data class WorkActiveJobRequest(
    val jobType: String
)

data class WorkProfileDto(
    val level: Int = 1,
    val experience: String = "0",
    val completedTasksCount: Int = 0,
    val totalEarnings: String = "0",
    val currentJob: String = "인턴"
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


// --- Member <-> member private chat ---
data class ChatConversationDto(
    @SerializedName(value = "conversationId", alternate = ["conversation_id"]) val conversationId: String,
    val state: String = "active",
    @SerializedName(value = "latestSequence", alternate = ["latest_sequence"]) val latestSequence: String = "0",
    @SerializedName(value = "peerUserId", alternate = ["peer_user_id"]) val peerUserId: String,
    @SerializedName(value = "peerDisplayName", alternate = ["peer_display_name"]) val peerDisplayName: String = "회원",
    @SerializedName(value = "unreadCount", alternate = ["unread_count"]) val unreadCount: String = "0",
    val muted: Boolean = false,
    val archived: Boolean = false,
    @SerializedName(value = "lastMessageBody", alternate = ["last_message_body"]) val lastMessageBody: String? = null,
    @SerializedName(value = "isPeerBlocked", alternate = ["is_peer_blocked"]) val isPeerBlocked: Boolean = false
)
data class ChatMessageDto(
    val id: String,
    @SerializedName(value = "conversationId", alternate = ["conversation_id"]) val conversationId: String,
    @SerializedName(value = "senderId", alternate = ["sender_id"]) val senderId: String,
    val sequence: String,
    val body: String,
    @SerializedName(value = "createdAt", alternate = ["created_at"]) val createdAt: String? = null,
    @SerializedName(value = "isMine", alternate = ["is_mine"]) val isMine: Boolean = false
)
data class ChatConversationsResponse(val conversations: List<ChatConversationDto> = emptyList(), val totalUnread: Int = 0)
data class ChatMessagesResponse(val messages: List<ChatMessageDto> = emptyList())
data class OpenChatRequest(val peerUserId: String)
data class OpenChatResponse(@SerializedName(value = "conversationId", alternate = ["conversation_id"]) val conversationId: String)
data class SendChatMessageRequest(val body: String, val idempotencyKey: String = java.util.UUID.randomUUID().toString())
data class MarkChatReadRequest(val sequence: Int)
data class ChatToggleRequest(val archived: Boolean? = null, val muted: Boolean? = null)
data class ChatReportRequest(val reason: String, val details: String)
data class ChatReportResponse(val ok: Boolean = false, val reportId: String? = null)

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

// --- Authoritative server game clock / administrator observability ---
data class GameClockDto(
    @SerializedName(value = "policy_version", alternate = ["policyVersion"]) val policyVersion: String,
    @SerializedName(value = "day_index", alternate = ["dayIndex"]) val dayIndex: String,
    @SerializedName(value = "week_index", alternate = ["weekIndex"]) val weekIndex: String,
    @SerializedName(value = "day_of_week", alternate = ["dayOfWeek"]) val dayOfWeek: Int,
    @SerializedName(value = "real_seconds_per_day", alternate = ["realSecondsPerDay"]) val realSecondsPerDay: Int,
    @SerializedName(value = "day_ends_at", alternate = ["dayEndsAt"]) val dayEndsAt: String
)

data class AdminActivityLogDto(
    val id: String,
    @SerializedName(value = "user_id", alternate = ["userId"]) val userId: String? = null,
    val username: String = "",
    @SerializedName(value = "event_type", alternate = ["eventType"]) val eventType: String,
    val path: String,
    @SerializedName(value = "target_label", alternate = ["targetLabel"]) val targetLabel: String? = null,
    val ip: String? = null,
    @SerializedName(value = "created_at", alternate = ["createdAt"]) val createdAt: String
) {
    val action: String get() = eventType
    val details: String get() = targetLabel ?: path
    val actor: String get() = username.ifBlank { userId ?: "시스템" }
}

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
) {
    val outcome: String get() = if (isWin) "WIN" else resultOutcome
    val payout: String get() = payoutAmount
    val stake: String get() = netProfit
}

data class CasinoSelfLimitDto(
    val dailyBetLimit: Long,
    val dailyLossLimit: Long,
    val lockedUntil: String? = null
)

data class CasinoTermsDto(
    val enabled: Boolean = true,
    @SerializedName("min_stake") val minStake: String = "0",
    @SerializedName("max_stake") val maxStake: String = "0",
    @SerializedName("daily_stake_limit") val dailyStakeLimit: String = "0",
    @SerializedName("daily_loss_limit") val dailyLossLimit: String = "0",
    @SerializedName("daily_stake_used") val dailyStakeUsed: String = "0",
    @SerializedName("daily_loss_used") val dailyLossUsed: String = "0",
    @SerializedName("remaining_stake") val remainingStake: String = "0",
    @SerializedName("remaining_loss") val remainingLoss: String = "0"
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
    @SerializedName(value = "body", alternate = ["content"]) val content: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString(),
    val imageStorageKey: String? = null,
    val imageAltText: String? = null
)

data class UpdatePostRequest(
    val title: String,
    @SerializedName(value = "body", alternate = ["content"]) val content: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString(),
    val imageStorageKey: String? = null,
    val imageAltText: String? = null
)

data class PostDetailResponse(
    val post: BoardPostDto
)

data class BoardCommentDto(
    @SerializedName(value = "commentId", alternate = ["id"]) val id: String = "",
    val postId: String = "",
    val authorUserId: String = "",
    @SerializedName(value = "authorUsername", alternate = ["authorName"]) val authorUsername: String = "익명",
    val authorRole: String = "MEMBER",
    @SerializedName(value = "body", alternate = ["content"]) val body: String = "",
    val createdAt: String = ""
) {
    val commentId: String get() = id
    val authorName: String get() = authorUsername
    val content: String get() = body
}

data class BoardCommentsResponse(
    val comments: List<BoardCommentDto> = emptyList()
)

data class CreateBoardCommentRequest(
    @SerializedName(value = "body", alternate = ["content"]) val body: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class CommentMutationResponse(
    val comment: BoardCommentDto
)

data class DeleteMutationRequest(
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

// --- Banking & Bonds DTOs ---
data class BondHoldingDto(
    val id: String = "",
    @SerializedName(value = "bondCode", alternate = ["bond_code"])
    val bondCode: String = "",
    val amount: String = "0",
    @SerializedName(value = "interestRate", alternate = ["interest_rate"])
    val interestRate: Double = 0.0,
    @SerializedName(value = "maturityAt", alternate = ["maturity_at"])
    val maturityAt: String? = null,
    @SerializedName(value = "isRedeemable", alternate = ["is_redeemable"])
    val isRedeemable: Boolean = false
)

data class BankingStandingDto(
    @SerializedName(value = "cashBalance", alternate = ["cash_balance"])
    val cashBalance: String? = null,
    @SerializedName(value = "depositBalance", alternate = ["deposit_balance"])
    val depositBalance: String? = null,
    @SerializedName(value = "accruedInterest", alternate = ["accrued_interest"])
    val accruedInterest: String? = null,
    @SerializedName(value = "creditGrade", alternate = ["credit_grade"])
    val creditGrade: String? = null,
    @SerializedName(value = "loanLimit", alternate = ["loan_limit"])
    val loanLimit: String? = null,
    @SerializedName(value = "activeBonds", alternate = ["active_bonds"])
    val activeBonds: List<BondHoldingDto> = emptyList()
)

data class BondPurchaseRequest(
    val bondCode: String,
    val amount: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class IdempotentRequest(
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

// --- Shop Holdings & Inventory DTOs ---
data class ShopHoldingDto(
    @SerializedName(value = "catalogId", alternate = ["catalog_id", "id"])
    val catalogId: String = "",
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val quantity: Int = 1,
    @SerializedName(value = "acquiredAt", alternate = ["acquired_at"])
    val acquiredAt: String = "",
    @SerializedName(value = "expiresAt", alternate = ["expires_at"])
    val expiresAt: String? = null,
    @SerializedName(value = "effectKind", alternate = ["effect_kind"])
    val effectKind: String = "",
    val rarity: String = "COMMON",
    @SerializedName(value = "isEquipped", alternate = ["is_equipped"])
    val isEquipped: Boolean = false,
    @SerializedName(value = "equippedSlot", alternate = ["equipped_slot"])
    val equippedSlot: String? = null,
    @SerializedName(value = "serialNumber", alternate = ["serial_number"])
    val serialNumber: Long? = null,
    val durable: Boolean = false,
    @SerializedName(value = "weeklyCost", alternate = ["weekly_cost"])
    val weeklyCost: String? = null,
    @SerializedName(value = "unpaidWeeks", alternate = ["unpaid_weeks"])
    val unpaidWeeks: Int = 0
)

data class ShopHoldingsResponse(
    val holdings: List<ShopHoldingDto> = emptyList()
)

data class ConsumeHoldingResponse(
    @SerializedName(value = "catalogId", alternate = ["catalog_id"])
    val catalogId: String? = null,
    @SerializedName(value = "remainingQuantity", alternate = ["remaining_quantity"])
    val remainingQuantity: Int? = null,
    val replayed: Boolean = false
)

data class EquipHoldingResponse(
    val success: Boolean = true,
    @SerializedName(value = "catalogId", alternate = ["catalog_id"])
    val catalogId: String? = null,
    val slot: String? = null,
    @SerializedName(value = "isEquipped", alternate = ["is_equipped"])
    val isEquipped: Boolean = false
)

data class UpkeepSettlementResponse(
    @SerializedName(value = "paidAmount", alternate = ["paid_amount"])
    val paidAmount: String? = null,
    @SerializedName(value = "ledgerTransactionId", alternate = ["ledger_transaction_id"])
    val ledgerTransactionId: String? = null,
    val replayed: Boolean = false
)

// --- Stock Candles, Watchlist & Alerts DTOs ---
data class StockCandleDto(
    @SerializedName(value = "bucketAt", alternate = ["bucket_at"])
    val bucketAt: String = "",
    @SerializedName(value = "openPrice", alternate = ["open_price"])
    val openPrice: String = "0",
    @SerializedName(value = "highPrice", alternate = ["high_price"])
    val highPrice: String = "0",
    @SerializedName(value = "lowPrice", alternate = ["low_price"])
    val lowPrice: String = "0",
    @SerializedName(value = "closePrice", alternate = ["close_price"])
    val closePrice: String = "0"
)

data class StockCandlesResponse(
    val interval: Long = 86400,
    val candles: List<StockCandleDto> = emptyList()
)

data class WatchlistStockDto(
    @SerializedName(value = "stockId", alternate = ["stock_id"])
    val stockId: String = "",
    val symbol: String = "",
    val name: String = "",
    @SerializedName(value = "currentPrice", alternate = ["current_price"])
    val currentPrice: String = "0",
    @SerializedName(value = "dayOpenPrice", alternate = ["day_open_price"])
    val dayOpenPrice: String = "0"
)

data class StockWatchlistResponse(
    val stocks: List<WatchlistStockDto> = emptyList()
)

data class StockAlertDto(
    @SerializedName(value = "alertId", alternate = ["alert_id"])
    val alertId: String = "",
    @SerializedName(value = "stockId", alternate = ["stock_id"])
    val stockId: String = "",
    val symbol: String = "",
    val name: String = "",
    @SerializedName(value = "conditionKind", alternate = ["condition_kind"])
    val conditionKind: String = "PRICE_ABOVE",
    @SerializedName(value = "thresholdAmount", alternate = ["threshold_amount"])
    val thresholdAmount: String? = null,
    @SerializedName(value = "currentPrice", alternate = ["current_price"])
    val currentPrice: String = "0",
    @SerializedName(value = "conditionMet", alternate = ["condition_met"])
    val conditionMet: Boolean = false,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String = ""
)

data class StockAlertsResponse(
    val alerts: List<StockAlertDto> = emptyList()
)

data class CreateStockAlertRequest(
    val stockId: String,
    val conditionKind: String = "PRICE_ABOVE",
    val thresholdAmount: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

// --- Account Sessions & Security DTOs ---
data class AccountSessionDto(
    @SerializedName(value = "sessionId", alternate = ["session_id", "id"])
    val sessionId: String = "",
    @SerializedName(value = "ipAddress", alternate = ["ip_address", "ip"])
    val ipAddress: String = "127.0.0.1",
    @SerializedName(value = "userAgent", alternate = ["user_agent", "device"])
    val userAgent: String = "모바일 앱",
    @SerializedName(value = "isCurrent", alternate = ["is_current", "current"])
    val isCurrent: Boolean = false,
    @SerializedName(value = "lastActiveAt", alternate = ["last_active_at", "updatedAt"])
    val lastActiveAt: String = "",
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String = ""
)

data class AccountSessionsResponse(
    val sessions: List<AccountSessionDto> = emptyList()
)

data class RevokeSessionResponse(
    val success: Boolean = true,
    @SerializedName(value = "revokedSessionId", alternate = ["revoked_session_id"])
    val revokedSessionId: String? = null,
    val message: String? = null
)

data class RevokeOtherSessionsResponse(
    val success: Boolean = true,
    @SerializedName(value = "revokedCount", alternate = ["revoked_count"])
    val revokedCount: Int = 0,
    val message: String? = null
)

data class SecurityLogDto(
    val id: String = "",
    @SerializedName(value = "eventType", alternate = ["event_type", "type"])
    val eventType: String = "",
    @SerializedName(value = "ipAddress", alternate = ["ip_address", "ip"])
    val ipAddress: String = "",
    @SerializedName(value = "userAgent", alternate = ["user_agent"])
    val userAgent: String = "",
    @SerializedName(value = "createdAt", alternate = ["created_at", "timestamp"])
    val createdAt: String = "",
    val details: String? = null
)

data class SecurityLogsResponse(
    val logs: List<SecurityLogDto> = emptyList()
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class TwoFactorSetupResponse(
    val secret: String = "",
    @SerializedName(value = "qrCodeUrl", alternate = ["qr_code_url", "otpauth_url"])
    val qrCodeUrl: String? = null,
    val manualEntryKey: String? = null
)

data class TwoFactorVerifyRequest(
    val code: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

// --- Admin Control Tower DTOs ---
data class AdminOverviewDto(
    @SerializedName(value = "totalUsers", alternate = ["total_users", "user_count"])
    val totalUsers: Long = 0,
    @SerializedName(value = "activeUsersToday", alternate = ["active_users_today", "active_today"])
    val activeUsersToday: Long = 0,
    @SerializedName(value = "totalWldSupply", alternate = ["total_wld_supply", "money_supply"])
    val totalWldSupply: String = "0",
    @SerializedName(value = "casinoTurnover24h", alternate = ["casino_turnover_24h"])
    val casinoTurnover24h: String = "0",
    @SerializedName(value = "pendingSupports", alternate = ["pending_supports"])
    val pendingSupports: Int = 0
)

data class AdminOverviewResponse(
    val overview: AdminOverviewDto = AdminOverviewDto()
)

data class AdminFeatureSwitchDto(
    @SerializedName(value = "featureKey", alternate = ["feature_key", "key"])
    val featureKey: String = "",
    val name: String = "",
    val enabled: Boolean = true,
    val description: String? = null,
    @SerializedName(value = "updatedAt", alternate = ["updated_at"])
    val updatedAt: String? = null
)

data class AdminFeatureSwitchesResponse(
    val switches: List<AdminFeatureSwitchDto> = emptyList()
)

data class UpdateFeatureSwitchRequest(
    val featureKey: String,
    val enabled: Boolean,
    val reason: String = "관리자 콘솔 토글",
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class FreezeUserRequest(
    val freeze: Boolean,
    val reason: String = "관리자 관제탑 조치",
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class AdminUserDto(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "USER",
    val isFrozen: Boolean = false,
    val createdAt: String = ""
)

data class AdminUsersResponse(
    val users: List<AdminUserDto> = emptyList()
)

// --- Phase 3: Engagement & Quest Goals DTOs ---
data class EngagementGoalDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val target: Int = 1,
    val current: Int = 0,
    val isCompleted: Boolean = false,
    val rewardWld: String = "0",
    val rewardExp: String = "0"
)

data class EngagementOverviewDto(
    @SerializedName(value = "todayGoals", alternate = ["today_goals", "dailyGoals"])
    val todayGoals: List<EngagementGoalDto> = emptyList(),
    @SerializedName(value = "weeklyGoals", alternate = ["weekly_goals"])
    val weeklyGoals: List<EngagementGoalDto> = emptyList(),
    @SerializedName(value = "nextUnlock", alternate = ["next_unlock"])
    val nextUnlock: String? = null,
    @SerializedName(value = "streakDays", alternate = ["streak_days", "consecutiveDays"])
    val streakDays: Int = 0
)

data class EngagementResponse(
    val engagement: EngagementOverviewDto = EngagementOverviewDto()
)

data class NpcOrderRequest(
    val orderCode: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class NpcOrderResponse(
    val success: Boolean = true,
    val message: String? = null,
    val rewardAmount: String? = null
)

// --- Phase 3: Banking & Bonds Redemption DTOs ---
data class BondRedeemResponse(
    val success: Boolean = true,
    @SerializedName(value = "principalAmount", alternate = ["principal_amount"])
    val principalAmount: String = "0",
    @SerializedName(value = "yieldAmount", alternate = ["yield_amount", "interest"])
    val yieldAmount: String = "0",
    @SerializedName(value = "totalPayout", alternate = ["total_payout"])
    val totalPayout: String = "0",
    val message: String? = null
)

data class SmartLoanApplyRequest(
    val amount: Long,
    val purpose: String = "INVESTMENT",
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class SmartLoanRepayRequest(
    val amount: Long,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

// --- Phase 3: Stocks History, Events & Sparklines DTOs ---
data class StockHistoryItemDto(
    val id: String = "",
    val symbol: String = "",
    val stockName: String = "",
    val side: String = "BUY",
    val quantity: Int = 0,
    val price: String = "0",
    val totalAmount: String = "0",
    val createdAt: String = ""
)

data class StockHistoryResponse(
    val trades: List<StockHistoryItemDto> = emptyList()
)

data class MarketEventDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val impactMultiplier: Double = 1.0,
    val affectedSector: String? = null,
    val expiresAt: String = ""
)

data class MarketEventsResponse(
    val events: List<MarketEventDto> = emptyList()
)

data class StockSparklineDto(
    val symbol: String = "",
    val name: String = "",
    val currentPrice: String = "0",
    val changePercent: Double = 0.0,
    val prices: List<Double> = emptyList()
)

data class StockSparklinesResponse(
    val sparklines: List<StockSparklineDto> = emptyList()
)

// --- Phase 3: Casino History & Fairness DTOs ---
data class CasinoHistoryItemDto(
    val id: String = "",
    val gameType: String = "COIN_FLIP",
    val stakeAmount: String = "0",
    val payoutAmount: String = "0",
    val win: Boolean = false,
    val outcomeDetail: String = "",
    val playedAt: String = ""
)

data class CasinoHistoryResponse(
    val history: List<CasinoHistoryItemDto> = emptyList()
)

data class FairnessProofDto(
    val game: String = "",
    val clientSeed: String = "",
    val serverSeedHash: String = "",
    val disclosedProbability: Double = 0.5,
    val verified: Boolean = true,
    val explanation: String = "서버 및 클라이언트 시드를 암호화 해시하여 결과를 사전 검증할 수 있습니다."
)

data class FairnessResponse(
    val fairness: FairnessProofDto = FairnessProofDto()
)

// --- Phase 3: Profile Titles & Credit Grade DTOs ---
data class ProfileTitleDto(
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val isEquipped: Boolean = false,
    val acquiredAt: String = ""
)

data class ProfileTitlesResponse(
    val titles: List<ProfileTitleDto> = emptyList()
)

data class CreditGradeDetailsDto(
    val grade: String = "A",
    val score: Int = 850,
    val maxLoanLimit: String = "50,000,000",
    val interestRateApr: Double = 4.5,
    val perks: List<String> = emptyList()
)

data class ProgressionCreditResponse(
    val credit: CreditGradeDetailsDto = CreditGradeDetailsDto()
)

// --- v8 Specification: Saving Pockets (저축·목표 포켓) DTOs ---
data class SavingPocketDto(
    val id: String = "",
    val name: String = "",
    val targetAmount: String = "0",
    val currentAmount: String = "0",
    val targetDate: String = "",
    val themeColor: String = "MINT", // MINT, SKY, GOLD, EMERALD
    val isArchived: Boolean = false,
    val createdAt: String = ""
) {
    val balance: Long get() = currentAmount.toLongOrNull() ?: 0L
    val targetAmountVal: Long get() = targetAmount.toLongOrNull() ?: 0L
}

data class CreatePocketRequest(
    val name: String,
    val targetAmount: Long,
    val targetDate: String,
    val themeColor: String = "MINT",
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class PocketMovementRequest(
    val direction: String, // "DEPOSIT" or "WITHDRAW"
    val amount: Long,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class UpdatePocketThemeRequest(
    val themeColor: String,
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class ArchivePocketRequest(
    val idempotencyKey: String = java.util.UUID.randomUUID().toString()
)

data class SavingPocketsResponse(
    val pockets: List<SavingPocketDto> = emptyList()
)

// --- v8 Specification: Casino Hilo 20 DTO ---
data class CasinoHiloRequest(
    val choice: String, // "low" (1~10) or "high" (11~20)
    val stake: Long
)

// --- v8 Specification: Notifications Governance DTOs ---
data class NotificationDto(
    val id: String = "",
    val type: String = "TRANSACTIONAL", // "TRANSACTIONAL" or "SECURITY_CRITICAL"
    val title: String = "",
    val content: String = "",
    val isRead: Boolean = false,
    val createdAt: String = ""
) {
    val read: Boolean get() = isRead
    val body: String get() = content
}

data class NotificationPreferencesDto(
    val marketingConsent: Boolean = true,
    val activityAlert: Boolean = true,
    val questAlert: Boolean = true,
    val maintenanceNotice: Boolean = true
) {
    val marketing: Boolean get() = marketingConsent
    val activity: Boolean get() = activityAlert
    val quest: Boolean get() = questAlert
    val maintenance: Boolean get() = maintenanceNotice
}

data class UpdateNotificationPreferencesRequest(
    val marketingConsent: Boolean,
    val activityAlert: Boolean,
    val questAlert: Boolean,
    val maintenanceNotice: Boolean
)

// --- v8 Specification: Safety Center & Urgent Takedown DTOs ---
data class TakedownRequest(
    val targetUrl: String,
    val reason: String,
    val contactEmail: String,
    val passwordHash: String
)

data class TakedownResponse(
    val trackingId: String = "",
    val status: String = "SUBMITTED",
    val message: String = "긴급 삭제 요청이 성공적으로 접수되었습니다."
)

data class TakedownStatusResponse(
    val trackingId: String = "",
    val status: String = "PENDING_REVIEW", // PENDING_REVIEW, ACTIONED_REMOVED, REJECTED
    val reviewedAt: String? = null,
    val adminNote: String? = null
) {
    val createdAt: String get() = reviewedAt ?: ""
    val resolvedAt: String? get() = reviewedAt
    val reviewNotes: String? get() = adminNote
}

data class AccountSafetyDto(
    val ageConfirmed: Boolean = true,
    val isChildRestricted: Boolean = false,
    val restrictions: List<String> = emptyList()
) {
    val isMinor: Boolean get() = isChildRestricted
    val guardianConsent: Boolean get() = ageConfirmed
    val guardianEmail: String? get() = null
}



