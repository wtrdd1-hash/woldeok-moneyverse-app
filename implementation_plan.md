# [통합 구현 계획서] Woldeok Moneyverse 모바일 앱 결함 해결 및 실서버 API 완전 동기화 (v1)

공식 모바일 API 통합 명세서(`mobile-api-complete-spec.md`) 및 BFF 아키텍처 규격에 따라 식별된 14대 결함(OAuth 딥링크 수신 불가, 쿠키/세션 관리 결함, 쓰기 API DTO 불일치, 이메일 인증 누락, 보안 취약점 등)을 전면 해결하고 실서버 연동을 완결합니다.

## Viewed SKILL.md
- `oauth2-pkce-flow`
- `csrf-token-synchronizer`
- `restful-idempotency-header-redis`

---

## User Review Required

> [!IMPORTANT]
> **1. OAuth 복귀 스킴 및 호스트 변경 (`AndroidManifest.xml`)**
> - 기존: `woldeokmoneyverse` (오동작)
> - 변경: `woldeok-moneyverse://oauth/callback` (명세서 규격 일치)
> 
> **2. Chrome Custom Tabs 도입 (`androidx.browser:browser:1.8.0`)**
> - 외부 웹 브라우저 대신 인앱 Custom Tab을 통해 보안 인증 컨텍스트를 유지하고 앱 복귀 경험을 최적화합니다.
> 
> **3. 지갑 송금 및 은행 입출금 DTO 계약 변경 (422 에러 방지)**
> - 송금: `recipientUserId` (UUID), `amount: Long` (Number), `idempotencyKey: UUID`
> - 은행: `direction: "deposit" | "withdraw"` (소문자), `amount: Long`, `idempotencyKey: UUID`
> 
> **4. 구글 플레이 필수: 회원 탈퇴 (계정 삭제 `DELETE /account`) 진입점 추가**

---

## Proposed Changes

### 1. 매니페스트 및 빌드 설정 (Deep Link & Custom Tabs & Keystore 보안)

#### [MODIFY] [AndroidManifest.xml](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/AndroidManifest.xml)
- OAuth 리다이렉트 인텐트 필터를 정규 스펙(`scheme="woldeok-moneyverse"`, `host="oauth"`, `path="/callback"`)으로 전면 수정.
- `android:exported="true"` 및 `launchMode="singleTask"` 설정으로 딥링크 복귀 시 신규 인텐트 전달 보장.

#### [MODIFY] [gradle/libs.versions.toml](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/gradle/libs.versions.toml)
- `androidx-browser = { group = "androidx.browser", name = "browser", version = "1.8.0" }` 의존성 추가.

#### [MODIFY] [app/build.gradle.kts](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/build.gradle.kts)
- `implementation(libs.androidx.browser)` 추가.
- `signingConfigs` 내 하드코딩된 비밀번호 제거 및 안전한 fallback 설정.

---

### 2. 네트워크 & 세션 엔진 (Persistent CookieJar & CSRF & 로깅 보안)

#### [NEW] [PersistentCookieJar.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/data/remote/PersistentCookieJar.kt)
- OkHttp의 `CookieJar` 인터페이스를 구현하는 영속 쿠키 관리자 신설.
- SharedPreferences에 직렬화하여 앱 재시작 후에도 `__Host-mv_session` 및 세션 쿠키 완벽 보존.
- 도메인/경로 일치 자동 검증 및 쿠키 헤더 자동 주입.

#### [MODIFY] [ApiClient.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/data/remote/ApiClient.kt)
- `OkHttpClient.Builder()`에 `cookieJar(persistentCookieJar)` 장착.
- `HttpLoggingInterceptor` 레벨을 `Level.HEADERS`로 조정하고, 민감 정보(비밀번호, 쿠키, CSRF, handoff code)가 노출되지 않도록 새니타이징.

#### [MODIFY] [MockApiInterceptor.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/data/remote/MockApiInterceptor.kt)
- 5xx 및 네트워크 예외 발생 시 임의로 Mock 데이터로 변환하던 장애 은폐 로직 전면 제거.
- `isMockModeEnabled == true`일 때만 Mock 응답을 반환하도록 순수 분리.

