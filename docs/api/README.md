# 🌐 머니버스(Woldeok Moneyverse) 통합 REST API 마스터 레퍼런스 (v2026.09.23 쇄신판)

본 문서는 머니버스 프로덕션 시스템의 전체 **358개 REST API 엔드포인트**에 대한 포괄적이고 권위 있는 공식 기술 레퍼런스입니다.
실제 런타임 URL(`/api/v1` 프리픽스 및 예외 라우트), 보안 가드 스택, 멱등성 규약 및 영구 소각 규칙이 100% 온전히 반영되었습니다.

---

## 🏛️ 주요 아키텍처 및 공통 호출 규격

### 1. 엔드포인트 베이스 URL 및 라우팅 규칙
- **프로덕션 게이트웨이**: `https://easy-scraping.com`
- **표준 API 프리픽스**: `/api/v1` (모든 일반 API는 `/api/v1/...`로 호출)
- **프리픽스 예외 라우트 (UNPREFIXED_ROUTES)**:
  - `GET /health` (오케스트레이터 및 헬스체크 프로브)
  - `GET /media/:key` 및 `GET /media/profile/:key` (정적 미디어)
  - `GET /auth/:provider/authorize` 및 `GET /auth/:provider/callback` (OAuth 리다이렉트 콜백)
- **모바일/앱 게이트웨이**: `/app-api/v1` (BFF 프록시를 통해 내부 토큰 자동 주입)

### 2. 표준 보안 및 인증 헤더
| 헤더명 | 필수 여부 | 설명 |
| :--- | :---: | :--- |
| `Cookie` | **필수** (보호 라우트) | `__Host-session=<UUID>` 브라우저/클라이언트 인증 세션 쿠키 |
| `x-csrf-token` | **필수** (상태 변경 C/U/D) | CSRF 공격 방지를 위한 이중 토큰 (Double Submit Cookie) |
| `x-internal-token` | **내부 전용** | Next.js BFF 및 내부 마이크로서비스 간 서버-투-서버 신뢰 토큰 (클라이언트 노출 금지) |
| `x-request-id` | 선택 | 분산 트레이싱 및 디버깅용 요청 고유 UUID |

### 3. 보안 가드 (Guard Stack) 범례
- `🔒 로그인 필수`: 회원 인증 세션 필요 (`AuthenticatedGuard`, 미인증 시 401)
- `📜 약관동의 필수`: 서비스 이용약관 및 개인정보 동의 완료 필요 (`ConsentGuard`, 미동의 시 403)
- `🛡️ 최근재인증 필수 (15분)`: 최근 900초 이내 비밀번호/소셜 재인증 필요 (`ReauthGuard`)
- `👑 운영진 전용`: 플랫폼 관리자 권한 필요 (`AdminGuard`, 미보유 시 403)
- `⭐ 최고운영진 전용`: 슈퍼어드민 최고 권한 필요 (`SuperadminGuard`)
- `🔑 2FA TOTP 필수`: 2차 인증 스텝업 필요 (`SecondFactorGuard`)
- `🔓 공개 (게스트 허용)`: 비로그인 사용자 및 검색 봇 접근 가능

### 4. 상태 변경 요청의 멱등성 (Idempotency Contract)
- 자산 이동, 결제, 상점 구매, 경매 입찰, 세금 납부 등 모든 상태 변경(POST/PUT) 엔드포인트는 클라이언트가 생성한 UUID v4 형식의 `idempotencyKey`를 본문에 포함해야 합니다.
- 동일한 키로 중복 요청 시, 시스템은 중복 거래를 실행하지 않고 최초 처리 결과를 캐시에서 원자적으로 반환합니다.

### 5. 통화 소각 코드 (Permanent Sinks)
머니버스는 통화 가치 안정화를 위해 다양한 소각 코드로 WLD를 100% 영구 폐기합니다:
- `SINK_PROPERTY_TAX`: 가상 부동산 일일 정액 보유세
- `SINK_AUCTION_FEE`: 경매 체결 시 2% 시스템 수수료
- `SINK_APPRAISAL_FEE`: 공인 시스템 감정소 발급 수수료 (`max(250 WLD, ceil(0.25%))`)
- `SINK_SUPPLY_CHAIN_PROCUREMENT`: 사업체 원자재 공급망 조달 수수료 (2%)
- `SINK_PROJECT_DONATION`: 공공 도시 인프라 크라우드펀딩 출자액 (100%)
- `SINK_HOUSING_PURCHASE`: 개인 공간 분양 대금 (100%)

