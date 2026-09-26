# 운영진 관제 타워 & 경제 국고·보안 통제 API (Admin Control Tower)

> 실시간 금융 관제 타워, 경제 시나리오 랩, 통화량 조절, 감사 로그 검색, 사용자 제재 및 권한 관리, 킬스위치/피처 플래그 토글 그리드, 2FA 스텝업 인증, 국고 자산 배분 및 콘텐츠 테이크다운 큐 엔드포인트

## 📋 목차 (Table of Contents)

- [POST `/api/v1/admin/ai-news/auto-generate`](#post--api-v1-admin-ai-news-auto-generate) - Auto-generate and optionally publish market news based on currently registered active stocks | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/ai-news/batches`](#post--api-v1-admin-ai-news-batches) - Start a run that asks the model for five scenarios | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/ai-news/batches/latest`](#get--api-v1-admin-ai-news-batches-latest) - The current batch of proposed scenarios, and the last run that asked for one | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/ai-news/models`](#get--api-v1-admin-ai-news-models) - What the stored key can reach, as GET {base}/models lists it | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/ai-news/scenarios/{id}/discard`](#post--api-v1-admin-ai-news-scenarios--id--discard) - Set a scenario aside | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/ai-news/scenarios/{id}/publish`](#post--api-v1-admin-ai-news-scenarios--id--publish) - Publish a scenario as a market event, with the values the operator settled on | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/ai-news/settings`](#get--api-v1-admin-ai-news-settings) - API address, model name and whether a key is stored | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [PUT `/api/v1/admin/ai-news/settings`](#put--api-v1-admin-ai-news-settings) - Store the model address, model name and, optionally, a new key | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/approvals`](#get--api-v1-admin-approvals) - Withdrawn: two-person approval was retired | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/approvals`](#post--api-v1-admin-approvals) - Withdrawn: two-person approval was retired | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/approvals/{id}/decisions`](#post--api-v1-admin-approvals--id--decisions) - Withdrawn: two-person approval was retired | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/audit-events`](#get--api-v1-admin-audit-events) - Recent audit trail entries | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/audit/dispositions`](#get--api-v1-admin-audit-dispositions) - What was archived, destroyed or held | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/audit/dispositions`](#post--api-v1-admin-audit-dispositions) - Record what was decided about a range past its retention period | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/audit/events`](#get--api-v1-admin-audit-events) - Search the audit trail; addresses and session hashes are masked | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/audit/events/{id}/reveal`](#post--api-v1-admin-audit-events--id--reveal) - Unmask one entry; the reveal is itself recorded | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/audit/retention`](#get--api-v1-admin-audit-retention) - Retention periods, what is past them, and the last disposition | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [PUT `/api/v1/admin/audit/retention/{category}`](#put--api-v1-admin-audit-retention--category-) - Append a retention policy version for one category | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/audit/verifications`](#get--api-v1-admin-audit-verifications) - Past chain verifications | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/audit/verify`](#post--api-v1-admin-audit-verify) - Recompute the hash chain over a window and record the result | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/bank`](#get--api-v1-admin-bank) - Deposits and the loan book, with the credit ladder behind it | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/business-types`](#get--api-v1-admin-business-types) - Every business type, including inactive ones | `🔓 공개 (게스트 허용)`
- [PATCH `/api/v1/admin/business-types/{id}`](#patch--api-v1-admin-business-types--id-) - Rename, redescribe or deactivate a business type | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/controls`](#get--api-v1-admin-controls) - Feature switches, economy policy versions and role assignments | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/controls/auto-policy`](#get--api-v1-admin-controls-auto-policy) - Policy knobs plus classical proposal and matching AI review evidence | `🔓 공개 (게스트 허용)`
- [PUT `/api/v1/admin/controls/auto-policy/knobs/{knobKey}`](#put--api-v1-admin-controls-auto-policy-knobs--knobkey-) - Take one knob off automatic, or move its approved range | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/controls/auto-policy/runs`](#post--api-v1-admin-controls-auto-policy-runs) - Run the dual-lane automatic adjustment now instead of waiting for Monday | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [POST `/api/v1/admin/controls/consent-versions`](#post--api-v1-admin-controls-consent-versions) - Publish a new terms and privacy policy version (Superadmin only) | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/controls/consent-versions`](#get--api-v1-admin-controls-consent-versions) - List recent terms and privacy policy versions | `🔓 공개 (게스트 허용)`
- [PUT `/api/v1/admin/controls/feature-switches/{featureKey}`](#put--api-v1-admin-controls-feature-switches--featurekey-) - Enable, pause, put into safe mode or disable a feature | `🔓 공개 (게스트 허용)`
- [PUT `/api/v1/admin/controls/feature-switches/economy_auto_policy`](#put--api-v1-admin-controls-feature-switches-economy-auto-policy) - Enable or pause the automatic economy policy without step-up | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [POST `/api/v1/admin/controls/policies`](#post--api-v1-admin-controls-policies) - Create an economy policy version, immediate or scheduled | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/controls/policies/activations`](#post--api-v1-admin-controls-policies-activations) - Activate every policy version whose effective time has passed | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/controls/policies/rollbacks`](#post--api-v1-admin-controls-policies-rollbacks) - Return the economy to the previous policy version | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/controls/role-revocations`](#post--api-v1-admin-controls-role-revocations) - Take back an administrative role | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/controls/roles`](#post--api-v1-admin-controls-roles) - Grant an administrative role, or move the superadmin designation | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/discord`](#get--api-v1-admin-discord) - Discord delivery: which types are routed, and what is stuck | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/discord-outbox-events`](#get--api-v1-admin-discord-outbox-events) - Recent Discord outbox deliveries | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/economy`](#get--api-v1-admin-economy) - Money supply, issuance and burn, concentration and operational health | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/economy/ai-status`](#get--api-v1-admin-economy-ai-status) - Economy AI feature switch, latest council review and agent scoreboard | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/economy/alerts`](#get--api-v1-admin-economy-alerts) - Alerts, unacknowledged first | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/economy/alerts/{id}/acknowledgements`](#post--api-v1-admin-economy-alerts--id--acknowledgements) - Acknowledge an alert | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [POST `/api/v1/admin/economy/bulk-payouts`](#post--api-v1-admin-economy-bulk-payouts) - Pay every member the filter matches | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/economy/bulk-payouts/{id}/report`](#get--api-v1-admin-economy-bulk-payouts--id--report) - Who was paid, who was skipped and who failed, one row each | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/economy/bulk-payouts/previews`](#post--api-v1-admin-economy-bulk-payouts-previews) - Count the members a payout would reach, and what it would cost | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/economy/killswitch`](#post--api-v1-admin-economy-killswitch) - Toggle master killswitch or module circuit breaker | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/economy/knobs-v2`](#post--api-v1-admin-economy-knobs-v2) - Update economic knobs (interest, bond yields, loan rates) | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/economy/macro-v2`](#get--api-v1-admin-economy-macro-v2) - Admin Control Center 2.0 Macro Economy statistics | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/economy/reconciliations/latest`](#get--api-v1-admin-economy-reconciliations-latest) - Most recent economy reconciliation health snapshot | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/economy/scenario-lab/preview`](#get--api-v1-admin-economy-scenario-lab-preview) - Read-only deterministic economy scenario projection | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/economy/stats`](#get--api-v1-admin-economy-stats) - Realtime Faucet vs Sink stats and circulation summary | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/economy/transactions/{id}/reversal`](#post--api-v1-admin-economy-transactions--id--reversal) - Reverse one transaction, posting its opposite back to the ledger | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/economy/users/{id}/inspect-v2`](#get--api-v1-admin-economy-users--id--inspect-v2) - Inspect user wallet, deposits, loans, jobs, businesses | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/economy/users/{id}/override-v2`](#post--api-v1-admin-economy-users--id--override-v2) - Override user cash or bank balance (grant or confiscate WLD) | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/me`](#get--api-v1-admin-me) - Roles held by the caller | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/photos`](#post--api-v1-admin-photos) - Upload image bytes to the private store | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/safety/chat-reports`](#get--api-v1-admin-safety-chat-reports) - 관리자 1:1 개인 채팅 신고 큐 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/safety/chat-reports/{id}`](#get--api-v1-admin-safety-chat-reports--id-) - 관리자 1:1 개인 채팅 신고 상세 및 증거 스냅샷 열람 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/safety/chat-reports/{id}/action`](#post--api-v1-admin-safety-chat-reports--id--action) - 관리자 1:1 개인 채팅 신고 조치 실행 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/safety/takedowns`](#get--api-v1-admin-safety-takedowns) - 관리자 긴급 콘텐츠 삭제 큐 조회 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/safety/takedowns/{caseId}/action`](#post--api-v1-admin-safety-takedowns--caseid--action) - 관리자 긴급 콘텐츠 삭제 조치 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/season-events`](#get--api-v1-admin-season-events) - Every season event, including inactive ones | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/season-events`](#post--api-v1-admin-season-events) - Create a season event in the active season | `🔓 공개 (게스트 허용)`
- [PATCH `/api/v1/admin/season-events/{id}`](#patch--api-v1-admin-season-events--id-) - Retitle, redescribe or deactivate a season event | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/security`](#get--api-v1-admin-security) - Console session state and login policy | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/security/forced-logouts`](#post--api-v1-admin-security-forced-logouts) - End every live session a member holds | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/security/ip-blocks`](#get--api-v1-admin-security-ip-blocks) - List current and historical service IP blocks | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/security/ip-blocks`](#post--api-v1-admin-security-ip-blocks) - Block an IP address or CIDR until manually lifted | `🔓 공개 (게스트 허용)`
- [DELETE `/api/v1/admin/security/ip-blocks/{id}`](#delete--api-v1-admin-security-ip-blocks--id-) - Lift a service IP block | `🔓 공개 (게스트 허용)`
- [PUT `/api/v1/admin/security/login-policies/{userId}`](#put--api-v1-admin-security-login-policies--userid-) - Replace the address allowlist for an administrator | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/security/sessions`](#post--api-v1-admin-security-sessions) - Enter the operations console, rotating the session | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [DELETE `/api/v1/admin/security/sessions`](#delete--api-v1-admin-security-sessions) - Leave the operations console | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [POST `/api/v1/admin/security/users/{id}/permanent-suspension`](#post--api-v1-admin-security-users--id--permanent-suspension) - Permanently restrict a member and revoke all live sessions | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/shop/items`](#get--api-v1-admin-shop-items) - List all items in the catalog for admin inspection | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [PATCH `/api/v1/admin/shop/items/{id}`](#patch--api-v1-admin-shop-items--id-) - Update price, active status, or stock of a catalog item | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/stocks`](#get--api-v1-admin-stocks) - Every stock, including inactive ones | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/stocks`](#post--api-v1-admin-stocks) - List a new stock | `🔓 공개 (게스트 허용)`
- [PATCH `/api/v1/admin/stocks/{id}`](#patch--api-v1-admin-stocks--id-) - Rename, redescribe or deactivate a stock | `🔓 공개 (게스트 허용)`
- [DELETE `/api/v1/admin/stocks/{id}`](#delete--api-v1-admin-stocks--id-) - Delete a stock that has no history | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/stocks/{id}/corporate-actions`](#post--api-v1-admin-stocks--id--corporate-actions) - Apply a split or reverse split | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/stocks/{id}/halt`](#post--api-v1-admin-stocks--id--halt) - Halt stock trading and auto-settle all holdings into cost-basis WLD | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/stocks/{id}/halt-settlement`](#get--api-v1-admin-stocks--id--halt-settlement) - Get stock halt settlement progress and statistics | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/stocks/{id}/halt-settlement/retry`](#post--api-v1-admin-stocks--id--halt-settlement-retry) - Retry failed or quarantined stock halt settlements | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/stocks/{id}/price`](#post--api-v1-admin-stocks--id--price) - Set a stock price by hand | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/stocks/dynamics`](#get--api-v1-admin-stocks-dynamics) - Trend, volatility and fair value per stock | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/stocks/market-events`](#get--api-v1-admin-stocks-market-events) - Recent market events, ended and cancelled included | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/stocks/market-events`](#post--api-v1-admin-stocks-market-events) - Publish a market event: news that leans the market | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [DELETE `/api/v1/admin/stocks/market-events/{id}`](#delete--api-v1-admin-stocks-market-events--id-) - End a market event now | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/support/threads`](#get--api-v1-admin-support-threads) - Administrator support inbox | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/support/threads/{id}/messages`](#get--api-v1-admin-support-threads--id--messages) - Read a support conversation as administrator | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/support/threads/{id}/messages`](#post--api-v1-admin-support-threads--id--messages) - Reply to a member support conversation | `🔓 공개 (게스트 허용)`
- [PUT `/api/v1/admin/support/threads/{id}/status`](#put--api-v1-admin-support-threads--id--status) - Change support conversation status | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/treasury/drain`](#post--api-v1-admin-treasury-drain) - 국고 잉여 자금 영구 소각 (Step-Up/Admin) | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/treasury/inject`](#post--api-v1-admin-treasury-inject) - 국고 자금 긴급 주입 (Step-Up/Admin) | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/treasury/overview`](#get--api-v1-admin-treasury-overview) - 중앙 국고 및 비축금 현황 대시보드 조회 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/treasury/transactions`](#get--api-v1-admin-treasury-transactions) - 국고 원장 입출금 및 순환 감사 내역 조회 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/users`](#get--api-v1-admin-users) - Members and their status | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/users/{id}/portfolio`](#get--api-v1-admin-users--id--portfolio) - Detailed user asset portfolio | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [PUT `/api/v1/admin/users/{id}/restriction`](#put--api-v1-admin-users--id--restriction) - Restrict or unrestrict a member | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/work`](#get--api-v1-admin-work) - The work catalogue, the reward policy in force, and job levels | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/work/auto-tune`](#post--api-v1-admin-work-auto-tune) - Automatically calculate and tune daily reward cap based on economy health | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [PUT `/api/v1/admin/work/policy`](#put--api-v1-admin-work-policy) - Update work reward policy daily cap, weekly cap, and repeat decay | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/work/stats`](#get--api-v1-admin-work-stats) - Real-time 24h work ranking, daily cap usage buckets, and 7-day trend | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [PATCH `/api/v1/admin/work/tasks/{id}`](#patch--api-v1-admin-work-tasks--id-) - Update base reward, duration, daily limit, and active state of a work task | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/privacy/requests`](#get--api-v1-privacy-requests) - Data subject requests the caller has made | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/privacy/requests`](#post--api-v1-privacy-requests) - Raise a data subject request | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/safety/takedown`](#post--api-v1-safety-takedown) - 비회원 공개 긴급 콘텐츠 삭제 접수 | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/safety/takedown/status`](#post--api-v1-safety-takedown-status) - 비회원 접수 상태 조회 | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/version`](#get--api-v1-version) - Backend runtime identity | `🔓 공개 (게스트 허용)`
- [GET `/health`](#get--health) - Liveness probe | `🔓 공개 (게스트 허용)`

---

## 🛠️ 엔드포인트 상세 규격

<a id="post--api-v1-admin-ai-news-auto-generate"></a>
### POST `/api/v1/admin/ai-news/auto-generate`

**설명:** Auto-generate and optionally publish market news based on currently registered active stocks

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AiNewsController_autoGenerate`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/ai-news/auto-generate" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-ai-news-batches"></a>
### POST `/api/v1/admin/ai-news/batches`

**설명:** Start a run that asks the model for five scenarios

> Answers with the run, not with the batch: the model takes longer than any gateway in front of this will wait.

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AiNewsController_generate`
#### 📦 요청 본문 (Request Body)

  - `prompt` (`string`) *(선택)* - The operator's wish for this batch
  - `idempotencyKey` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/ai-news/batches" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-ai-news-batches-latest"></a>
### GET `/api/v1/admin/ai-news/batches/latest`

**설명:** The current batch of proposed scenarios, and the last run that asked for one

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AiNewsController_latest`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/ai-news/batches/latest" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-ai-news-models"></a>
### GET `/api/v1/admin/ai-news/models`

**설명:** What the stored key can reach, as GET {base}/models lists it

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AiNewsController_models`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/ai-news/models" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-ai-news-scenarios--id--discard"></a>
### POST `/api/v1/admin/ai-news/scenarios/{id}/discard`

**설명:** Set a scenario aside

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AiNewsController_discard`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/ai-news/scenarios/{id}/discard" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-ai-news-scenarios--id--publish"></a>
### POST `/api/v1/admin/ai-news/scenarios/{id}/publish`

**설명:** Publish a scenario as a market event, with the values the operator settled on

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AiNewsController_publish`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `effects` (`array`) **(필수)**
  - `hours` (`number`) **(필수)**
  - `headline` (`string`) **(필수)**
  - `body` (`string`) *(선택)*
  - `idempotencyKey` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/ai-news/scenarios/{id}/publish" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-ai-news-settings"></a>
### GET `/api/v1/admin/ai-news/settings`

**설명:** API address, model name and whether a key is stored

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AiNewsController_settings`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/ai-news/settings" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-ai-news-settings"></a>
### PUT `/api/v1/admin/ai-news/settings`

**설명:** Store the model address, model name and, optionally, a new key

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AiNewsController_saveSettings`
#### 📦 요청 본문 (Request Body)

  - `apiBaseUrl` (`string`) **(필수)** - The OpenAI-standard base: /chat/completions and /models hang off it
  - `model` (`string`) **(필수)**
  - `apiKey` (`string`) *(선택)* - Absent or empty keeps the stored key
  - `idempotencyKey` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/ai-news/settings" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-approvals"></a>
### GET `/api/v1/admin/approvals`

**설명:** Withdrawn: two-person approval was retired

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminController_approvals`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/approvals" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-approvals"></a>
### POST `/api/v1/admin/approvals`

**설명:** Withdrawn: two-person approval was retired

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminController_approve`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/approvals" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-approvals--id--decisions"></a>
### POST `/api/v1/admin/approvals/{id}/decisions`

**설명:** Withdrawn: two-person approval was retired

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminController_decide`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/approvals/{id}/decisions" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-audit-events"></a>
### GET `/api/v1/admin/audit-events`

**설명:** Recent audit trail entries

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminController_auditEvents`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/audit-events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-audit-dispositions"></a>
### GET `/api/v1/admin/audit/dispositions`

**설명:** What was archived, destroyed or held

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminAuditController_dispositions`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/audit/dispositions" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-audit-dispositions"></a>
### POST `/api/v1/admin/audit/dispositions`

**설명:** Record what was decided about a range past its retention period

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminAuditController_recordDisposition`
#### 📦 요청 본문 (Request Body)

  - `category` (`string`) **(필수)**
  - `fromSequence` (`string`) **(필수)**
  - `toSequence` (`string`) **(필수)**
  - `method` (`string`) **(필수)**
  - `note` (`string`) *(선택)*
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/audit/dispositions" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-audit-events"></a>
### GET `/api/v1/admin/audit/events`

**설명:** Search the audit trail; addresses and session hashes are masked

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminAuditController_events`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/audit/events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-audit-events--id--reveal"></a>
### POST `/api/v1/admin/audit/events/{id}/reveal`

**설명:** Unmask one entry; the reveal is itself recorded

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminAuditController_reveal`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/audit/events/{id}/reveal" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-audit-retention"></a>
### GET `/api/v1/admin/audit/retention`

**설명:** Retention periods, what is past them, and the last disposition

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminAuditController_retention`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/audit/retention" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-audit-retention--category-"></a>
### PUT `/api/v1/admin/audit/retention/{category}`

**설명:** Append a retention policy version for one category

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminAuditController_setRetention`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `category` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `retentionDays` (`number`) **(필수)**
  - `legalBasis` (`string`) **(필수)**
  - `description` (`string`) **(필수)**
  - `effectiveAt` (`string`) *(선택)*
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/audit/retention/{category}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-audit-verifications"></a>
### GET `/api/v1/admin/audit/verifications`

**설명:** Past chain verifications

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminAuditController_verifications`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/audit/verifications" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-audit-verify"></a>
### POST `/api/v1/admin/audit/verify`

**설명:** Recompute the hash chain over a window and record the result

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminAuditController_verify`
#### 📦 요청 본문 (Request Body)

  - `fromSequence` (`string`) *(선택)*
  - `toSequence` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/audit/verify" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-bank"></a>
### GET `/api/v1/admin/bank`

**설명:** Deposits and the loan book, with the credit ladder behind it

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminBankOperationsController_overview`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `limit` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/bank" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-business-types"></a>
### GET `/api/v1/admin/business-types`

**설명:** Every business type, including inactive ones

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_businessTypes`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/business-types" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="patch--api-v1-admin-business-types--id-"></a>
### PATCH `/api/v1/admin/business-types/{id}`

**설명:** Rename, redescribe or deactivate a business type

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `GameCatalogController_updateBusinessType`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `name` (`string`) *(선택)*
  - `description` (`string`) *(선택)*
  - `active` (`boolean`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PATCH "https://easy-scraping.com/api/v1/admin/business-types/{id}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-controls"></a>
### GET `/api/v1/admin/controls`

**설명:** Feature switches, economy policy versions and role assignments

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminControlsController_overview`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/controls" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-controls-auto-policy"></a>
### GET `/api/v1/admin/controls/auto-policy`

**설명:** Policy knobs plus classical proposal and matching AI review evidence

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_autoPolicy`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/controls/auto-policy" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-controls-auto-policy-knobs--knobkey-"></a>
### PUT `/api/v1/admin/controls/auto-policy/knobs/{knobKey}`

**설명:** Take one knob off automatic, or move its approved range

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_setPolicyKnob`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `knobKey` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**
  - `autoAdjustable` (`boolean`) *(선택)*
  - `minValue` (`string`) *(선택)* - decimal string or number
  - `maxValue` (`string`) *(선택)* - decimal string or number

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/controls/auto-policy/knobs/{knobKey}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-controls-auto-policy-runs"></a>
### POST `/api/v1/admin/controls/auto-policy/runs`

**설명:** Run the dual-lane automatic adjustment now instead of waiting for Monday

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminControlsController_runAutoPolicy`
#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/controls/auto-policy/runs" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-controls-consent-versions"></a>
### POST `/api/v1/admin/controls/consent-versions`

**설명:** Publish a new terms and privacy policy version (Superadmin only)

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_publishConsentVersion`
#### 📦 요청 본문 (Request Body)

  - `termsVersion` (`string`) **(필수)**
  - `privacyVersion` (`string`) **(필수)**
  - `reason` (`string`) **(필수)**
  - `confirmText` (`string`) **(필수)** - Must exactly match PUBLISH_NEW_POLICY_VERSION
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/controls/consent-versions" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-controls-consent-versions"></a>
### GET `/api/v1/admin/controls/consent-versions`

**설명:** List recent terms and privacy policy versions

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_listConsentVersions`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/controls/consent-versions" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-controls-feature-switches--featurekey-"></a>
### PUT `/api/v1/admin/controls/feature-switches/{featureKey}`

**설명:** Enable, pause, put into safe mode or disable a feature

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_setFeatureSwitch`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `featureKey` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `state` (`string`) **(필수)**
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/controls/feature-switches/{featureKey}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="put--api-v1-admin-controls-feature-switches-economy-auto-policy"></a>
### PUT `/api/v1/admin/controls/feature-switches/economy_auto_policy`

**설명:** Enable or pause the automatic economy policy without step-up

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminControlsController_setAutoPolicyFeatureSwitch`
#### 📦 요청 본문 (Request Body)

  - `state` (`string`) **(필수)**
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/controls/feature-switches/economy_auto_policy" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-controls-policies"></a>
### POST `/api/v1/admin/controls/policies`

**설명:** Create an economy policy version, immediate or scheduled

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_createPolicyVersion`
#### 📦 요청 본문 (Request Body)

  - `version` (`string`) **(필수)**
  - `effectiveAt` (`string`) *(선택)* - ISO 8601 instant; omitted means immediately
  - `payload` (`object`) **(필수)**
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/controls/policies" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-controls-policies-activations"></a>
### POST `/api/v1/admin/controls/policies/activations`

**설명:** Activate every policy version whose effective time has passed

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_activateDuePolicies`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/controls/policies/activations" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-controls-policies-rollbacks"></a>
### POST `/api/v1/admin/controls/policies/rollbacks`

**설명:** Return the economy to the previous policy version

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_rollbackPolicy`
#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/controls/policies/rollbacks" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-controls-role-revocations"></a>
### POST `/api/v1/admin/controls/role-revocations`

**설명:** Take back an administrative role

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_revokeRole`
#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**
  - `userId` (`string`) **(필수)**
  - `role` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/controls/role-revocations" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-controls-roles"></a>
### POST `/api/v1/admin/controls/roles`

**설명:** Grant an administrative role, or move the superadmin designation

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminControlsController_grantRole`
#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**
  - `userId` (`string`) **(필수)**
  - `role` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/controls/roles" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-discord"></a>
### GET `/api/v1/admin/discord`

**설명:** Discord delivery: which types are routed, and what is stuck

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminDiscordOperationsController_overview`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/discord" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-discord-outbox-events"></a>
### GET `/api/v1/admin/discord-outbox-events`

**설명:** Recent Discord outbox deliveries

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminController_discordOutboxEvents`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/discord-outbox-events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-economy"></a>
### GET `/api/v1/admin/economy`

**설명:** Money supply, issuance and burn, concentration and operational health

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_dashboard`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-economy-ai-status"></a>
### GET `/api/v1/admin/economy/ai-status`

**설명:** Economy AI feature switch, latest council review and agent scoreboard

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_aiStatus`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy/ai-status" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-economy-alerts"></a>
### GET `/api/v1/admin/economy/alerts`

**설명:** Alerts, unacknowledged first

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_alerts`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `limit` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy/alerts" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-economy-alerts--id--acknowledgements"></a>
### POST `/api/v1/admin/economy/alerts/{id}/acknowledgements`

**설명:** Acknowledge an alert

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_acknowledgeAlert`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/economy/alerts/{id}/acknowledgements" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-economy-bulk-payouts"></a>
### POST `/api/v1/admin/economy/bulk-payouts`

**설명:** Pay every member the filter matches

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_executeBulkPayout`
#### 📦 요청 본문 (Request Body)

  - `userIds` (`array`) *(선택)*
  - `minWorkCompletions` (`number`) *(선택)*
  - `stageCode` (`string`) *(선택)* - A progression stage code
  - `amount` (`number`) **(필수)** - WLD per member
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)** - One key for the batch; re-send it to retry

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/economy/bulk-payouts" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-economy-bulk-payouts--id--report"></a>
### GET `/api/v1/admin/economy/bulk-payouts/{id}/report`

**설명:** Who was paid, who was skipped and who failed, one row each

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_payoutReport`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy/bulk-payouts/{id}/report" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-economy-bulk-payouts-previews"></a>
### POST `/api/v1/admin/economy/bulk-payouts/previews`

**설명:** Count the members a payout would reach, and what it would cost

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_previewBulkPayout`
#### 📦 요청 본문 (Request Body)

  - `userIds` (`array`) *(선택)*
  - `minWorkCompletions` (`number`) *(선택)*
  - `stageCode` (`string`) *(선택)* - A progression stage code
  - `amount` (`number`) **(필수)** - WLD per member

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/economy/bulk-payouts/previews" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-economy-killswitch"></a>
### POST `/api/v1/admin/economy/killswitch`

**설명:** Toggle master killswitch or module circuit breaker

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_toggleKillswitch`
#### 📦 요청 본문 (Request Body)

  - `scope` (`string`) **(필수)**
  - `active` (`boolean`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/economy/killswitch" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-economy-knobs-v2"></a>
### POST `/api/v1/admin/economy/knobs-v2`

**설명:** Update economic knobs (interest, bond yields, loan rates)

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_updateKnobsV2`
#### 📦 요청 본문 (Request Body)

  - `depositRateBps` (`number`) **(필수)** - 일일 복리 이자율 bps (예: 5 = 0.05%)
  - `bond7dBps` (`number`) **(필수)** - 7일 국채 만기 수익률 bps (예: 100 = 1.0%)
  - `bond30dBps` (`number`) **(필수)** - 30일 국채 만기 수익률 bps (예: 500 = 5.0%)
  - `loanRateBps` (`number`) **(필수)** - 대출 일일 이자율 bps (예: 10 = 0.1%)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/economy/knobs-v2" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-economy-macro-v2"></a>
### GET `/api/v1/admin/economy/macro-v2`

**설명:** Admin Control Center 2.0 Macro Economy statistics

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_macroV2`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy/macro-v2" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-economy-reconciliations-latest"></a>
### GET `/api/v1/admin/economy/reconciliations/latest`

**설명:** Most recent economy reconciliation health snapshot

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `ReconciliationController_latest`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy/reconciliations/latest" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-economy-scenario-lab-preview"></a>
### GET `/api/v1/admin/economy/scenario-lab/preview`

**설명:** Read-only deterministic economy scenario projection

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_scenarioLabPreview`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy/scenario-lab/preview" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-economy-stats"></a>
### GET `/api/v1/admin/economy/stats`

**설명:** Realtime Faucet vs Sink stats and circulation summary

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_stats`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy/stats" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-economy-transactions--id--reversal"></a>
### POST `/api/v1/admin/economy/transactions/{id}/reversal`

**설명:** Reverse one transaction, posting its opposite back to the ledger

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_reverseTransaction`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/economy/transactions/{id}/reversal" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-economy-users--id--inspect-v2"></a>
### GET `/api/v1/admin/economy/users/{id}/inspect-v2`

**설명:** Inspect user wallet, deposits, loans, jobs, businesses

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_inspectUserV2`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/economy/users/{id}/inspect-v2" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-economy-users--id--override-v2"></a>
### POST `/api/v1/admin/economy/users/{id}/override-v2`

**설명:** Override user cash or bank balance (grant or confiscate WLD)

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminEconomyController_overrideUserV2`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `assetType` (`string`) **(필수)**
  - `amount` (`string`) **(필수)**
  - `direction` (`string`) **(필수)**
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)** - Caller-owned idempotency key for retry-safe asset overrides

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/economy/users/{id}/override-v2" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-me"></a>
### GET `/api/v1/admin/me`

**설명:** Roles held by the caller

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminController_me`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/me" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-photos"></a>
### POST `/api/v1/admin/photos`

**설명:** Upload image bytes to the private store

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `PhotoUploadController_upload`
#### 📦 요청 본문 (Request Body)

*(빈 객체)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/photos" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-safety-chat-reports"></a>
### GET `/api/v1/admin/safety/chat-reports`

**설명:** 관리자 1:1 개인 채팅 신고 큐 목록 조회

- **분류 도메인 (Tag):** `safety`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `SafetyController_adminListChatReports`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `status` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/safety/chat-reports" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-safety-chat-reports--id-"></a>
### GET `/api/v1/admin/safety/chat-reports/{id}`

**설명:** 관리자 1:1 개인 채팅 신고 상세 및 증거 스냅샷 열람

- **분류 도메인 (Tag):** `safety`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `SafetyController_adminGetChatReport`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/safety/chat-reports/{id}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-safety-chat-reports--id--action"></a>
### POST `/api/v1/admin/safety/chat-reports/{id}/action`

**설명:** 관리자 1:1 개인 채팅 신고 조치 실행

- **분류 도메인 (Tag):** `safety`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `SafetyController_adminActionChatReport`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/safety/chat-reports/{id}/action" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-safety-takedowns"></a>
### GET `/api/v1/admin/safety/takedowns`

**설명:** 관리자 긴급 콘텐츠 삭제 큐 조회

- **분류 도메인 (Tag):** `safety`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `SafetyController_adminListTakedowns`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `status` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/safety/takedowns" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-safety-takedowns--caseid--action"></a>
### POST `/api/v1/admin/safety/takedowns/{caseId}/action`

**설명:** 관리자 긴급 콘텐츠 삭제 조치

- **분류 도메인 (Tag):** `safety`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `SafetyController_adminActionTakedown`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `caseId` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/safety/takedowns/{caseId}/action" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-season-events"></a>
### GET `/api/v1/admin/season-events`

**설명:** Every season event, including inactive ones

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_seasonEvents`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/season-events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-season-events"></a>
### POST `/api/v1/admin/season-events`

**설명:** Create a season event in the active season

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_createSeasonEvent`
#### 📦 요청 본문 (Request Body)

  - `title` (`string`) **(필수)**
  - `description` (`string`) *(선택)*
  - `costWld` (`number`) **(필수)**
  - `pointsPerEntry` (`number`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/season-events" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="patch--api-v1-admin-season-events--id-"></a>
### PATCH `/api/v1/admin/season-events/{id}`

**설명:** Retitle, redescribe or deactivate a season event

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_updateSeasonEvent`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `title` (`string`) *(선택)*
  - `description` (`string`) *(선택)*
  - `active` (`boolean`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PATCH "https://easy-scraping.com/api/v1/admin/season-events/{id}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-security"></a>
### GET `/api/v1/admin/security`

**설명:** Console session state and login policy

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminSecurityController_overview`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/security" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-security-forced-logouts"></a>
### POST `/api/v1/admin/security/forced-logouts`

**설명:** End every live session a member holds

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminSecurityController_forceLogout`
#### 📦 요청 본문 (Request Body)

  - `userId` (`string`) **(필수)**
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/security/forced-logouts" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-security-ip-blocks"></a>
### GET `/api/v1/admin/security/ip-blocks`

**설명:** List current and historical service IP blocks

- **분류 도메인 (Tag):** `admin-security`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AbuseSecurityController_ipBlocks`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/security/ip-blocks" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-security-ip-blocks"></a>
### POST `/api/v1/admin/security/ip-blocks`

**설명:** Block an IP address or CIDR until manually lifted

- **분류 도메인 (Tag):** `admin-security`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AbuseSecurityController_blockAddress`
#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**
  - `network` (`string`) **(필수)**
  - `reason` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/security/ip-blocks" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="delete--api-v1-admin-security-ip-blocks--id-"></a>
### DELETE `/api/v1/admin/security/ip-blocks/{id}`

**설명:** Lift a service IP block

- **분류 도메인 (Tag):** `admin-security`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AbuseSecurityController_liftAddress`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X DELETE "https://easy-scraping.com/api/v1/admin/security/ip-blocks/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-security-login-policies--userid-"></a>
### PUT `/api/v1/admin/security/login-policies/{userId}`

**설명:** Replace the address allowlist for an administrator

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminSecurityController_setIpAllowlist`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `userId` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `networks` (`array`) **(필수)** - Networks in CIDR form
  - `reason` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/security/login-policies/{userId}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-security-sessions"></a>
### POST `/api/v1/admin/security/sessions`

**설명:** Enter the operations console, rotating the session

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminSecurityController_openConsole`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/security/sessions" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="delete--api-v1-admin-security-sessions"></a>
### DELETE `/api/v1/admin/security/sessions`

**설명:** Leave the operations console

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminSecurityController_closeConsole`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **204** | - | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X DELETE "https://easy-scraping.com/api/v1/admin/security/sessions" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-security-users--id--permanent-suspension"></a>
### POST `/api/v1/admin/security/users/{id}/permanent-suspension`

**설명:** Permanently restrict a member and revoke all live sessions

- **분류 도메인 (Tag):** `admin-security`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AbuseSecurityController_suspendMember`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/security/users/{id}/permanent-suspension" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-shop-items"></a>
### GET `/api/v1/admin/shop/items`

**설명:** List all items in the catalog for admin inspection

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminShopController_listItems`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/shop/items" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="patch--api-v1-admin-shop-items--id-"></a>
### PATCH `/api/v1/admin/shop/items/{id}`

**설명:** Update price, active status, or stock of a catalog item

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminShopController_updateItem`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PATCH "https://easy-scraping.com/api/v1/admin/shop/items/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-stocks"></a>
### GET `/api/v1/admin/stocks`

**설명:** Every stock, including inactive ones

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `GameCatalogController_stockList`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/stocks" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-stocks"></a>
### POST `/api/v1/admin/stocks`

**설명:** List a new stock

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_createStock`
#### 📦 요청 본문 (Request Body)

  - `symbol` (`string`) **(필수)**
  - `name` (`string`) **(필수)**
  - `description` (`string`) *(선택)*
  - `price` (`number`) **(필수)**
  - `shares` (`number`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/stocks" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="patch--api-v1-admin-stocks--id-"></a>
### PATCH `/api/v1/admin/stocks/{id}`

**설명:** Rename, redescribe or deactivate a stock

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_updateStock`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `name` (`string`) *(선택)*
  - `description` (`string`) *(선택)*
  - `active` (`boolean`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PATCH "https://easy-scraping.com/api/v1/admin/stocks/{id}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="delete--api-v1-admin-stocks--id-"></a>
### DELETE `/api/v1/admin/stocks/{id}`

**설명:** Delete a stock that has no history

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_deleteStock`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X DELETE "https://easy-scraping.com/api/v1/admin/stocks/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-stocks--id--corporate-actions"></a>
### POST `/api/v1/admin/stocks/{id}/corporate-actions`

**설명:** Apply a split or reverse split

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_corporateAction`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `action` (`string`) **(필수)**
  - `factor` (`number`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)** - Client-generated key reused when retrying the same mutation

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/stocks/{id}/corporate-actions" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-stocks--id--halt"></a>
### POST `/api/v1/admin/stocks/{id}/halt`

**설명:** Halt stock trading and auto-settle all holdings into cost-basis WLD

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_haltStock`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/stocks/{id}/halt" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-stocks--id--halt-settlement"></a>
### GET `/api/v1/admin/stocks/{id}/halt-settlement`

**설명:** Get stock halt settlement progress and statistics

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_getHaltSettlement`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/stocks/{id}/halt-settlement" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-stocks--id--halt-settlement-retry"></a>
### POST `/api/v1/admin/stocks/{id}/halt-settlement/retry`

**설명:** Retry failed or quarantined stock halt settlements

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_retryHaltSettlement`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/stocks/{id}/halt-settlement/retry" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-stocks--id--price"></a>
### POST `/api/v1/admin/stocks/{id}/price`

**설명:** Set a stock price by hand

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_setStockPrice`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `price` (`number`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)** - Client-generated key reused when retrying the same mutation

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/stocks/{id}/price" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-stocks-dynamics"></a>
### GET `/api/v1/admin/stocks/dynamics`

**설명:** Trend, volatility and fair value per stock

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `GameCatalogController_stockDynamics`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/stocks/dynamics" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-stocks-market-events"></a>
### GET `/api/v1/admin/stocks/market-events`

**설명:** Recent market events, ended and cancelled included

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `GameCatalogController_marketEvents`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/stocks/market-events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-stocks-market-events"></a>
### POST `/api/v1/admin/stocks/market-events`

**설명:** Publish a market event: news that leans the market

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `GameCatalogController_publishMarketEvent`
#### 📦 요청 본문 (Request Body)

  - `stockId` (`string`) *(선택)* - Absent for the whole market
  - `direction` (`string`) **(필수)**
  - `strength` (`number`) **(필수)**
  - `hours` (`number`) **(필수)**
  - `headline` (`string`) **(필수)**
  - `body` (`string`) *(선택)*
  - `source` (`string`) *(선택)*
  - `idempotencyKey` (`string`) **(필수)** - Client-generated key reused when retrying the same mutation

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/stocks/market-events" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="delete--api-v1-admin-stocks-market-events--id-"></a>
### DELETE `/api/v1/admin/stocks/market-events/{id}`

**설명:** End a market event now

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `GameCatalogController_cancelMarketEvent`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X DELETE "https://easy-scraping.com/api/v1/admin/stocks/market-events/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-support-threads"></a>
### GET `/api/v1/admin/support/threads`

**설명:** Administrator support inbox

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminSupportController_threads`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `status` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/support/threads" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-support-threads--id--messages"></a>
### GET `/api/v1/admin/support/threads/{id}/messages`

**설명:** Read a support conversation as administrator

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminSupportController_messages`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/support/threads/{id}/messages" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-support-threads--id--messages"></a>
### POST `/api/v1/admin/support/threads/{id}/messages`

**설명:** Reply to a member support conversation

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminSupportController_reply`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `body` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/support/threads/{id}/messages" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="put--api-v1-admin-support-threads--id--status"></a>
### PUT `/api/v1/admin/support/threads/{id}/status`

**설명:** Change support conversation status

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminSupportController_status`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `status` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/support/threads/{id}/status" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-treasury-drain"></a>
### POST `/api/v1/admin/treasury/drain`

**설명:** 국고 잉여 자금 영구 소각 (Step-Up/Admin)

- **분류 도메인 (Tag):** `Admin Treasury`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminTreasuryController_absorbFunds`
#### 📦 요청 본문 (Request Body)

  - `vaultCode` (`string`) **(필수)** - 금고 코드
  - `amountWld` (`string`) **(필수)** - 금액 (정수 WLD)
  - `reason` (`string`) **(필수)** - 감사 사유 (최소 10자)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/treasury/drain" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-treasury-inject"></a>
### POST `/api/v1/admin/treasury/inject`

**설명:** 국고 자금 긴급 주입 (Step-Up/Admin)

- **분류 도메인 (Tag):** `Admin Treasury`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminTreasuryController_injectFunds`
#### 📦 요청 본문 (Request Body)

  - `vaultCode` (`string`) **(필수)** - 금고 코드
  - `amountWld` (`string`) **(필수)** - 금액 (정수 WLD)
  - `reason` (`string`) **(필수)** - 감사 사유 (최소 10자)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/treasury/inject" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-treasury-overview"></a>
### GET `/api/v1/admin/treasury/overview`

**설명:** 중앙 국고 및 비축금 현황 대시보드 조회

- **분류 도메인 (Tag):** `Admin Treasury`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminTreasuryController_getOverview`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/treasury/overview" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-treasury-transactions"></a>
### GET `/api/v1/admin/treasury/transactions`

**설명:** 국고 원장 입출금 및 순환 감사 내역 조회

- **분류 도메인 (Tag):** `Admin Treasury`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminTreasuryController_listTransactions`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `limit` | `number` | 선택 | - |
| `query` | `cursor` | `string` | 선택 | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/treasury/transactions" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-users"></a>
### GET `/api/v1/admin/users`

**설명:** Members and their status

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminController_users`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/users" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-users--id--portfolio"></a>
### GET `/api/v1/admin/users/{id}/portfolio`

**설명:** Detailed user asset portfolio

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminController_userPortfolio`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/users/{id}/portfolio" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-users--id--restriction"></a>
### PUT `/api/v1/admin/users/{id}/restriction`

**설명:** Restrict or unrestrict a member

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AdminController_restrict`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `restricted` (`boolean`) **(필수)**
  - `reason` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/users/{id}/restriction" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-work"></a>
### GET `/api/v1/admin/work`

**설명:** The work catalogue, the reward policy in force, and job levels

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminWorkOperationsController_overview`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/work" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-work-auto-tune"></a>
### POST `/api/v1/admin/work/auto-tune`

**설명:** Automatically calculate and tune daily reward cap based on economy health

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminWorkOperationsController_autoTune`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/work/auto-tune" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-work-policy"></a>
### PUT `/api/v1/admin/work/policy`

**설명:** Update work reward policy daily cap, weekly cap, and repeat decay

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminWorkOperationsController_updatePolicy`
#### 📦 요청 본문 (Request Body)

  - `dailyCap` (`number`) *(선택)*
  - `weeklyCap` (`number`) *(선택)*
  - `repeatDecayPercent` (`number`) *(선택)*
  - `enabled` (`boolean`) *(선택)*
  - `reason` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/work/policy" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-work-stats"></a>
### GET `/api/v1/admin/work/stats`

**설명:** Real-time 24h work ranking, daily cap usage buckets, and 7-day trend

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `AdminWorkOperationsController_stats`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/work/stats" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="patch--api-v1-admin-work-tasks--id-"></a>
### PATCH `/api/v1/admin/work/tasks/{id}`

**설명:** Update base reward, duration, daily limit, and active state of a work task

- **분류 도메인 (Tag):** `admin`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AdminWorkOperationsController_updateTask`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `baseReward` (`number`) *(선택)*
  - `baseExperience` (`number`) *(선택)*
  - `minimumDurationSeconds` (`number`) *(선택)*
  - `dailyLimit` (`number`) *(선택)*
  - `active` (`boolean`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PATCH "https://easy-scraping.com/api/v1/admin/work/tasks/{id}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-privacy-requests"></a>
### GET `/api/v1/privacy/requests`

**설명:** Data subject requests the caller has made

- **분류 도메인 (Tag):** `privacy`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `PrivacyController_list`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/privacy/requests" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-privacy-requests"></a>
### POST `/api/v1/privacy/requests`

**설명:** Raise a data subject request

- **분류 도메인 (Tag):** `privacy`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `PrivacyController_create`
#### 📦 요청 본문 (Request Body)

  - `requestType` (`string`) **(필수)**
  - `detail` (`string`) *(선택)*
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/privacy/requests" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-safety-takedown"></a>
### POST `/api/v1/safety/takedown`

**설명:** 비회원 공개 긴급 콘텐츠 삭제 접수

- **분류 도메인 (Tag):** `safety`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `SafetyController_submitEmergencyTakedown`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/safety/takedown" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-safety-takedown-status"></a>
### POST `/api/v1/safety/takedown/status`

**설명:** 비회원 접수 상태 조회

- **분류 도메인 (Tag):** `safety`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `SafetyController_getTakedownStatus`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/safety/takedown/status" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-version"></a>
### GET `/api/v1/version`

**설명:** Backend runtime identity

- **분류 도메인 (Tag):** `Version`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `VersionController_check`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/version" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--health"></a>
### GET `/health`

**설명:** Liveness probe

- **분류 도메인 (Tag):** `Health`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `HealthController_check`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/health" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