---

### 3. API 인터페이스 및 DTO 데이터 모델 동기화

#### [MODIFY] [DataModels.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/data/model/DataModels.kt)
- `VerifyEmailRequest(val token: String)` 추가.
- `TransferRequest(val recipientUserId: String, val amount: Long, val idempotencyKey: String)` 필드명 및 타입 수정.
- `BankMovementRequest(val direction: String, val amount: Long, val idempotencyKey: String)` 수정.
- `BorrowRequest(val amount: Long, val idempotencyKey: String)`, `RepayRequest(val amount: Long, val idempotencyKey: String)` 수정.

#### [MODIFY] [MoneyverseApi.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/data/remote/MoneyverseApi.kt)
- `@POST("app-api/v1/auth/local/verify-email")` 추가.
- `@GET("app-api/v1/auth/session")` 추가.
- `@DELETE("app-api/v1/account")` 추가.

---

### 4. 비즈니스 레포지토리 & 뷰모델 (인증 플로우 & 멱등성 & 에러 핸들링)

#### [MODIFY] [MoneyverseRepositories.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/data/repository/MoneyverseRepositories.kt)
- `AuthRepository`:
  - `verifyEmail(token: String)` 구현: `verify-email` 성공 시 신규 세션 쿠키/CSRF 반영 후 `/auth/viewer` 검증.
  - `deleteAccount()` 구현: 계정 삭제 후 세션 및 로컬 저장소 완전 초기화.
  - `getOAuthAuthorizeUrl`: BFF를 통해 정규 `authorizationUrl` 수신.
- `WalletRepository`:
  - 송금/입출금/대출 시 클라이언트 UUID `idempotencyKey` 자동 생성 및 타입 매핑.

#### [MODIFY] [MoneyverseViewModels.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/ui/viewmodel/MoneyverseViewModels.kt)
- `AuthViewModel`:
  - `verifyEmail(token: String)` 추가.
  - `deleteAccount()` 추가.
  - `isVerificationPending` 상태 관리 추가 (회원가입 후 인증코드 입력 대기 상태).
- `HomeViewModel`, `EconomyViewModel`, `PlayViewModel`, `CommunityViewModel`:
  - `init` 블록에서 자동 호출하던 방식 대신, 로그인 완료 신호를 수신했을 때만 데이터를 호출하는 지연 로딩(Lazy Fetch) 구조로 리팩터링.

---

### 5. UI 화면 및 사용자 인터랙션 (Auth, Economy, My)

#### [MODIFY] [MainActivity.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/MainActivity.kt)
- `isLoggedIn` 상태를 전역 `StateFlow` 또는 싱글톤 옵저버블 상태로 일원화하여 OAuth 핸드오프 완료 시 메인 화면으로 즉각 전환되도록 수정.
- 인증 완료 시 각 ViewModel에 `loadData()` 이벤트 발행.

#### [MODIFY] [AuthScreen.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/ui/screen/AuthScreen.kt)
- OAuth 버튼 클릭 시 Custom Tabs(`CustomTabsIntent`)를 사용하여 BFF에서 획득한 URL 실행.
- 이메일 회원가입 완료 후 202 응답 시 이메일 인증 토큰(`token`) 입력 다이얼로그 팝업 제공.
- 액션 버튼 중복 클릭 방지 (Pending Lock).

#### [MODIFY] [EconomyScreen.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/ui/screen/EconomyScreen.kt)
- 입출금 `direction`을 소문자(`deposit`/`withdraw`)로 전달.
- 송금/입출금 처리 중 버튼 비활성화 및 로딩 인디케이터 표시.

#### [MODIFY] [MyScreen.kt](file:///c:/Users/sds/AndroidStudioProjects/WoldeokMoneyverse/app/src/main/java/com/example/woldeokmoneyverse/ui/screen/MyScreen.kt)
- 하드코딩된 사용자 정보(`wtrdd@woldeok.com`)를 실제 로그인된 사용자 세션 정보(`SessionManager`)로 동적 바인딩.
- "회원 탈퇴 (계정 삭제)" 버튼 및 재확인 다이얼로그 추가 (`DELETE /account` 연동).

