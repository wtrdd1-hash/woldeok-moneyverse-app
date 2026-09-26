package com.example.woldeokmoneyverse.data.remote

import com.example.woldeokmoneyverse.data.model.*
import com.google.gson.JsonElement
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface MoneyverseApi {
    @GET
    suspend fun contractGet(@Url url: String): Response<JsonElement>

    @POST
    suspend fun contractPost(@Url url: String, @Body body: JsonElement? = null): Response<JsonElement>

    @PUT
    suspend fun contractPut(@Url url: String, @Body body: JsonElement? = null): Response<JsonElement>

    @HTTP(method = "DELETE", hasBody = true)
    suspend fun contractDelete(@Url url: String, @Body body: JsonElement): Response<JsonElement>

    @HTTP(method = "DELETE", hasBody = false)
    suspend fun contractDeleteNoBody(@Url url: String): Response<JsonElement>

    @POST
    suspend fun contractPostRaw(@Url url: String, @Body body: RequestBody): Response<JsonElement>

    @Streaming
    @GET
    suspend fun contractGetRaw(@Url url: String): Response<ResponseBody>

    @POST
    suspend fun contractPostRawBinary(@Url url: String, @Body body: RequestBody): Response<ResponseBody>

    // Universal transport escape hatch: preserves access to current and future BFF routes
    // without requiring a typed Retrofit method for every endpoint before the UI can use it.
    @GET
    suspend fun universalGet(@Url url: String, @HeaderMap headers: Map<String, String> = emptyMap()): Response<ResponseBody>

    @POST
    suspend fun universalPost(@Url url: String, @Body body: RequestBody, @HeaderMap headers: Map<String, String> = emptyMap()): Response<ResponseBody>

    @PUT
    suspend fun universalPut(@Url url: String, @Body body: RequestBody, @HeaderMap headers: Map<String, String> = emptyMap()): Response<ResponseBody>

    @PATCH
    suspend fun universalPatch(@Url url: String, @Body body: RequestBody, @HeaderMap headers: Map<String, String> = emptyMap()): Response<ResponseBody>

    @HTTP(method = "DELETE", hasBody = true)
    suspend fun universalDelete(@Url url: String, @Body body: RequestBody, @HeaderMap headers: Map<String, String> = emptyMap()): Response<ResponseBody>

    @HTTP(method = "DELETE", hasBody = false)
    suspend fun universalDeleteNoBody(@Url url: String, @HeaderMap headers: Map<String, String> = emptyMap()): Response<ResponseBody>

    @GET("app-api/v1/auth/viewer")
    suspend fun getViewer(): Response<ViewerResponse>

    @POST("app-api/v1/auth/prelogin-session")
    suspend fun preloginSession(): Response<AuthResponse>

    @GET("app-api/v1/auth/policy")
    suspend fun getPolicy(): Response<Map<String, String>>

    @PUT("app-api/v1/auth/consent")
    suspend fun recordConsent(@Body body: ConsentRequest): Response<AuthResponse>

    @POST("app-api/v1/auth/local/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("app-api/v1/auth/local/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("app-api/v1/auth/local/verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<AuthResponse>

    @GET("app-api/v1/auth/session")
    suspend fun getSession(): Response<AuthResponse>

    @POST("app-api/v1/auth/mobile/handoff")
    suspend fun mobileHandoff(@Body body: HandoffRequest): Response<AuthResponse>

    @POST("app-api/v1/auth/logout")
    suspend fun logout(): Response<AuthResponse>

    @DELETE("app-api/v1/account")
    suspend fun deleteAccount(): Response<AuthResponse>

    @GET("app-api/v1/auth/providers")
    suspend fun getAuthProviders(): Response<AuthProvidersResponse>

    @GET("app-api/v1/auth/google/authorize?client=mobile")
    suspend fun getGoogleAuthorizeUrl(): Response<Map<String, String>>

    @GET("app-api/v1/auth/discord/authorize?client=mobile")
    suspend fun getDiscordAuthorizeUrl(): Response<Map<String, String>>

    @GET("app-api/v1/wallet")
    suspend fun getWalletOverview(@Query("recent") recentLimit: Int? = 20): Response<WalletOverviewResponse>

    @POST("app-api/v1/wallet/transfers")
    suspend fun transferMoney(@Body body: TransferRequest): Response<AuthResponse>

    @POST("app-api/v1/bank/movements")
    suspend fun moveBankMoney(@Body body: BankMovementRequest): Response<AuthResponse>

    @GET("app-api/v1/bank/loans")
    suspend fun getLoans(): Response<Map<String, List<LoanDto>>>

    @POST("app-api/v1/bank/loans")
    suspend fun borrowLoan(@Body body: BorrowRequest): Response<AuthResponse>

    @POST("app-api/v1/bank/loans/{id}/repayments")
    suspend fun repayLoan(@Path("id") loanId: String, @Body body: RepayRequest): Response<AuthResponse>

    @POST("app-api/v1/rewards/daily/claims")
    suspend fun claimDailyReward(@Body body: DailyClaimRequest = DailyClaimRequest()): Response<DailyClaimApiResponse>

    @GET("app-api/v1/work")
    suspend fun getWorkStatus(): Response<WorkDashboardResponse>

    @GET("app-api/v1/work/profile")
    suspend fun getWorkProfile(): Response<WorkProfileResponse>

    @GET("app-api/v1/progression")
    suspend fun getProgression(): Response<ProgressionResponse>

    @GET("app-api/v1/early-game/today")
    suspend fun getTodayEarlyGame(): Response<TodayEarlyGameResponse>

    @GET("app-api/v1/stocks")
    suspend fun getStocks(): Response<List<StockDto>>

    @GET("app-api/v1/stocks/portfolio")
    suspend fun getStockPortfolio(): Response<StockPortfolioDto>

    @POST("app-api/v1/stocks/{id}/orders")
    suspend fun orderStock(@Path("id") stockId: String, @Body body: StockOrderRequest): Response<StockOrderResponse>

    @GET("app-api/v1/businesses/my-v2")
    suspend fun getOwnedBusinesses(): Response<BusinessListResponse>

    @GET("app-api/v1/businesses/catalog")
    suspend fun getBusinessCatalog(): Response<BusinessCatalogResponse>

    @GET("app-api/v1/businesses/equity")
    suspend fun getBusinessEquity(): Response<BusinessEquityResponse>

    @POST("app-api/v1/businesses/catalog/{id}/purchases")
    suspend fun purchaseBusiness(@Path("id") typeId: String, @Body body: BusinessPurchaseRequest): Response<AuthResponse>

    @POST("app-api/v1/businesses/{id}/settlements")
    suspend fun settleBusinessProfit(@Path("id") businessId: String): Response<BusinessSettlementResponse>

    @POST("app-api/v1/casino/coin/plays")
    suspend fun playCoinFlip(@Body body: CasinoPlayRequest): Response<CasinoPlayResponse>

    @POST("app-api/v1/casino/dice/plays")
    suspend fun playDice(@Body body: CasinoDiceRequest): Response<CasinoPlayResponse>

    @GET("app-api/v1/casino/coin/terms")
    suspend fun getCasinoTerms(): Response<CasinoTermsDto>

    @GET("app-api/v1/casino/self-limit")
    suspend fun getCasinoLimits(): Response<CasinoSelfLimitDto>

    @GET("app-api/v1/game-clock")
    suspend fun getGameClock(): Response<GameClockDto>

    @GET("app-api/v1/seasons/events")
    suspend fun getSeasons(): Response<List<SeasonDto>>

    @GET("app-api/v1/seasons/events/{id}/leaderboard")
    suspend fun getSeasonLeaderboard(@Path("id") seasonId: String): Response<List<LeaderboardEntryDto>>

    @GET("app-api/v1/shop/items")
    suspend fun getShopItems(): Response<List<ShopItemDto>>

    @GET("app-api/v1/shop/purchases")
    suspend fun getPurchasedItems(): Response<ShopPurchasesResponse>

    @POST("app-api/v1/shop/items/{id}/purchases")
    suspend fun purchaseShopItem(@Path("id") itemId: String, @Body body: ShopPurchaseRequest): Response<AuthResponse>

    @GET("app-api/v1/board/posts")
    suspend fun getBoardPosts(): Response<BoardPostsResponse>

    @GET("app-api/v1/board/posts/{id}")
    suspend fun getBoardPostDetail(@Path("id") postId: String): Response<PostDetailResponse>

    @POST("app-api/v1/board/posts")
    suspend fun createBoardPost(@Body body: CreatePostRequest): Response<PostDetailResponse>

    @PUT("app-api/v1/board/posts/{id}")
    suspend fun updateBoardPost(@Path("id") postId: String, @Body body: UpdatePostRequest): Response<PostDetailResponse>

    @DELETE("app-api/v1/board/posts/{id}")
    suspend fun deleteBoardPost(@Path("id") postId: String): Response<AuthResponse>

    @GET("app-api/v1/board/posts/{id}/comments")
    suspend fun getBoardPostComments(@Path("id") postId: String): Response<BoardCommentsResponse>

    @POST("app-api/v1/board/posts/{id}/comments")
    suspend fun addPostComment(@Path("id") postId: String, @Body body: AddCommentRequest): Response<CommentDto>

    @DELETE("app-api/v1/board/posts/{id}/comments/{commentId}")
    suspend fun deleteBoardPostComment(@Path("id") postId: String, @Path("commentId") commentId: String): Response<AuthResponse>

    // --- Work & Career Endpoints ---
    @GET("app-api/v1/work/tasks")
    suspend fun getWorkTasks(): Response<WorkTasksResponse>

    @POST("app-api/v1/work/tasks/{id}/complete")
    suspend fun completeWorkTask(@Path("id") taskId: String, @Body body: WorkCompleteTaskRequest = WorkCompleteTaskRequest()): Response<WorkCompleteTaskResponse>

    @POST("app-api/v1/work/active-job")
    suspend fun setActiveJob(@Body body: WorkActiveJobRequest): Response<AuthResponse>

    @POST("app-api/v1/photos/uploads")
    suspend fun uploadPhotoBytes(@Body imageBytes: RequestBody): Response<Map<String, String>>

    @POST("app-api/v1/photos")
    suspend fun submitPhoto(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("app-api/v1/photos/mine")
    suspend fun getMyPhotos(): Response<MyPhotosResponse>

    @GET("app-api/v1/profile")
    suspend fun getMyProfile(): Response<ProfileResponse>

    @PUT("app-api/v1/profile")
    suspend fun updateMyProfile(@Body body: UpdateProfileRequest): Response<ProfileResponse>

    @POST("app-api/v1/profile/image")
    suspend fun uploadProfileImage(@Body imageBytes: RequestBody): Response<JsonElement>

    @DELETE("app-api/v1/profile/image")
    suspend fun deleteProfileImage(): Response<Unit>

    @POST("app-api/v1/privacy/requests")
    suspend fun requestPrivacyData(@Query("type") type: String): Response<PrivacyRequestDto>

    @GET("app-api/v1/content/photos")
    suspend fun getGalleryPhotos(): Response<GalleryPhotosResponse>

    @GET("app-api/v1/content/announcements")
    suspend fun getAnnouncements(): Response<List<AnnouncementDto>>

    @POST("app-api/v1/chat/conversations")
    suspend fun openPrivateChat(@Body body: OpenChatRequest): Response<OpenChatResponse>

    @GET("app-api/v1/chat/conversations")
    suspend fun getPrivateChats(@Query("limit") limit: Int = 50): Response<ChatConversationsResponse>

    @GET("app-api/v1/chat/unread-count")
    suspend fun getPrivateChatUnreadCount(): Response<Map<String, Int>>

    @GET("app-api/v1/chat/conversations/{id}/messages")
    suspend fun getPrivateChatMessages(
        @Path("id") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("beforeSequence") beforeSequence: Int? = null
    ): Response<ChatMessagesResponse>

    @POST("app-api/v1/chat/conversations/{id}/messages")
    suspend fun sendPrivateChatMessage(
        @Path("id") conversationId: String,
        @Body body: SendChatMessageRequest
    ): Response<JsonElement>

    @POST("app-api/v1/chat/conversations/{id}/read")
    suspend fun markPrivateChatRead(@Path("id") conversationId: String, @Body body: MarkChatReadRequest): Response<JsonElement>

    @POST("app-api/v1/chat/conversations/{id}/archive")
    suspend fun archivePrivateChat(@Path("id") conversationId: String, @Body body: ChatToggleRequest): Response<JsonElement>

    @POST("app-api/v1/chat/conversations/{id}/mute")
    suspend fun mutePrivateChat(@Path("id") conversationId: String, @Body body: ChatToggleRequest): Response<JsonElement>

    @POST("app-api/v1/chat/users/{id}/block")
    suspend fun blockPrivateChatUser(@Path("id") userId: String): Response<JsonElement>

    @DELETE("app-api/v1/chat/users/{id}/block")
    suspend fun unblockPrivateChatUser(@Path("id") userId: String): Response<JsonElement>

    @POST("app-api/v1/chat/conversations/{id}/report")
    suspend fun reportPrivateChat(@Path("id") conversationId: String, @Body body: ChatReportRequest): Response<ChatReportResponse>

    @GET("app-api/v1/support/threads")
    suspend fun getSupportThreads(): Response<SupportThreadsResponse>

    @POST("app-api/v1/support/threads")
    suspend fun createSupportThread(@Body body: CreateSupportThreadRequest): Response<SupportThreadResponse>

    @GET("app-api/v1/support/threads/{id}/messages")
    suspend fun getSupportMessages(@Path("id") threadId: String): Response<SupportMessagesResponse>

    @POST("app-api/v1/support/threads/{id}/messages")
    suspend fun sendSupportMessage(@Path("id") threadId: String, @Body body: CreateSupportMessageRequest): Response<SupportMessageResponse>

    @GET("app-api/v1/admin/support/threads")
    suspend fun getAdminSupportThreads(@Query("status") status: String? = null): Response<SupportThreadsResponse>

    @GET("app-api/v1/admin/support/threads/{id}/messages")
    suspend fun getAdminSupportMessages(@Path("id") threadId: String): Response<SupportMessagesResponse>

    @POST("app-api/v1/admin/support/threads/{id}/messages")
    suspend fun sendAdminSupportMessage(@Path("id") threadId: String, @Body body: CreateSupportMessageRequest): Response<SupportMessageResponse>

    @PUT("app-api/v1/admin/support/threads/{id}/status")
    suspend fun setAdminSupportStatus(@Path("id") threadId: String, @Body body: SupportStatusRequest): Response<Map<String, String>>

    @GET("app-api/v1/admin/activity/logs")
    suspend fun getAdminActivityLogs(@Query("limit") limit: Int = 50, @Query("offset") offset: Int = 0): Response<List<AdminActivityLogDto>>

    @GET("app-api/v1/content/status")
    suspend fun getServiceStatus(): Response<ServiceStatusResponse>

    // --- Banking & Bonds ---
    @GET("app-api/v1/banking/standing")
    suspend fun getBankingStanding(): Response<BankingStandingDto>

    @POST("app-api/v1/banking/claim-interest")
    suspend fun claimBankInterest(@Body body: IdempotentRequest = IdempotentRequest()): Response<JsonElement>

    @POST("app-api/v1/banking/bonds/purchase")
    suspend fun purchaseBond(@Body body: BondPurchaseRequest): Response<JsonElement>

    @POST("app-api/v1/banking/bonds/{id}/redeem")
    suspend fun redeemBond(@Path("id") bondId: String, @Body body: IdempotentRequest = IdempotentRequest()): Response<BondRedeemResponse>

    // --- Shop Holdings & Inventory ---
    @GET("app-api/v1/shop/holdings")
    suspend fun getShopHoldings(): Response<ShopHoldingsResponse>

    @POST("app-api/v1/shop/holdings/{id}/consumptions")
    suspend fun consumeHoldingItem(@Path("id") holdingId: String, @Body body: IdempotentRequest = IdempotentRequest()): Response<ConsumeHoldingResponse>

    @POST("app-api/v1/shop/holdings/{id}/equip")
    suspend fun equipHoldingItem(@Path("id") holdingId: String): Response<EquipHoldingResponse>

    @POST("app-api/v1/shop/holdings/{id}/upkeep-settlements")
    suspend fun settleHoldingUpkeep(@Path("id") holdingId: String, @Body body: IdempotentRequest = IdempotentRequest()): Response<UpkeepSettlementResponse>

    // --- Stock Candles, Watchlist & Alerts ---
    @GET("app-api/v1/stocks/{id}/candles")
    suspend fun getStockCandles(
        @Path("id") stockId: String,
        @Query("interval") interval: String = "86400",
        @Query("limit") limit: String = "30"
    ): Response<StockCandlesResponse>

    @GET("app-api/v1/stocks/watchlist")
    suspend fun getStockWatchlist(): Response<StockWatchlistResponse>

    @POST("app-api/v1/stocks/{id}/watchlist")
    suspend fun toggleStockWatchlist(@Path("id") stockId: String): Response<JsonElement>

    @GET("app-api/v1/stocks/alerts")
    suspend fun getStockAlerts(): Response<StockAlertsResponse>

    @POST("app-api/v1/stocks/alerts")
    suspend fun createStockAlert(@Body body: CreateStockAlertRequest): Response<StockAlertDto>

    @DELETE("app-api/v1/stocks/alerts/{id}")
    suspend fun deleteStockAlert(@Path("id") alertId: String): Response<JsonElement>

    // --- Account Security & Sessions Endpoints ---
    @GET("app-api/v1/account/sessions")
    suspend fun getAccountSessions(): Response<AccountSessionsResponse>

    @DELETE("app-api/v1/account/sessions/{id}")
    suspend fun revokeAccountSession(@Path("id") sessionId: String): Response<RevokeSessionResponse>

    @POST("app-api/v1/account/sessions/revoke-others")
    suspend fun revokeOtherSessions(): Response<RevokeOtherSessionsResponse>

    @GET("app-api/v1/account/security-logs")
    suspend fun getSecurityLogs(): Response<SecurityLogsResponse>

    @POST("app-api/v1/account/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<AuthResponse>

    @POST("app-api/v1/account/2fa/setup")
    suspend fun setupTwoFactor(): Response<TwoFactorSetupResponse>

    @POST("app-api/v1/account/2fa/verify")
    suspend fun verifyTwoFactor(@Body body: TwoFactorVerifyRequest): Response<AuthResponse>

    // --- Admin Control Tower Endpoints ---
    @GET("app-api/v1/admin/overview")
    suspend fun getAdminOverview(): Response<AdminOverviewResponse>

    @GET("app-api/v1/admin/feature-switches")
    suspend fun getAdminFeatureSwitches(): Response<AdminFeatureSwitchesResponse>

    @POST("app-api/v1/admin/feature-switches")
    suspend fun updateAdminFeatureSwitch(@Body body: UpdateFeatureSwitchRequest): Response<AuthResponse>

    @GET("app-api/v1/admin/users")
    suspend fun getAdminUsers(): Response<AdminUsersResponse>

    @POST("app-api/v1/admin/users/{userId}/freeze")
    suspend fun freezeUser(@Path("userId") userId: String, @Body body: FreezeUserRequest): Response<AuthResponse>

    // --- Phase 3: Engagement & Quests Endpoints ---
    @GET("app-api/v1/engagement")
    suspend fun getEngagement(): Response<EngagementResponse>

    @POST("app-api/v1/engagement/npcs/{code}/orders")
    suspend fun takeNpcOrder(@Path("code") code: String, @Body body: NpcOrderRequest = NpcOrderRequest(code)): Response<NpcOrderResponse>

    // --- Phase 3: Banking & Bonds Endpoints ---
    @POST("app-api/v1/banking/borrow")
    suspend fun applySmartLoan(@Body body: SmartLoanApplyRequest): Response<AuthResponse>

    @POST("app-api/v1/banking/repay")
    suspend fun repaySmartLoan(@Body body: SmartLoanRepayRequest): Response<AuthResponse>

    // --- Phase 3: Stocks History, Events & Sparklines ---
    @GET("app-api/v1/stocks/history")
    suspend fun getStockTradesHistory(): Response<StockHistoryResponse>

    @GET("app-api/v1/stocks/market-events")
    suspend fun getMarketEvents(): Response<MarketEventsResponse>

    @GET("app-api/v1/stocks/sparklines")
    suspend fun getStockSparklines(): Response<StockSparklinesResponse>

    // --- Phase 3: Casino History & Fairness ---
    @GET("app-api/v1/casino/history")
    suspend fun getCasinoHistory(): Response<CasinoHistoryResponse>

    @GET("app-api/v1/casino/coin/fairness")
    suspend fun getCoinFairness(): Response<FairnessResponse>

    @GET("app-api/v1/casino/dice/fairness")
    suspend fun getDiceFairness(): Response<FairnessResponse>

    // --- Phase 3: Profile Titles & Credit Grade ---
    @GET("app-api/v1/profile/titles")
    suspend fun getProfileTitles(): Response<ProfileTitlesResponse>

    @GET("app-api/v1/progression/credit")
    suspend fun getProgressionCredit(): Response<ProgressionCreditResponse>

    // --- Phase 3: Shop Holdings Actions ---
    @POST("app-api/v1/shop/holdings/{id}/consumptions")
    suspend fun consumeShopHolding(@Path("id") holdingId: String, @Body body: IdempotentRequest = IdempotentRequest()): Response<ConsumeHoldingResponse>

    @POST("app-api/v1/shop/holdings/{id}/equip")
    suspend fun equipShopHolding(@Path("id") holdingId: String, @Body body: IdempotentRequest = IdempotentRequest()): Response<EquipHoldingResponse>

    @POST("app-api/v1/shop/holdings/{id}/upkeep-settlements")
    suspend fun settleShopHoldingUpkeep(@Path("id") holdingId: String, @Body body: IdempotentRequest = IdempotentRequest()): Response<UpkeepSettlementResponse>

    // --- v8: Saving Pockets (저축·목표 포켓) ---
    @GET("app-api/v1/bank/pockets")
    suspend fun getSavingPockets(): Response<SavingPocketsResponse>

    @POST("app-api/v1/bank/pockets")
    suspend fun createSavingPocket(@Body body: CreatePocketRequest): Response<SavingPocketDto>

    @POST("app-api/v1/bank/pockets/{id}/movements")
    suspend fun movePocketMoney(@Path("id") pocketId: String, @Body body: PocketMovementRequest): Response<AuthResponse>

    @PUT("app-api/v1/bank/pockets/{id}/theme")
    suspend fun updatePocketTheme(@Path("id") pocketId: String, @Body body: UpdatePocketThemeRequest): Response<SavingPocketDto>

    @POST("app-api/v1/bank/pockets/{id}/archive")
    suspend fun archivePocket(@Path("id") pocketId: String, @Body body: ArchivePocketRequest): Response<AuthResponse>

    // --- v8: Casino Hilo 20 ---
    @POST("app-api/v1/casino/hilo/plays")
    suspend fun playHilo(@Body body: CasinoHiloRequest): Response<CasinoPlayResponse>

    // --- v8: Notifications Governance ---
    @GET("app-api/v1/account/notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @POST("app-api/v1/account/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<AuthResponse>

    @GET("app-api/v1/account/notifications/preferences")
    suspend fun getNotificationPreferences(): Response<NotificationPreferencesDto>

    @PUT("app-api/v1/account/notifications/preferences")
    suspend fun updateNotificationPreferences(@Body body: UpdateNotificationPreferencesRequest): Response<NotificationPreferencesDto>

    // --- v8: Safety Center & Urgent Takedown ---
    @POST("app-api/v1/safety/takedowns")
    suspend fun submitTakedown(@Body body: TakedownRequest): Response<TakedownResponse>

    @GET("app-api/v1/safety/takedowns/{id}/status")
    suspend fun getTakedownStatus(@Path("id") id: String, @Query("password") pwd: String): Response<TakedownStatusResponse>

    @GET("app-api/v1/account/safety")
    suspend fun getAccountSafety(): Response<AccountSafetyDto>
}


