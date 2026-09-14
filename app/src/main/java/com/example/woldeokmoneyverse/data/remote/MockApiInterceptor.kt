package com.example.woldeokmoneyverse.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.random.Random

class MockApiInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Only intercept when force mock mode is explicitly enabled in settings
        if (ApiClient.isMockModeEnabled) {
            return createMockResponse(chain)
        }

        return chain.proceed(request)
    }

    private fun createMockResponse(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        val mockJson = when {
            // Keep mock mode contract-identical to the production mobile BFF.
            // These cases deliberately precede legacy fixtures below, which use
            // retired endpoint names and response envelopes.
            path.endsWith("/auth/viewer") -> """
                { "signedIn": true, "csrfToken": "csrf_mock_token_992384",
                  "user": { "userId": "usr_778e5c4a", "displayName": "wtrdd",
                  "email": "wtrdd@woldeok.com", "level": 12,
                  "title": "월덕 머니버서 패스트트랙", "joinedAt": "2026-01-15" } }
            """.trimIndent()

            path.endsWith("/auth/prelogin-session") -> """
                { "success": true, "csrfToken": "csrf_mock_token_992384" }
            """.trimIndent()

            path.endsWith("/auth/policy") -> """
                { "termsVersion": "2026-09-02", "privacyVersion": "2026-09-02" }
            """.trimIndent()

            path.endsWith("/auth/session") || path.endsWith("/auth/mobile/handoff") -> """
                { "success": true, "outcome": "signed-in", "userId": "usr_778e5c4a",
                  "email": "wtrdd@woldeok.com", "displayName": "wtrdd",
                  "csrfToken": "csrf_mock_token_992384" }
            """.trimIndent()

            path.endsWith("/rewards/daily/claims") -> """
                { "transactionId": "tx_daily_001", "amount": "500000", "replayed": false }
            """.trimIndent()

            path.endsWith("/work") -> """
                { "daily_paid": "1200000", "daily_cap": "5000000", "weekly_paid": "4800000",
                  "weekly_cap": "25000000", "active_assignments": "1" }
            """.trimIndent()

            path.endsWith("/progression") -> """
                { "progression": { "stageCode": "INVESTOR", "reachedAt": "2026-09-12T10:30:00Z",
                  "nextStageCode": "ENTREPRENEUR", "nextRequirements": { "level": 15, "netWorth": 100000000 } } }
            """.trimIndent()

            path.endsWith("/early-game/today") -> """
                { "event": { "event_date": "2026-09-14", "event_code": "DAILY_WELCOME",
                  "event_label": "오늘의 머니버스 미션", "event_detail": "일일 보상을 수령하세요.",
                  "reward_amount": "500000", "reward_experience": "100", "claimed": false, "claim_block": null } }
            """.trimIndent()

            path.endsWith("/casino/coin/plays") || path.endsWith("/casino/dice/plays") -> """
                { "success": true, "isWin": true, "resultOutcome": "heads", "payoutAmount": "2000000",
                  "netProfit": "1000000", "message": "Mock 카지노 게임 결과입니다." }
            """.trimIndent()

            path.endsWith("/casino/self-limit") -> """
                { "dailyBetLimit": 50000000, "dailyLossLimit": 20000000, "lockedUntil": null }
            """.trimIndent()

            path.endsWith("/businesses/my-v2") -> """
                { "businesses": [ { "id": "biz_101", "typeId": "bt_cafe", "name": "월덕 카페 강남 1호점",
                  "level": 2, "isSettlementReady": true, "nextSettlementAt": "2026-09-15T00:00:00Z",
                  "pendingRevenue": "3500000", "licenseActive": true, "dailyRevenue": "3500000",
                  "dailyOperatingCost": "800000" } ] }
            """.trimIndent()

            path.endsWith("/businesses/catalog") -> """
                { "businessTypes": [ { "id": "bt_cafe", "name": "월덕 프리미엄 카페", "category": "식음료",
                  "purchaseCost": "20000000", "dailyRevenue": "3500000", "dailyOperatingCost": "800000",
                  "requiredLevel": 5, "description": "안정적인 일일 유동인구 기반 고수익 카페" } ] }
            """.trimIndent()

            path.endsWith("/businesses/equity") -> """
                { "equity": { "availableEquity": "45000000", "totalBusinessValuation": "120000000",
                  "maxLoanCapacity": "80000000" } }
            """.trimIndent()

            path.endsWith("/profile") -> """
                { "displayName": "wtrdd", "email": "wtrdd@woldeok.com", "joinedAt": "2026-01-15",
                  "jobType": "투자자", "jobLevel": 12, "featuredTitle": "월덕 머니버서 패스트트랙" }
            """.trimIndent()

            path.endsWith("/photos/uploads") -> """
                { "storageKey": "mock/photos/upload_001.jpg" }
            """.trimIndent()

            path.endsWith("/privacy/requests") -> """
                { "type": "EXPORT", "status": "ACCEPTED", "requestedAt": "2026-09-14T00:00:00Z" }
            """.trimIndent()

            path.contains("/stocks/") && path.endsWith("/orders") -> """
                { "success": true, "executedPrice": "285000", "totalCost": "285000",
                  "message": "Mock 주식 주문이 체결되었습니다." }
            """.trimIndent()

            path.contains("/businesses/") && path.endsWith("/settlements") -> """
                { "success": true, "collectedAmount": "3500000", "nextSettlementAt": "2026-09-15T00:00:00Z",
                  "message": "Mock 사업 정산이 완료되었습니다." }
            """.trimIndent()

            path.contains("/shop/items/") && path.endsWith("/purchases") -> """
                { "success": true, "message": "Mock 상점 구매가 완료되었습니다." }
            """.trimIndent()

            path.contains("/board/posts/") && path.endsWith("/comments") -> """
                { "id": "comment_mock_001", "authorName": "wtrdd", "content": "Mock 댓글",
                  "createdAt": "2026-09-14T00:00:00Z" }
            """.trimIndent()

            path.endsWith("/board/posts") && request.method == "POST" -> """
                { "id": "post_mock_001", "authorId": "usr_778e5c4a", "authorName": "wtrdd",
                  "title": "Mock 게시글", "content": "Mock 게시글 내용", "commentCount": 0,
                  "createdAt": "2026-09-14T00:00:00Z", "comments": [] }
            """.trimIndent()

            path.endsWith("/auth/local/login") || path.endsWith("/auth/local/register") -> """
                {
                  "success": true, "outcome": "signed-in",
                  "userId": "usr_778e5c4a",
                  "email": "wtrdd@woldeok.com",
                  "displayName": "wtrdd",
                  "csrfToken": "csrf_mock_token_992384"
                }
            """.trimIndent()

            path.endsWith("/wallet") -> """
                {
                  "userId": "usr_778e5c4a",
                  "balances": {
                    "currency": "WLD",
                    "cash": { "availableAmount": "15420000", "updatedAt": "2026-09-12T12:00:00.000Z" },
                    "bank": { "availableAmount": "85000000", "updatedAt": "2026-09-12T12:00:00.000Z" },
                    "totalAvailableAmount": "100420000"
                  },
                  "recentTransactions": [
                    { "transactionId": "tx_101", "type": "REWARD", "label": "일일출석 보상 수령", "netAmount": "500000", "direction": "in", "occurredAt": "2026-09-12T12:00:00.000Z" },
                    { "transactionId": "tx_102", "type": "WORK", "label": "금융 컨설팅 근무 완료", "netAmount": "1200000", "direction": "in", "occurredAt": "2026-09-12T10:30:00.000Z" }
                  ]
                }
            """.trimIndent()

            path.contains("/bank/loans") -> """
                {
                  "loans": [
                    { "id": "loan_01", "principal": "10000000", "remainingBalance": "3500000", "interestRate": 0.045, "dueDate": "2026-12-31", "status": "ACTIVE" }
                  ]
                }
            """.trimIndent()

            path.endsWith("/rewards/daily/claims") -> """
                {
                  "success": true,
                  "claimedAmount": "500000",
                  "nextEligibleAt": "2026-09-13 00:00:00",
                  "message": "일일출석 보상 500,000 WLD 지급 완료!"
                }
            """.trimIndent()

            path.endsWith("/work") -> """
                {
                  "jobTitle": "수석 자산 관리사",
                  "canWork": true,
                  "cooldownSeconds": 0,
                  "estimatedReward": "1,200,000 WLD",
                  "lastWorkedAt": "2026-09-12 10:30:00"
                }
            """.trimIndent()

            path.endsWith("/progression") -> """
                {
                  "level": 12,
                  "currentExp": 3450,
                  "requiredExp": 5000,
                  "title": "월덕 머니버서 패스트트랙",
                  "unlockedFeatures": ["대출 한도 확대", "사업장 3호점 라이선스", "주식 레버리지 거래"]
                }
            """.trimIndent()

            path.endsWith("/early-game/tasks") -> """
                [
                  { "id": "eg_1", "title": "첫 출석 보상 받기", "description": "플레이 탭에서 일일출석 보상을 받으세요.", "isCompleted": true, "rewardAmount": "500000" },
                  { "id": "eg_2", "title": "첫 근무 완료하기", "description": "근무 메뉴에서 일을 완료하고 경험치를 쌓으세요.", "isCompleted": true, "rewardAmount": "1200000" },
                  { "id": "eg_3", "title": "첫 사업장 매수하기", "description": "사업 카탈로그에서 소형 매장을 구매해보세요.", "isCompleted": false, "rewardAmount": "5000000" }
                ]
            """.trimIndent()

            path.endsWith("/stocks/portfolio") -> """
                {
                  "totalStockValue": "42500000",
                  "holdings": [
                    { "stockId": "stk_tech", "symbol": "WLD-TECH", "name": "월덕 테크놀로지", "quantity": 100, "averageBuyPrice": "250000", "currentPrice": "285000", "totalValue": "28500000", "profitLossPercent": 14.0 },
                    { "stockId": "stk_bio", "symbol": "WLD-BIO", "name": "월덕 바이오제약", "quantity": 50, "averageBuyPrice": "300000", "currentPrice": "280000", "totalValue": "14000000", "profitLossPercent": -6.67 }
                  ]
                }
            """.trimIndent()

            path.endsWith("/stocks") -> """
                [
                  { "id": "stk_tech", "symbol": "WLD-TECH", "name": "월덕 테크놀로지", "currentPrice": "285000", "priceChangePercent": 3.42, "isMarketOpen": true, "historyPrices": [250000, 260000, 255000, 270000, 285000] },
                  { "id": "stk_bio", "symbol": "WLD-BIO", "name": "월덕 바이오제약", "currentPrice": "280000", "priceChangePercent": -1.20, "isMarketOpen": true, "historyPrices": [300000, 295000, 290000, 285000, 280000] },
                  { "id": "stk_energy", "symbol": "WLD-NRG", "name": "월덕 에너지 솔루션", "currentPrice": "152000", "priceChangePercent": 5.80, "isMarketOpen": true, "historyPrices": [140000, 145000, 150000, 148000, 152000] },
                  { "id": "stk_food", "symbol": "WLD-FD", "name": "월덕 푸드텍", "currentPrice": "89000", "priceChangePercent": 0.45, "isMarketOpen": true, "historyPrices": [88000, 88500, 89000, 88800, 89000] }
                ]
            """.trimIndent()

            path.endsWith("/casino/coinflip") -> {
                val win = Random.nextBoolean()
                val payout = if (win) "2000000" else "0"
                val profit = if (win) "+1000000" else "-1000000"
                """
                    {
                      "success": true,
                      "isWin": $win,
                      "resultOutcome": "${if (win) "heads" else "tails"}",
                      "payoutAmount": "$payout",
                      "netProfit": "$profit",
                      "message": "${if (win) "🎉 동전 던지기 승리! 2,000,000 WLD 획득!" else "💸 동전 던지기 패배! 다음 기회에..."}"
                    }
                """.trimIndent()
            }

            path.endsWith("/casino/dice") -> {
                val dice = (1..6).random()
                val win = Random.nextBoolean()
                val payout = if (win) "3000000" else "0"
                val profit = if (win) "+2000000" else "-1000000"
                """
                    {
                      "success": true,
                      "isWin": $win,
                      "resultOutcome": "$dice 번 주사위",
                      "payoutAmount": "$payout",
                      "netProfit": "$profit",
                      "message": "${if (win) "🎲 주사위 적중! [$dice 번] 당첨 3,000,000 WLD 획득!" else "🎲 주사위 결과 [$dice 번] - 미적중"}"
                    }
                """.trimIndent()
            }

            path.endsWith("/casino/limits") -> """
                {
                  "dailyBetLimit": 50000000,
                  "dailyLossLimit": 20000000,
                  "lockedUntil": null
                }
            """.trimIndent()

            path.endsWith("/seasons/events") -> """
                [
                  { "id": "sea_2026_q3", "name": "2026 Q3 대부호 리그", "description": "가장 많은 자산을 증식한 상위 100명에게 스페셜 칭호 및 VIP 훈장 지급", "endsAt": "2026-09-30", "currentProgress": 78, "totalMilestone": 100 }
                ]
            """.trimIndent()

            path.contains("/seasons/events/") && path.endsWith("/leaderboard") -> """
                [
                  { "rank": 1, "userId": "usr_top1", "displayName": "월덕갓", "score": "580,420,000 WLD", "title": "전설의 머니버서" },
                  { "rank": 2, "userId": "usr_778e5c4a", "displayName": "wtrdd", "score": "100,420,000 WLD", "title": "패스트트랙" },
                  { "rank": 3, "userId": "usr_top3", "displayName": "김덕철", "score": "89,150,000 WLD", "title": "신흥 재벌" }
                ]
            """.trimIndent()

            path.endsWith("/businesses/catalog") -> """
                [
                  { "id": "bt_cafe", "name": "월덕 프리미엄 카페", "category": "식음료", "purchaseCost": "20000000", "dailyRevenue": "3500000", "dailyOperatingCost": "800000", "requiredLevel": 5, "description": "안정적인 일일 유동인구 기반 고수익 카페" },
                  { "id": "bt_tech", "name": "월덕 AI 스타트업", "category": "IT 기술", "purchaseCost": "50000000", "dailyRevenue": "12000000", "dailyOperatingCost": "3500000", "requiredLevel": 10, "description": "높은 성장 잠재력을 지닌 기술 기반 인큐베이터" },
                  { "id": "bt_realestate", "name": "월덕 빌딩 임대업", "category": "부동산", "purchaseCost": "150000000", "dailyRevenue": "28000000", "dailyOperatingCost": "6000000", "requiredLevel": 15, "description": "매주 고정적인 임대료 수입 발생" }
                ]
            """.trimIndent()

            path.endsWith("/businesses/equity") -> """
                {
                  "availableEquity": "45000000",
                  "totalBusinessValuation": "120000000",
                  "maxLoanCapacity": "80000000"
                }
            """.trimIndent()

            path.endsWith("/businesses") -> """
                [
                  { "id": "biz_101", "typeId": "bt_cafe", "name": "월덕 카페 강남 1호점", "level": 2, "isSettlementReady": true, "nextSettlementAt": "2026-09-12 23:59:59", "pendingRevenue": "3,500,000 WLD", "licenseActive": true },
                  { "id": "biz_102", "typeId": "bt_tech", "name": "월덕 소프트웨어 연구소", "level": 1, "isSettlementReady": false, "nextSettlementAt": "2026-09-13 18:00:00", "pendingRevenue": "0 WLD", "licenseActive": true }
                ]
            """.trimIndent()

            path.endsWith("/shop/items") -> """
                [
                  { "id": "shp_vip", "name": "VIP 월렛 배지", "category": "프로필", "price": "10000000", "description": "프로필 및 커뮤니티에 황금 VIP 배지 표시", "isOwned": true },
                  { "id": "shp_boost", "name": "사업 정산 2배 부스터 (24h)", "category": "소모품", "price": "5000000", "description": "24시간 동안 모든 사업장 정산액 2배 적용", "isOwned": false },
                  { "id": "shp_title", "name": "칭호: 머니버스 억만장자", "category": "칭호", "price": "50000000", "description": "전용 황금 글씨체 칭호 해금", "isOwned": false }
                ]
            """.trimIndent()

            path.endsWith("/shop/purchases") -> """
                { "purchases": [
                  { "purchaseId": "pur_001", "itemId": "shp_vip", "itemName": "VIP 월렛 배지", "transactionId": "tx_001", "amount": "10000000", "purchasedAt": "2026-09-12T12:00:00Z" }
                ] }
            """.trimIndent()

            path.endsWith("/auth/providers") -> """
                { "providers": [
                  { "id": "google", "enabled": true },
                  { "id": "discord", "enabled": true }
                ] }
            """.trimIndent()

            path.endsWith("/photos/mine") -> """
                { "submissions": [
                    { "photo_id": "my_p_1", "image_url": null, "alt_text": "내 월덕 머니버스 기록", "published": true, "submitted_at": "2026-09-12T12:00:00Z", "published_at": "2026-09-12T12:00:00Z" }
                  ]
                }
            """.trimIndent()

            path.endsWith("/content/photos") -> """
                [
                  { "id": "p_1", "title": "월덕 머니버스 강남 타워 전경", "imageUrl": "https://raw.githubusercontent.com/wtrdd1-hash/Woldeok-Moneyverse-Migration/main/docs/images/tower.png", "category": "부동산", "likes": 124 },
                  { "id": "p_2", "title": "2026 Q3 대부호 시상식 현장", "imageUrl": "https://raw.githubusercontent.com/wtrdd1-hash/Woldeok-Moneyverse-Migration/main/docs/images/event.png", "category": "이벤트", "likes": 89 }
                ]
            """.trimIndent()

            path.endsWith("/content/announcements") -> """
                [
                  { "id": "ann_1", "title": "[안내] Woldeok Moneyverse 모바일 v2026.09.12 정식 서비스 개시", "content": "안녕하세요! 월덕 머니버스 모바일 네이티브 앱이 정식 출시되었습니다. 지갑, 주식, 사업, 커뮤니티 기능을 경험해보세요.", "isImportant": true, "createdAt": "2026-09-12 00:00:00" },
                  { "id": "ann_2", "title": "[이벤트] 신규 가입자 일일 출석 보상 2배 지급 안내", "content": "금주일 동안 모든 회원의 일일 출석 보상 및 근무 보상이 2배로 증가합니다.", "isImportant": false, "createdAt": "2026-09-11 12:00:00" }
                ]
            """.trimIndent()

            path.endsWith("/content/status") -> """
                {
                  "status": "OPERATIONAL",
                  "notice": "모든 시스템 및 BFF 서버가 정상 작동 중입니다.",
                  "updatedAt": "2026-09-12 23:00:00"
                }
            """.trimIndent()

            path.endsWith("/profile/me") -> """
                {
                  "userId": "usr_778e5c4a",
                  "displayName": "wtrdd",
                  "email": "wtrdd@woldeok.com",
                  "level": 12,
                  "title": "월덕 머니버서 패스트트랙",
                  "joinedAt": "2026-01-15"
                }
            """.trimIndent()

            path.endsWith("/board/posts") -> """
                [
                  { "id": "post_1", "authorId": "usr_778e5c4a", "authorName": "wtrdd", "title": "주식 WLD-TECH 14% 수익 달성 후기!", "content": "테크주 매수 후 꾸준히 홀딩해서 좋은 수익 거뒀네요 ㅎㅎ 다들 성투하세요!", "commentCount": 2, "createdAt": "2026-09-12 21:00:00", "comments": [ { "id": "c1", "authorName": "김덕철", "content": "축하드립니다! 대단하시네요.", "createdAt": "21:05" }, { "id": "c2", "authorName": "이머니", "content": "저도 매수해야겠습니다.", "createdAt": "21:10" } ] },
                  { "id": "post_2", "authorId": "usr_8819aa01", "authorName": "김덕철", "title": "카페 1호점 정산 완료했습니다.", "content": "사업장 레벨업하니까 정산금액이 쏠쏠하군요.", "commentCount": 1, "createdAt": "2026-09-12 19:30:00", "comments": [ { "id": "c3", "authorName": "wtrdd", "content": "2호점도 오픈하세요!", "createdAt": "19:35" } ] }
                ]
            """.trimIndent()

            else -> """
                {
                  "success": true,
                  "message": "Moneyverse Real API Operation Successful"
                }
            """.trimIndent()
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK (Mock Fallback)")
            .body(mockJson.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