---

## Verification Plan

### Automated Tests
- Gradle 컴파일 및 빌드 검증:
  `./gradlew compileDebugKotlin`
  `./gradlew assembleDebug`

### Manual Verification
1. **OAuth 딥링크 & Custom Tab 검증**:
   - Google / Discord OAuth 버튼 클릭 시 Custom Tab 구동 확인.
   - `woldeok-moneyverse://oauth/callback?code=...` 딥링크 수신 시 앱 진입 및 핸드오프 교환 확인.
2. **이메일 회원가입 & 인증 검증**:
   - 회원가입 요청 후 인증번호 입력 다이얼로그 노출 확인.
   - 인증번호 제출 시 `/auth/local/verify-email` 호출 및 메인 진입 확인.
3. **지갑 송금 & 입출금 DTO 검증**:
   - UUID 멱등키 및 숫자형 amount 전송 시 백엔드 422 에러 없이 200/201 성공 여부 확인.
4. **마이페이지 계정 삭제 & 동적 프로필 검증**:
   - 실시간 로그인된 유저 프로필 표출 확인.
   - 계정 삭제 다이얼로그 동작 및 로그아웃 처리 확인.

---

## 🚀 [v2 Execution & Verification Complete] 구현 및 컴파일 검증 완료 보고

### 1. 완료된 변경 내역 (14대 결함 전수 해결)
1. **[빌드 & 매니페스트] Chrome Custom Tabs & 딥링크 복구**:
   - `gradle/libs.versions.toml`: `androidx.browser:browser:1.8.0` 의존성 추가
   - `app/build.gradle.kts`: 브라우저 의존성 반영 및 Keystore 비밀번호 환경변수 분리
   - `AndroidManifest.xml`: `woldeok-moneyverse://oauth/callback` 인텐트 필터 및 `singleTask` 적용
2. **[세션 & 보안 쿠키 엔진] 영속 쿠키 & 장애 은폐 제거**:
   - `PersistentCookieJar.kt` 신설: SharedPreferences 기반 `__Host-mv_session` 및 세션 쿠키 영속 저장/동기화
   - `ApiClient.kt`: `PersistentCookieJar` 연동, 민감 정보(헤더, 쿠키, 토큰) 마스킹 로거 적용, `init(context)` 및 `clearSession()` 지원
   - `MockApiInterceptor.kt`: 서버 장애 시 가짜 Mock JSON을 반환하여 에러를 은폐하던 버그 제거 (`isMockModeEnabled` 상태에서만 동작)
3. **[API & DTO 규격 동기화] 백엔드 계약 전면 일치**:
   - `DataModels.kt`: `VerifyEmailRequest(token)` 추가, `AuthResponse`에 `verificationRequired` 필드 추가, `TransferRequest`(`recipientUserId`, `amount: Long`, `idempotencyKey: String`), `BankMovementRequest`(`direction: String`, `amount: Long`, `idempotencyKey: String`), `BorrowRequest`, `RepayRequest` 타입 수정
   - `MoneyverseApi.kt`: 이메일 인증(`POST /auth/local/verify-email`), 세션 조회(`GET /auth/session`), 회원 탈퇴(`DELETE /account`) 엔드포인트 추가
4. **[인증 & 비즈니스 로직] 멱등키 & 이메일 인증 & 세션 라이프사이클**:
   - `MoneyverseRepositories.kt`: `AuthRepository`의 `verifyEmail`, `deleteAccount`, `exchangeMobileHandoff`, `WalletRepository` 멱등키 UUID 발급 적용
   - `MoneyverseViewModels.kt`: `AuthViewModel`에 이메일 인증 코드 입력 대기 상태(`isVerificationPending`) 및 `verifyEmail`, `deleteAccount` 구현