---

## 📚 도메인별 상세 명세서 카탈로그 (Total: 358 Operations)

| 번호 | 도메인 명세서 파일 | 포함 태그 | API 개수 | 설명 |
| :---: | :--- | :--- | :---: | :--- |
| **01** | [01-auth-and-account.md](./01-auth-and-account.md) | `auth`, `account` | 28 | 로컬/소셜 인증, 세션, 2FA/TOTP, 비밀번호 관리 |
| **02** | [02-wallet-and-banking.md](./02-wallet-and-banking.md) | `wallet`, `banking`, `game-clock` | 23 | WLD 지갑, P2P 송금, 은행 예적금, 대출 및 포켓 |
| **03** | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) | `stocks`, `newspaper`, `businesses` | 34 | 가상 증시 호가/차트, 매수/매도, 경제 신문, 가상 사업체 공급망 |
| **04** | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) | `marketplace`, `crafting` | 17 | 고정가 장터, 에스크로 경매, P2P 1:1 직거래, 공인 감정소, 제작 |
| **05** | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) | `spaces`, `clubs`, `seasons`, `collections` | 33 | 부동산 분양/세무 구청(보유세 소각), 클럽 캔버스, 시즌 보상, 컬렉션 |
| **06** | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) | `work`, `casino`, `progression`, `engagement`, `early-game` | 33 | 직업 노동, 카지노(주사위/코인), 레벨업, 업적 |
| **07** | [07-community-board-chat.md](./07-community-board-chat.md) | `board`, `chat`, `content`, `profile`, `activity`, `discord` | 61 | 게시판 글/댓글, 실시간 채팅, 갤러리 미디어, 프로필 |
| **08** | [08-admin-control-tower.md](./08-admin-control-tower.md) | `admin`, `Admin Treasury`, `admin-security`, `safety`, `privacy`, `Health`, `Version` | 128 | 실시간 관제 타워, 국고, 감사 로그, 피처 플래그, 사용자 제재 |

---

## 🔎 전체 API 색인 테이블 (Total: 358 Endpoints)

