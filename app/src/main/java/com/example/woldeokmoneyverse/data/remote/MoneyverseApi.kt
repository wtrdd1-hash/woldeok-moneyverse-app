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

    @POST("app-api/v1/board/posts")
    suspend fun createBoardPost(@Body body: CreatePostRequest): Response<BoardPostDto>

    @POST("app-api/v1/board/posts/{id}/comments")
    suspend fun addPostComment(@Path("id") postId: String, @Body body: AddCommentRequest): Response<CommentDto>

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
}