5. **[UI & UX 인터랙션] 반응형 인증 플로우 & 구글 플레이 컴플라이언스**:
   - `MainActivity.kt`: `_isLoggedInFlow` StateFlow를 통한 인증 상태 즉각 반영, 딥링크 수신 즉시 반응, 로그인 후 지연 데이터 로드(Lazy load) 적용
   - `AuthScreen.kt`: Chrome Custom Tabs 연동, 버튼 중복 탭 방지, 이메일 인증번호 팝업 다이얼로그 추가
   - `EconomyScreen.kt`: 소문자 `deposit`/`withdraw` direction 전달, 폼 유효성 검사 적용
   - `MyScreen.kt`: `SessionManager` 세션 정보 동적 표출, 구글 플레이 정책 필수 항목인 "회원 탈퇴 (계정 삭제)" 버튼 및 삭제 확인 다이얼로그 추가

### 2. 자동 검증 결과 (Automated Verification)
- **명령어**: `.\gradlew.bat compileDebugKotlin`
- **결과**: `BUILD SUCCESSFUL` (Exit Code 0, 6 actionable tasks executed)
- **상태**: 문법 오류 및 심볼 참조 오류 0건, 전체 코틀린 소스 정상 컴파일 검증 완료.

---

## [v3 — APP-API-UNIVERSAL-001] 모든 BFF API 접근성 유지 (DONE, 2026-09-22)

### 요구사항
- 앱 UI/기능을 수정하더라도 네트워크 계층 때문에 기존 또는 신규 BFF API를 호출하지 못하는 상황을 방지한다.
- 기존 typed Retrofit API는 유지하고, 미정의 API를 즉시 연결할 수 있는 범용 transport 경로를 병행한다.
- 운영 BFF origin 고정, 세션 쿠키, CSRF, telemetry, compatibility interceptor 보안 경계는 그대로 유지한다.

### 구현
- `MoneyverseApi`에 범용 GET/POST/PUT/PATCH/DELETE 경로를 추가했다.
- POST/PUT/PATCH/DELETE는 임의 `RequestBody`, 모든 범용 경로는 동적 `@Url`과 `@HeaderMap`을 지원한다.
- 범용 응답은 `ResponseBody`로 받아 JSON 외 응답도 손실 없이 처리할 수 있게 했다.
- 기존 canonical typed API와 compatibility interceptor는 변경하지 않아 기존 화면 호환성을 보존한다.
- 앱 버전을 `1.0.17` / versionCode `18`로 갱신하고 User-Agent 버전을 일치시켰다.

### QA
- `MobileApiContractTest`에 GET/POST/PUT/PATCH/DELETE 범용 transport 계약 회귀 테스트 추가.
- `./gradlew testDebugUnitTest lintDebug`: BUILD SUCCESSFUL.
- 테스트 서버/운영 서버 승격은 서버 접근성 및 비파괴 스모크 결과를 별도 게이트로 확인한다.

### QA follow-up — APP-API-UNIVERSAL-002 (2026-09-22)
- 발견 결함 1: universal API의 사용자 지정 `Accept` 헤더가 공통 interceptor에서 `application/json`으로 덮어써짐.
- 수정: 호출자가 `Accept`를 지정하지 않은 경우에만 JSON 기본값 적용.
- 발견 결함 2: HTTP User-Agent는 1.0.17인데 telemetry 및 realtime client가 1.0.14를 전송.
- 수정: `ApiClient.APP_VERSION` 단일 상수로 HTTP/telemetry/realtime 버전 식별자를 통합.
- 회귀 테스트: 사용자 지정 Accept 보존, 단일 앱 버전 광고 테스트 추가.
- 재검증: `testDebugUnitTest + lintDebug + assembleDebug` BUILD SUCCESSFUL.
- 운영 공개/인증 경계 스모크: 공개 API 200, 인증 필요 API 401 login required 정상.
- GitHub Android CI run #184: success.

---

## [v4 — APP-PRIVATE-CHAT-001] Android 개인 1:1 채팅 연결 및 전체 API 기능 감사 (2026-09-23)

