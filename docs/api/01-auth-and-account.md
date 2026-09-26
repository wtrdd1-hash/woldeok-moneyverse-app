# 인증 & 계정 관리 API (Authentication & Account)

> 회원가입, 로컬 로그인, 세션 검증, 2FA/TOTP 스텝업 인증, OAuth(Discord/Google) 연동, 비밀번호 변경/재설정 및 계정 프로필 관리 엔드포인트

## 📋 목차 (Table of Contents)

- [DELETE `/api/v1/account`](#delete--api-v1-account) - 회원 본인 계정 영구 삭제 (회원 탈퇴 처리) | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증`
- [GET `/api/v1/account/identities`](#get--api-v1-account-identities) - 계정에 연동된 로그인 수단 목록 조회 (OAuth & Local) | `🔒 로그인 필수` `📜 약관동의 필수`
- [DELETE `/api/v1/account/identities/{id}`](#delete--api-v1-account-identities--id-) - 지정한 소셜 로그인 수단 연동 해제 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증`
- [POST `/api/v1/account/identities/{provider}/link`](#post--api-v1-account-identities--provider--link) - 새로운 소셜 로그인 수단 연동 시작 (OAuth 리다이렉트) | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/account/security/events`](#get--api-v1-account-security-events) - 최근 계정 보안 감사 이벤트 로그 조회 (로그인/비밀번호 변경 등) | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/account/security/sessions`](#get--api-v1-account-security-sessions) - 현재 접속 중인 모든 활성 세션 목록 조회 (기기 및 IP 정보) | `🔒 로그인 필수` `📜 약관동의 필수`
- [DELETE `/api/v1/account/security/sessions/{id}`](#delete--api-v1-account-security-sessions--id-) - 지정한 다른 기기의 원격 세션 강제 종료 (로그아웃) | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증`
- [POST `/api/v1/account/security/sessions/revoke-others`](#post--api-v1-account-security-sessions-revoke-others) - 현재 접속 기기를 제외한 모든 타 기기 세션 일괄 종료 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증`
- [POST `/api/v1/auth/{provider}/reauthentication`](#post--api-v1-auth--provider--reauthentication) - 민감 작업용 OAuth 스텝업 재인증 개시 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [PUT `/api/v1/auth/consent`](#put--api-v1-auth-consent) - 필수 서비스 이용약관 및 개인정보 처리방침 동의 기록 | `🛡️ CSRF 검증` `👤 세션 필요`
- [POST `/api/v1/auth/local/email-change/complete`](#post--api-v1-auth-local-email-change-complete) - 이메일 변경 인증 토큰 확인 및 이메일 교체 완료 | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/auth/local/email-change/request`](#post--api-v1-auth-local-email-change-request) - 새로운 로그인 이메일 변경 확인 메일 발송 요청 | `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` `👤 세션 필요`
- [POST `/api/v1/auth/local/login`](#post--api-v1-auth-local-login) - 이메일/비밀번호 로컬 계정 로그인 | `🛡️ CSRF 검증` `👤 세션 필요`
- [POST `/api/v1/auth/local/password-reset/complete`](#post--api-v1-auth-local-password-reset-complete) - 비밀번호 재설정 토큰 검증 및 새 비밀번호 설정 | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/auth/local/password-reset/request`](#post--api-v1-auth-local-password-reset-request) - 비밀번호 재설정 일회용 인증 링크 발송 요청 | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/auth/local/password/change`](#post--api-v1-auth-local-password-change) - 비밀번호 변경 (최근 재인증 필요) | `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` `👤 세션 필요`
- [POST `/api/v1/auth/local/reauthentication`](#post--api-v1-auth-local-reauthentication) - 현재 계정 비밀번호 재확인 (스텝업 재인증) | `🛡️ CSRF 검증` `👤 세션 필요`
- [POST `/api/v1/auth/local/register`](#post--api-v1-auth-local-register) - 이메일/비밀번호 로컬 회원가입 신청 (인증 메일 발송) | `🛡️ CSRF 검증` `👤 세션 필요`
- [POST `/api/v1/auth/local/verify-email`](#post--api-v1-auth-local-verify-email) - 이메일 인증 토큰 검증 및 계정 정식 활성화 | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/auth/logout`](#post--api-v1-auth-logout) - 현재 세션 로그아웃 및 보안 쿠키 무효화 | `🛡️ CSRF 검증` `👤 세션 필요`
- [POST `/api/v1/auth/mobile/handoff`](#post--api-v1-auth-mobile-handoff) - Exchange a one-time native OAuth handoff for an app session | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/auth/policy`](#get--api-v1-auth-policy) - Currently published consent version | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/auth/prelogin-session`](#post--api-v1-auth-prelogin-session) - Create or reuse the pre-login session | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/auth/providers`](#get--api-v1-auth-providers) - Sign-in providers this deployment can offer | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/auth/session`](#get--api-v1-auth-session) - 현재 세션의 CSRF 공격 방지용 이중 토큰 조회 | `🔒 로그인 필수`
- [GET `/api/v1/auth/viewer`](#get--api-v1-auth-viewer) - 내비게이션 및 UI 렌더링용 현재 세션 프로필(뷰어) 상태 조회 | `🔓 공개 (게스트 허용)`
- [GET `/auth/{provider}/authorize`](#get--auth--provider--authorize) - OAuth 소셜 로그인 인증 세션 개시 (Discord / Google) | `👤 세션 필요`
- [GET `/auth/{provider}/callback`](#get--auth--provider--callback) - OAuth 콜백 처리 및 브라우저 세션 쿠키 발급 | `👤 세션 필요`

---

## 🛠️ 엔드포인트 상세 규격

<a id="delete--api-v1-account"></a>
### DELETE `/api/v1/account`

**설명:** 회원 본인 계정 영구 삭제 (회원 탈퇴 처리)

- **분류 도메인 (Tag):** `account`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AccountController_remove`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **202** | 접수 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X DELETE "https://easy-scraping.com/api/v1/account" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-account-identities"></a>
### GET `/api/v1/account/identities`

**설명:** 계정에 연동된 로그인 수단 목록 조회 (OAuth & Local)

- **분류 도메인 (Tag):** `account`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `AccountController_identities`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/account/identities" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="delete--api-v1-account-identities--id-"></a>
### DELETE `/api/v1/account/identities/{id}`

**설명:** 지정한 소셜 로그인 수단 연동 해제

- **분류 도메인 (Tag):** `account`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AccountController_unlink`

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
curl -X DELETE "https://easy-scraping.com/api/v1/account/identities/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-account-identities--provider--link"></a>
### POST `/api/v1/account/identities/{provider}/link`

**설명:** 새로운 소셜 로그인 수단 연동 시작 (OAuth 리다이렉트)

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AuthController_startLink`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `provider` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/account/identities/{provider}/link" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-account-security-events"></a>
### GET `/api/v1/account/security/events`

**설명:** 최근 계정 보안 감사 이벤트 로그 조회 (로그인/비밀번호 변경 등)

- **분류 도메인 (Tag):** `account`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `AccountSecurityController_events`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/account/security/events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-account-security-sessions"></a>
### GET `/api/v1/account/security/sessions`

**설명:** 현재 접속 중인 모든 활성 세션 목록 조회 (기기 및 IP 정보)

- **분류 도메인 (Tag):** `account`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `AccountSecurityController_sessions`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/account/security/sessions" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="delete--api-v1-account-security-sessions--id-"></a>
### DELETE `/api/v1/account/security/sessions/{id}`

**설명:** 지정한 다른 기기의 원격 세션 강제 종료 (로그아웃)

- **분류 도메인 (Tag):** `account`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AccountSecurityController_revokeSession`

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
curl -X DELETE "https://easy-scraping.com/api/v1/account/security/sessions/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-account-security-sessions-revoke-others"></a>
### POST `/api/v1/account/security/sessions/revoke-others`

**설명:** 현재 접속 기기를 제외한 모든 타 기기 세션 일괄 종료

- **분류 도메인 (Tag):** `account`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AccountSecurityController_revokeOtherSessions`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/account/security/sessions/revoke-others" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-auth--provider--reauthentication"></a>
### POST `/api/v1/auth/{provider}/reauthentication`

**설명:** 민감 작업용 OAuth 스텝업 재인증 개시

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `AuthController_startReauthentication`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `provider` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/{provider}/reauthentication" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-auth-consent"></a>
### PUT `/api/v1/auth/consent`

**설명:** 필수 서비스 이용약관 및 개인정보 처리방침 동의 기록

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🛡️ CSRF 검증` `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `AuthController_consent`
#### 📦 요청 본문 (Request Body)

  - `termsCompleted` (`boolean`) **(필수)**
  - `privacyCompleted` (`boolean`) **(필수)**
  - `ageConfirmed` (`boolean`) **(필수)**
  - `termsVersion` (`string`) **(필수)**
  - `privacyVersion` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/auth/consent" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-email-change-complete"></a>
### POST `/api/v1/auth/local/email-change/complete`

**설명:** 이메일 변경 인증 토큰 확인 및 이메일 교체 완료

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `LocalAuthController_completeEmailChange`
#### 📦 요청 본문 (Request Body)

  - `token` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/email-change/complete" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-email-change-request"></a>
### POST `/api/v1/auth/local/email-change/request`

**설명:** 새로운 로그인 이메일 변경 확인 메일 발송 요청

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `LocalAuthController_requestEmailChange`
#### 📦 요청 본문 (Request Body)

  - `email` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **202** | 접수 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/email-change/request" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-login"></a>
### POST `/api/v1/auth/local/login`

**설명:** 이메일/비밀번호 로컬 계정 로그인

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🛡️ CSRF 검증` `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `LocalAuthController_login`
#### 📦 요청 본문 (Request Body)

  - `email` (`string`) **(필수)**
  - `password` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/login" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-password-reset-complete"></a>
### POST `/api/v1/auth/local/password-reset/complete`

**설명:** 비밀번호 재설정 토큰 검증 및 새 비밀번호 설정

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `LocalAuthController_completePasswordReset`
#### 📦 요청 본문 (Request Body)

  - `token` (`string`) **(필수)**
  - `password` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/password-reset/complete" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-password-reset-request"></a>
### POST `/api/v1/auth/local/password-reset/request`

**설명:** 비밀번호 재설정 일회용 인증 링크 발송 요청

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `LocalAuthController_requestPasswordReset`
#### 📦 요청 본문 (Request Body)

  - `email` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **202** | 접수 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/password-reset/request" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-password-change"></a>
### POST `/api/v1/auth/local/password/change`

**설명:** 비밀번호 변경 (최근 재인증 필요)

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🛡️ 최근재인증 필수 (15분)` `🛡️ CSRF 검증` `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `LocalAuthController_changePassword`
#### 📦 요청 본문 (Request Body)

  - `password` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/password/change" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-reauthentication"></a>
### POST `/api/v1/auth/local/reauthentication`

**설명:** 현재 계정 비밀번호 재확인 (스텝업 재인증)

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🛡️ CSRF 검증` `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `LocalAuthController_reauthenticate`
#### 📦 요청 본문 (Request Body)

  - `email` (`string`) **(필수)**
  - `password` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/reauthentication" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-register"></a>
### POST `/api/v1/auth/local/register`

**설명:** 이메일/비밀번호 로컬 회원가입 신청 (인증 메일 발송)

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🛡️ CSRF 검증` `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `LocalAuthController_register`
#### 📦 요청 본문 (Request Body)

  - `email` (`string`) **(필수)**
  - `password` (`string`) **(필수)** - Non-empty password. No numeric minimum length is enforced.
  - `displayName` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **202** | 접수 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/register" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-local-verify-email"></a>
### POST `/api/v1/auth/local/verify-email`

**설명:** 이메일 인증 토큰 검증 및 계정 정식 활성화

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `LocalAuthController_verifyEmail`
#### 📦 요청 본문 (Request Body)

  - `token` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/local/verify-email" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-auth-logout"></a>
### POST `/api/v1/auth/logout`

**설명:** 현재 세션 로그아웃 및 보안 쿠키 무효화

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🛡️ CSRF 검증` `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `AuthController_logout`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **204** | - | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/logout" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-auth-mobile-handoff"></a>
### POST `/api/v1/auth/mobile/handoff`

**설명:** Exchange a one-time native OAuth handoff for an app session

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AuthController_mobileHandoff`
#### 📦 요청 본문 (Request Body)

  - `code` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/mobile/handoff" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-auth-policy"></a>
### GET `/api/v1/auth/policy`

**설명:** Currently published consent version

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AuthBootstrapController_policy`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/auth/policy" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-auth-prelogin-session"></a>
### POST `/api/v1/auth/prelogin-session`

**설명:** Create or reuse the pre-login session

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AuthBootstrapController_preloginSession`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/auth/prelogin-session" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-auth-providers"></a>
### GET `/api/v1/auth/providers`

**설명:** Sign-in providers this deployment can offer

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AuthBootstrapController_providers`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/auth/providers" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-auth-session"></a>
### GET `/api/v1/auth/session`

**설명:** 현재 세션의 CSRF 공격 방지용 이중 토큰 조회

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수`
- **엔드포인트 핸들러 ID:** `AuthController_session`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/auth/session" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-auth-viewer"></a>
### GET `/api/v1/auth/viewer`

**설명:** 내비게이션 및 UI 렌더링용 현재 세션 프로필(뷰어) 상태 조회

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AuthBootstrapController_viewer`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/auth/viewer" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--auth--provider--authorize"></a>
### GET `/auth/{provider}/authorize`

**설명:** OAuth 소셜 로그인 인증 세션 개시 (Discord / Google)

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `AuthController_authorize`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `provider` | `string` | **필수** | - |
| `query` | `client` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/auth/{provider}/authorize" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--auth--provider--callback"></a>
### GET `/auth/{provider}/callback`

**설명:** OAuth 콜백 처리 및 브라우저 세션 쿠키 발급

- **분류 도메인 (Tag):** `auth`
- **보안 및 권한 계층 (Guards):** `👤 세션 필요`
- **엔드포인트 핸들러 ID:** `AuthController_callback`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `provider` | `string` | **필수** | - |
| `query` | `state` | `string` | **필수** | - |
| `query` | `code` | `string` | **필수** | - |
| `query` | `error` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/auth/{provider}/callback" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