| Method | 실제 호출 경로 (Actual URL) | 기능 명칭 (Summary) | 보안 가드 (Security Guards) | 도메인 명세서 |
| :---: | :--- | :--- | :--- | :--- |
| **DELETE** | `/api/v1/account` | 회원 본인 계정 영구 삭제 (회원 탈퇴 처리) | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/api/v1/account/identities` | 계정에 연동된 로그인 수단 목록 조회 (OAuth & Local) | `🔒 로그인 필수` `📜 약관동의 필수` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **DELETE** | `/api/v1/account/identities/{id}` | 지정한 소셜 로그인 수단 연동 해제 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/account/identities/{provider}/link` | 새로운 소셜 로그인 수단 연동 시작 (OAuth 리다이렉트) | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/api/v1/account/security/events` | 최근 계정 보안 감사 이벤트 로그 조회 (로그인/비밀번호 변경 등) | `🔒 로그인 필수` `📜 약관동의 필수` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/api/v1/account/security/sessions` | 현재 접속 중인 모든 활성 세션 목록 조회 (기기 및 IP 정보) | `🔒 로그인 필수` `📜 약관동의 필수` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **DELETE** | `/api/v1/account/security/sessions/{id}` | 지정한 다른 기기의 원격 세션 강제 종료 (로그아웃) | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/account/security/sessions/revoke-others` | 현재 접속 기기를 제외한 모든 타 기기 세션 일괄 종료 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/{provider}/reauthentication` | 민감 작업용 OAuth 스텝업 재인증 개시 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **PUT** | `/api/v1/auth/consent` | 필수 서비스 이용약관 및 개인정보 처리방침 동의 기록 | `🛡️ CSRF 검증` `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/email-change/complete` | 이메일 변경 인증 토큰 확인 및 이메일 교체 완료 | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/email-change/request` | 새로운 로그인 이메일 변경 확인 메일 발송 요청 | `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/login` | 이메일/비밀번호 로컬 계정 로그인 | `🛡️ CSRF 검증` `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/password-reset/complete` | 비밀번호 재설정 토큰 검증 및 새 비밀번호 설정 | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/password-reset/request` | 비밀번호 재설정 일회용 인증 링크 발송 요청 | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/password/change` | 비밀번호 변경 (최근 재인증 필요) | `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/reauthentication` | 현재 계정 비밀번호 재확인 (스텝업 재인증) | `🛡️ CSRF 검증` `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/register` | 이메일/비밀번호 로컬 회원가입 신청 (인증 메일 발송) | `🛡️ CSRF 검증` `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/local/verify-email` | 이메일 인증 토큰 검증 및 계정 정식 활성화 | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/logout` | 현재 세션 로그아웃 및 보안 쿠키 무효화 | `🛡️ CSRF 검증` `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/mobile/handoff` | Exchange a one-time native OAuth handoff for an app session | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/api/v1/auth/policy` | Currently published consent version | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **POST** | `/api/v1/auth/prelogin-session` | Create or reuse the pre-login session | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/api/v1/auth/providers` | Sign-in providers this deployment can offer | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/api/v1/auth/session` | 현재 세션의 CSRF 공격 방지용 이중 토큰 조회 | `🔒 로그인 필수` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/api/v1/auth/viewer` | 내비게이션 및 UI 렌더링용 현재 세션 프로필(뷰어) 상태 조회 | `🔓 공개 (게스트 허용)` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/auth/{provider}/authorize` | OAuth 소셜 로그인 인증 세션 개시 (Discord / Google) | `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/auth/{provider}/callback` | OAuth 콜백 처리 및 브라우저 세션 쿠키 발급 | `👤 세션 필요` | [01-auth-and-account.md](./01-auth-and-account.md) |
| **GET** | `/api/v1/bank/loans` | Outstanding loans for the caller | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/bank/loans` | Borrow from the virtual bank | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/bank/loans/{id}/repayments` | Repay part or all of a loan | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/bank/movements` | Move balance between cash and bank | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/bonds/{id}/redeem` | Redeem matured virtual bond and payout principal with yield | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/bonds/purchase` | Purchase 7-day or 30-day virtual government bonds | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/borrow` | Borrow smart credit loan evaluated by job level and business value | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/claim-interest` | Claim accrued compound deposit interest into bank balance | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/deposit` | Deposit WLD cash into bank compound interest deposit account | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **GET** | `/api/v1/banking/pockets` | List all saving pockets for current user | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/pockets` | Create a new saving pocket | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **PUT** | `/api/v1/banking/pockets/{id}` | Customize saving pocket appearance and theme | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/pockets/{id}/archive` | Archive saving pocket and recover all funds to cash balance | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/pockets/{id}/transfer` | Transfer funds between main bank balance and saving pocket | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/repay` | Repay active bank loan | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **GET** | `/api/v1/banking/standing` | Bank overview: cash, deposit balance, compound interest, loans, bonds | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/banking/withdraw` | Withdraw WLD from bank deposit account to cash | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **GET** | `/api/v1/game-clock` | Read the authoritative accelerated Moneyverse server day/week | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **GET** | `/api/v1/rewards/availability` | Next eligible times for the caller reward controls | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/rewards/daily/claims` | Claim the daily reward | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/rewards/work/claims` | Retired legacy work faucet; use professional work tasks | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **GET** | `/api/v1/wallet` | Balances and recent ledger entries for the caller | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **POST** | `/api/v1/wallet/transfers` | Send WLD to another member | `🔓 공개 (게스트 허용)` | [02-wallet-and-banking.md](./02-wallet-and-banking.md) |
| **GET** | `/api/v1/business-equity` | The own capital the caller can put behind a purchase | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/business-types` | Business types available to buy, excluding types already owned | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/business-types/{id}/purchases` | Buy a business of this type | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/businesses` | Businesses the caller owns | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/businesses/{id}/boost` | Equip a boost item from inventory to business | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/businesses/{id}/procure` | Procure raw materials for business with WLD payment | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/businesses/{id}/settle-v2` | Settle a day of revenue with active boosts and double-entry ledger sink | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/businesses/{id}/settlements` | Settle a day of revenue | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/businesses/{id}/storage/upgrade` | Upgrade business storage capacity | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/businesses/{id}/supply-chain` | Get supply chain inventory, storage capacity and demand factors | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/businesses/activate-license` | Activate a business using a purchased license item from inventory | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/businesses/catalog` | App API alias: business types available to buy, excluding owned types | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/businesses/catalog/{id}/purchases` | App API alias: buy a business from the catalogue | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/businesses/equity` | App API alias: own capital available for a business purchase | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/businesses/my-v2` | Enhanced businesses the caller owns with boosts and settlement status | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/newspaper/lore` | 주간 금융 개념 배움터 아티클 목록 조회 | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/newspaper/poll` | 주간 독자 여론조사 현황 조회 | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/newspaper/poll/vote` | 주간 독자 여론조사 투표 참여 | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/newspaper/pulse` | 실시간 월드 펄스 및 시장 심리 조회 | `🔓 공개 (게스트 허용)` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks` | Listed stocks and their current prices | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/{id}/candles` | Open/high/low/close for one stock at a given interval | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/stocks/{id}/orders` | Buy or sell a stock | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/{id}/prices` | Recorded price history for one stock | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/stocks/{id}/watchlist` | Add or remove a stock from the caller watchlist | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/alerts` | Conditional virtual-stock alerts belonging to the caller | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/stocks/alerts` | Create a server-evaluated virtual-stock alert | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **DELETE** | `/api/v1/stocks/alerts/{id}` | Delete one virtual-stock alert belonging to the caller | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/alerts/events` | Recent virtual-stock alert events belonging to the caller | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/halt-receipts` | Stock halt cost-basis settlement receipts for caller | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/history` | Trades made by the caller | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/market-events` | Market events currently in effect | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/portfolio` | Holdings of the caller | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/sparklines` | Recent prices for every listed stock | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **GET** | `/api/v1/stocks/watchlist` | Stocks watched by the caller | `🔒 로그인 필수` `📜 약관동의 필수` | [03-stocks-and-businesses.md](./03-stocks-and-businesses.md) |
| **POST** | `/api/v1/crafting/execute` | Execute crafting recipe: consume materials and WLD fee to mint crafted item | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **GET** | `/api/v1/crafting/recipes` | List all official crafting recipes with required materials and fees | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **GET** | `/api/v1/marketplace/appraisals` | List my issued provenance appraisal certificates | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/appraisals` | Request system provenance appraisal for collectible with fee burn | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **GET** | `/api/v1/marketplace/auctions` | List active live English auctions | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/auctions` | Create a new live English auction | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/auctions/{id}/bid` | Bid on a live English auction with escrow and anti-sniping extension | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **GET** | `/api/v1/marketplace/listings` | List active player marketplace listings with filter and search | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/listings` | List an item for sale in player marketplace with escrow lock | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/listings/{id}/buy` | Buy a marketplace listing item with 1% burn fee and 99% seller settlement | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/listings/{id}/cancel` | Cancel active marketplace listing and recover item to inventory | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **GET** | `/api/v1/marketplace/my-listings` | List current user marketplace listings and trade history | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **GET** | `/api/v1/marketplace/trades` | List my P2P direct trades | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/trades` | Propose a new P2P 1:1 direct trade | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/trades/{id}/accept` | Accept a P2P 1:1 direct trade proposal (first step) | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/trades/{id}/cancel` | Cancel a P2P 1:1 direct trade | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **POST** | `/api/v1/marketplace/trades/{id}/confirm` | Sign-off and execute dual atomic swap for P2P 1:1 trade | `🔓 공개 (게스트 허용)` | [04-marketplace-and-crafting.md](./04-marketplace-and-crafting.md) |
| **GET** | `/api/v1/clubs` | 클럽 목록 탐색 및 검색 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/clubs` | 신규 클럽 창설 (10,000 WLD 소각) | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/clubs/{id}` | 클럽 상세 정보 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/clubs/{id}/canvas` | 소속 클럽하우스 12x12 가구 배치 그리드 및 장식 점수 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **PUT** | `/api/v1/clubs/{id}/canvas` | 클럽하우스 12x12 공유 캔버스 레이아웃 및 장식 점수 서버 영구 저장 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/clubs/{id}/feed` | 클럽 피드 글 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/clubs/{id}/feed` | 클럽 피드 또는 공지사항 작성 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/clubs/{id}/join` | 클럽 공개 가입 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/clubs/{id}/leave` | 클럽 탈퇴 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/clubs/{id}/members` | 클럽 회원 명부 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **PATCH** | `/api/v1/clubs/{id}/members/{userId}/role` | 클럽 회원 역할 변경 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/clubs/{id}/projects` | 협동 프로젝트 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/clubs/{id}/projects/{projectId}/contributions` | 협동 프로젝트 WLD 펀딩 기여 (영구 소각) | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/collections` | 내 수집품 조각 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **PUT** | `/api/v1/collections/{id}` | 수집품 유저 메모 및 즐겨찾기 수정 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/collections/curation/advance` | 소유권 큐레이션 사다리 단계 진척 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/collections/curation/status` | D1~D7 소유권 큐레이션 사다리 상태 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/seasons/claim-rewards` | 시즌 보상 청구 및 수령 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/seasons/current` | 현재 시즌 정보 및 내 티어/랭킹 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/seasons/events` | Active season events | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/seasons/events/{id}/consumptions` | Spend on a season event | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/seasons/events/{id}/leaderboard` | Leaderboard for one event | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/seasons/hall-of-fame` | 역대 시즌 명예의 전당 헌액자 목록 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/seasons/settle` | 시즌 종료 정산 엔진 (명예의 전당 및 6대 티어 보상 분배) | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/spaces` | 사용자가 분양받아 보유 중인 나만의 개인 공간 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/spaces/{id}` | 지정한 개인 공간의 상세 스펙 및 소유권 정보 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **PUT** | `/api/v1/spaces/{id}/layout` | 개인 공간 8x8 인터랙티브 가구 배치 및 인테리어 레이아웃 저장 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/spaces/{id}/tax/pay` | 개인 공간 일일 부동산세 자진 납부 (100% 영구 소각 SINK_PROPERTY_TAX) | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/spaces/{id}/tax/status` | 공간별 일일 보유세율, 완납 기한, 체납 일수 및 공매 상태 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/spaces/city/projects` | 머니버스 시민 공동 출자 공공 인프라 크라우드펀딩 프로젝트 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/spaces/city/projects/{id}/contributions` | 공공 도시 인프라 크라우드펀딩 WLD 출자 기여 (100% 소각 SINK_PROJECT_DONATION) | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **POST** | `/api/v1/spaces/purchase` | 신규 개인 공간 분양 신청 (대금 100% 영구 소각 SINK_HOUSING_PURCHASE) | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/spaces/tax/delinquencies` | 부동산세 7일 이상 체납으로 법정 유예 경과한 시청 강제 공매 매물 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [05-spaces-clubs-seasons.md](./05-spaces-clubs-seasons.md) |
| **GET** | `/api/v1/casino/clock` | Read the authoritative accelerated server day and week | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/casino/coin/fairness` | The disclosed win probability and the trial that evidences it | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/casino/coin/plays` | Stake WLD on one toss of the coin | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/casino/coin/terms` | The odds, the stake limits, and what today has already used | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/casino/dice/fairness` | Each dice game’s disclosed odds and the trial evidencing them | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/casino/dice/plays` | Stake WLD on one roll of the die | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/casino/games/terms` | Every game’s odds, payout and remaining exposure for today | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/casino/history` | Read the current member's recent casino plays | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/casino/jackpot` | Read current casino house reserve and jackpot pool | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **PUT** | `/api/v1/casino/self-limit` | Set the daily caps and the lock the member holds themselves to | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/casino/self-limit` | Read the daily limits chosen by the current member | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/casino/theme/plays` | Stake WLD on one play of a theme catalog game | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/early-game/claims` | Claim today’s event, once | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/early-game/first-day` | The seven steps of 16.1’s first day, counted from what happened | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/early-game/today` | The event this member is dealt today, and whether it is still theirs | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/engagement` | Today’s goals, this week’s goals, the next unlock and the preference | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/engagement/early-game` | The early-game weekly goals and collection books | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/engagement/npcs/{code}/orders` | Take an order from an NPC | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **PUT** | `/api/v1/engagement/preferences` | Set whether the member hears about their goals | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/progression` | The caller’s growth stage and what unlocks the next one | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/progression/credit` | The caller’s credit grade, what each grade buys, and their loans | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/progression/early-game` | The early-game unlock ladder and what the caller has reached | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/progression/refreshes` | Recompute the caller’s growth stage from their progress | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/work` | Caps, what has been paid against them, and open assignments | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/work/active-job` | Switch active job among 8 specialization careers | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/work/assignments` | The caller’s recent assignments | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/work/assignments` | Take a task | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/work/assignments/{id}/completions` | Submit a taken task as done | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/work/assignments/{id}/verify` | Verify a submitted task and pay it, within the caps | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/work/profile` | Current active job and all job masteries | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/work/receipts` | What the work paid, and the ledger transaction it paid through | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **GET** | `/api/v1/work/tasks` | Every task on offer, with this member’s standing against each | `🔓 공개 (게스트 허용)` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/work/tasks/{id}/complete` | Directly complete a career task with EXP and instant WLD faucet payout | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [06-gameplay-work-casino.md](./06-gameplay-work-casino.md) |
| **POST** | `/api/v1/activity/events` | Ingest client activity telemetry events (page view, dwell, clicks) | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/admin/activity/logs` | List user activity logs for administrators | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/admin/activity/traffic` | Privacy-safe day/month/year traffic analytics for administrators | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/admin/announcements` | Create or edit an announcement | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/admin/announcements` | List all announcements for administrators | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **PUT** | `/api/v1/admin/announcements/{id}` | Update an announcement | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **DELETE** | `/api/v1/admin/announcements/{id}` | Delete an announcement | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **PUT** | `/api/v1/admin/announcements/{id}/image` | Attach an uploaded image to a draft announcement | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **PUT** | `/api/v1/admin/announcements/{id}/publication` | Publish or unpublish an announcement | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/admin/photos` | List all gallery photos for administrators | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **DELETE** | `/api/v1/admin/photos/{id}` | Delete a draft or published photo | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/admin/photos/{id}/approval` | Approve and publish a pending member photo | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **PUT** | `/api/v1/admin/photos/{id}/publication` | Publish or unpublish a photo | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/admin/photos/metadata` | Create or edit a photo record | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/admin/photos/submissions` | List pending photo submissions awaiting review | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/announcements` | Published announcements | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/images/{key}` | Read an image attached to a visible board post | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/board/images/uploads` | Upload one image for a board post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/posts` | Recent member board posts | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/board/posts` | Write a post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/posts/{id}` | One post, with its body | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **PUT** | `/api/v1/board/posts/{id}` | Rewrite your own post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **DELETE** | `/api/v1/board/posts/{id}` | Delete your own post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/posts/{id}/comments` | The replies on a post, oldest first | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/board/posts/{id}/comments` | Reply to a post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **DELETE** | `/api/v1/board/posts/{id}/comments/{commentId}` | Delete your own reply | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/public/images/{key}` | Public image attached to a visible board post | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/public/posts` | Public recent board posts | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/public/posts/{id}` | Public board post | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/public/posts/{id}/comments` | Public replies on a board post | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/board/public/stock-posts` | 엔드포인트 상세 | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/board/stock-posts` | 엔드포인트 상세 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/chat/conversations` | 1:1 대화방 생성 또는 기존 대화방 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/chat/conversations` | 참여 중인 1:1 대화방 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/chat/conversations/{id}/archive` | 대화방 보관 또는 보관 해제 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/chat/conversations/{id}/messages` | 대화방 메시지 이력 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/chat/conversations/{id}/messages` | 대화방에 1:1 쪽지 메시지 전송 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/chat/conversations/{id}/mute` | 대화방 알림 음소거 또는 해제 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/chat/conversations/{id}/read` | 대화방 메시지 읽음 처리 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/chat/conversations/{id}/report` | 부적절한 대화 내용 신고 및 증거 스냅샷 접수 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/chat/unread-count` | 안 읽은 전체 쪽지 개수 조회 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/chat/users/{id}/block` | 특정 회원 1:1 쪽지 차단 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **DELETE** | `/api/v1/chat/users/{id}/block` | 특정 회원 1:1 쪽지 차단 해제 | `🔒 로그인 필수` `📜 약관동의 필수` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/content/announcements` | App API: published announcements | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/content/photos` | App API: published gallery photos | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/content/status` | App API: service status board | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/integrations/discord/interactions` | Discord interaction webhook | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/photos` | Published gallery photos | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/photos` | Send an uploaded photo to the gallery for review | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/photos/mine` | The caller’s own submissions and where each one got to | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/photos/uploads` | Upload image bytes and receive a storage key | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/profile` | The caller’s own profile, with every field | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **PUT** | `/api/v1/profile` | Replace the caller’s profile and its per-field visibility | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/profile/{userId}` | Another member’s profile, as they have chosen to show it | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/profile/image` | Upload a profile picture, replacing the current one | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **DELETE** | `/api/v1/profile/image` | Remove the profile picture | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/profile/settings` | The caller’s own profile settings, as stored | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/profile/titles` | Profile titles actually awarded to the caller | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/api/v1/status` | Server status board | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/media/{key}` | Bytes of a published gallery photo | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **GET** | `/media/profile/{key}` | Bytes of a member’s profile picture, on their terms | `🔓 공개 (게스트 허용)` | [07-community-board-chat.md](./07-community-board-chat.md) |
| **POST** | `/api/v1/admin/ai-news/auto-generate` | Auto-generate and optionally publish market news based on currently registered active stocks | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/ai-news/batches` | Start a run that asks the model for five scenarios | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/ai-news/batches/latest` | The current batch of proposed scenarios, and the last run that asked for one | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/ai-news/models` | What the stored key can reach, as GET {base}/models lists it | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/ai-news/scenarios/{id}/discard` | Set a scenario aside | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/ai-news/scenarios/{id}/publish` | Publish a scenario as a market event, with the values the operator settled on | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/ai-news/settings` | API address, model name and whether a key is stored | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/ai-news/settings` | Store the model address, model name and, optionally, a new key | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/approvals` | Withdrawn: two-person approval was retired | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/approvals` | Withdrawn: two-person approval was retired | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/approvals/{id}/decisions` | Withdrawn: two-person approval was retired | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/audit-events` | Recent audit trail entries | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/audit/dispositions` | What was archived, destroyed or held | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/audit/dispositions` | Record what was decided about a range past its retention period | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/audit/events` | Search the audit trail; addresses and session hashes are masked | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/audit/events/{id}/reveal` | Unmask one entry; the reveal is itself recorded | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/audit/retention` | Retention periods, what is past them, and the last disposition | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/audit/retention/{category}` | Append a retention policy version for one category | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/audit/verifications` | Past chain verifications | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/audit/verify` | Recompute the hash chain over a window and record the result | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/bank` | Deposits and the loan book, with the credit ladder behind it | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/business-types` | Every business type, including inactive ones | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PATCH** | `/api/v1/admin/business-types/{id}` | Rename, redescribe or deactivate a business type | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/controls` | Feature switches, economy policy versions and role assignments | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/controls/auto-policy` | Policy knobs plus classical proposal and matching AI review evidence | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/controls/auto-policy/knobs/{knobKey}` | Take one knob off automatic, or move its approved range | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/controls/auto-policy/runs` | Run the dual-lane automatic adjustment now instead of waiting for Monday | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/controls/consent-versions` | Publish a new terms and privacy policy version (Superadmin only) | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/controls/consent-versions` | List recent terms and privacy policy versions | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/controls/feature-switches/{featureKey}` | Enable, pause, put into safe mode or disable a feature | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/controls/feature-switches/economy_auto_policy` | Enable or pause the automatic economy policy without step-up | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/controls/policies` | Create an economy policy version, immediate or scheduled | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/controls/policies/activations` | Activate every policy version whose effective time has passed | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/controls/policies/rollbacks` | Return the economy to the previous policy version | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/controls/role-revocations` | Take back an administrative role | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/controls/roles` | Grant an administrative role, or move the superadmin designation | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/discord` | Discord delivery: which types are routed, and what is stuck | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/discord-outbox-events` | Recent Discord outbox deliveries | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy` | Money supply, issuance and burn, concentration and operational health | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy/ai-status` | Economy AI feature switch, latest council review and agent scoreboard | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy/alerts` | Alerts, unacknowledged first | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/economy/alerts/{id}/acknowledgements` | Acknowledge an alert | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/economy/bulk-payouts` | Pay every member the filter matches | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy/bulk-payouts/{id}/report` | Who was paid, who was skipped and who failed, one row each | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/economy/bulk-payouts/previews` | Count the members a payout would reach, and what it would cost | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/economy/killswitch` | Toggle master killswitch or module circuit breaker | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/economy/knobs-v2` | Update economic knobs (interest, bond yields, loan rates) | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy/macro-v2` | Admin Control Center 2.0 Macro Economy statistics | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy/reconciliations/latest` | Most recent economy reconciliation health snapshot | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy/scenario-lab/preview` | Read-only deterministic economy scenario projection | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy/stats` | Realtime Faucet vs Sink stats and circulation summary | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/economy/transactions/{id}/reversal` | Reverse one transaction, posting its opposite back to the ledger | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/economy/users/{id}/inspect-v2` | Inspect user wallet, deposits, loans, jobs, businesses | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/economy/users/{id}/override-v2` | Override user cash or bank balance (grant or confiscate WLD) | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/me` | Roles held by the caller | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/photos` | Upload image bytes to the private store | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/safety/chat-reports` | 관리자 1:1 개인 채팅 신고 큐 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/safety/chat-reports/{id}` | 관리자 1:1 개인 채팅 신고 상세 및 증거 스냅샷 열람 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/safety/chat-reports/{id}/action` | 관리자 1:1 개인 채팅 신고 조치 실행 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/safety/takedowns` | 관리자 긴급 콘텐츠 삭제 큐 조회 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/safety/takedowns/{caseId}/action` | 관리자 긴급 콘텐츠 삭제 조치 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/season-events` | Every season event, including inactive ones | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/season-events` | Create a season event in the active season | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PATCH** | `/api/v1/admin/season-events/{id}` | Retitle, redescribe or deactivate a season event | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/security` | Console session state and login policy | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/security/forced-logouts` | End every live session a member holds | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/security/ip-blocks` | List current and historical service IP blocks | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/security/ip-blocks` | Block an IP address or CIDR until manually lifted | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **DELETE** | `/api/v1/admin/security/ip-blocks/{id}` | Lift a service IP block | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/security/login-policies/{userId}` | Replace the address allowlist for an administrator | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/security/sessions` | Enter the operations console, rotating the session | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **DELETE** | `/api/v1/admin/security/sessions` | Leave the operations console | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/security/users/{id}/permanent-suspension` | Permanently restrict a member and revoke all live sessions | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/shop/items` | List all items in the catalog for admin inspection | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PATCH** | `/api/v1/admin/shop/items/{id}` | Update price, active status, or stock of a catalog item | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/stocks` | Every stock, including inactive ones | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/stocks` | List a new stock | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PATCH** | `/api/v1/admin/stocks/{id}` | Rename, redescribe or deactivate a stock | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **DELETE** | `/api/v1/admin/stocks/{id}` | Delete a stock that has no history | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/stocks/{id}/corporate-actions` | Apply a split or reverse split | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/stocks/{id}/halt` | Halt stock trading and auto-settle all holdings into cost-basis WLD | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/stocks/{id}/halt-settlement` | Get stock halt settlement progress and statistics | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/stocks/{id}/halt-settlement/retry` | Retry failed or quarantined stock halt settlements | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/stocks/{id}/price` | Set a stock price by hand | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/stocks/dynamics` | Trend, volatility and fair value per stock | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/stocks/market-events` | Recent market events, ended and cancelled included | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/stocks/market-events` | Publish a market event: news that leans the market | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **DELETE** | `/api/v1/admin/stocks/market-events/{id}` | End a market event now | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/support/threads` | Administrator support inbox | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/support/threads/{id}/messages` | Read a support conversation as administrator | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/support/threads/{id}/messages` | Reply to a member support conversation | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/support/threads/{id}/status` | Change support conversation status | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/treasury/drain` | 국고 잉여 자금 영구 소각 (Step-Up/Admin) | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/treasury/inject` | 국고 자금 긴급 주입 (Step-Up/Admin) | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/treasury/overview` | 중앙 국고 및 비축금 현황 대시보드 조회 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/treasury/transactions` | 국고 원장 입출금 및 순환 감사 내역 조회 | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/users` | Members and their status | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/users/{id}/portfolio` | Detailed user asset portfolio | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/users/{id}/restriction` | Restrict or unrestrict a member | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/work` | The work catalogue, the reward policy in force, and job levels | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/admin/work/auto-tune` | Automatically calculate and tune daily reward cap based on economy health | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PUT** | `/api/v1/admin/work/policy` | Update work reward policy daily cap, weekly cap, and repeat decay | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/admin/work/stats` | Real-time 24h work ranking, daily cap usage buckets, and 7-day trend | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **PATCH** | `/api/v1/admin/work/tasks/{id}` | Update base reward, duration, daily limit, and active state of a work task | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `👑 운영진 전용` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/privacy/requests` | Data subject requests the caller has made | `🔒 로그인 필수` `📜 약관동의 필수` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/privacy/requests` | Raise a data subject request | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/safety/takedown` | 비회원 공개 긴급 콘텐츠 삭제 접수 | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **POST** | `/api/v1/safety/takedown/status` | 비회원 접수 상태 조회 | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/api/v1/version` | Backend runtime identity | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
| **GET** | `/health` | Liveness probe | `🔓 공개 (게스트 허용)` | [08-admin-control-tower.md](./08-admin-control-tower.md) |