### QA 결론
- 백엔드/웹에는 1:1 개인 채팅 API가 존재하지만 Android 앱에는 화면·ViewModel·typed API가 누락되어 있었다.
- Android 앱에 대화방 생성/목록/안읽음/메시지 조회·전송/읽음/음소거/보관/차단·해제/신고를 추가했다.
- 커뮤니티 화면에 `개인 쪽지` 탭을 추가해 기존 실시간 로비/관리자 문의와 분리했다.
- 앱 버전은 1.0.18 / versionCode 19로 증가했다.
- `testDebugUnitTest + lintDebug + assembleDebug`: BUILD SUCCESSFUL.
- 운영 `GET /app-api/v1/chat/conversations`, `GET /app-api/v1/chat/unread-count`: 익명에서 401 login required로 route/auth boundary 정상 확인.

### 전체 API 기능 감사
Android 명시 연결 기능군:
account, admin, auth, bank, board, businesses, casino, chat, content, early-game, game-clock, photos, privacy, profile, progression, rewards, seasons, shop, stocks, support, wallet, work.

게이트웨이에 존재하지만 Android 명시 연결이 없는 기능군:
activity, banking, clubs, crafting, developer, engagement, marketplace, media, newspaper, notifications, spaces.

이 중 developer/activity/media/banking은 내부·보조·중복 성격을 추가 판별해야 하며,
clubs/crafting/marketplace/newspaper/notifications/spaces 등 사용자 기능은 Android 기능 동등성 미완료 항목으로 추적한다.

### 실제 단말 검증 제한
- SDK의 adb binary는 있으나 연결된 emulator가 offline이었고 reconnect 후 장치가 사라졌다.
- 현재 원격 환경에는 실행 가능한 emulator binary/온라인 Android 단말이 없어 실제 터치 E2E는 수행하지 못했다.
- 따라서 코드/계약/빌드 QA는 PASS지만 실제 단말 UI E2E는 BLOCKED 상태다.

---

## [v5 — APP-FULL-API-PARITY-001] Complete Android App API parity layer (DONE, 2026-09-23)

### Authority recheck
- Rechecked backend main at v2026.09.22.360 before and during implementation.
- Primary machine contract: mobile-api-contract.json, contract v2026.09.22.359, 179 endpoints.
- Runtime v360 additionally activates 11 private-chat operations while the published meta contract still reports v359.

### Implementation
- Added generated ApiFeatureRegistry covering all 179 machine-contract endpoints plus 11 v360 chat operations (190 total).
- Added an in-app “전체” feature hub grouped by all 33 gateway groups.
- Every registered GET/POST/PUT/DELETE route can use the existing persistent session, CSRF injection, production-origin pin, telemetry and compatibility interceptors through universal transport.
- Path parameters can be replaced in the app; JSON mutation templates are prefilled and idempotency UUID placeholders are generated at execution time.
- Added concrete body templates for saving pockets, crafting and marketplace operations whose generated contract schema was incomplete.
- Existing dedicated native screens remain authoritative for normal wallet, banking, stocks, businesses, shop, board, photos/media, profile, chat, support and other already-integrated flows.

### Coverage / QA
- Contract comparison: 179/179 official mobile-contract endpoints represented in Android; missing = 0.
- Runtime chat extension: 11/11 represented; total executable registry = 190.
- Gateway groups: 33/33 represented; groups with no current direct endpoint (for example clubs/developer/spaces) display that state instead of fake functionality.
- Production non-destructive GET smoke: 79 routes tested; HTTP 200 = 15, HTTP 401 = 64, HTTP 404 = 0.
- ./gradlew testDebugUnitTest lintDebug assembleDebug: BUILD SUCCESSFUL.
- git diff --check: PASS.
- Staging remains BLOCKED because staging.moneyverse.wtrdd1.com does not resolve.
- Physical/emulator UI E2E remains BLOCKED because no online Android device/emulator is available on the remote development host.

### Release gate
- Version: Android 1.0.19 / versionCode 20.
- Branch: feat/app-full-api-parity-v1.0.19.
- Do not promote to production until isolated staging and actual-device authenticated E2E are available and pass.
