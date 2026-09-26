# 게임플레이 & 직업·카지노 API (Gameplay, Work & Casino)

> 직업 배정, 일일 노동 퀘스트 및 급여 수령, 레벨업/성장 단계, 주사위·코인플립 미니게임 플레이, 이용약관, 공정성(Provably Fair) 시드 검증 및 자가 보호 베팅 한도 설정 엔드포인트

## 📋 목차 (Table of Contents)

- [GET `/api/v1/casino/clock`](#get--api-v1-casino-clock) - Read the authoritative accelerated server day and week | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/casino/coin/fairness`](#get--api-v1-casino-coin-fairness) - The disclosed win probability and the trial that evidences it | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/casino/coin/plays`](#post--api-v1-casino-coin-plays) - Stake WLD on one toss of the coin | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/casino/coin/terms`](#get--api-v1-casino-coin-terms) - The odds, the stake limits, and what today has already used | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/casino/dice/fairness`](#get--api-v1-casino-dice-fairness) - Each dice game’s disclosed odds and the trial evidencing them | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/casino/dice/plays`](#post--api-v1-casino-dice-plays) - Stake WLD on one roll of the die | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/casino/games/terms`](#get--api-v1-casino-games-terms) - Every game’s odds, payout and remaining exposure for today | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/casino/history`](#get--api-v1-casino-history) - Read the current member's recent casino plays | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/casino/jackpot`](#get--api-v1-casino-jackpot) - Read current casino house reserve and jackpot pool | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [PUT `/api/v1/casino/self-limit`](#put--api-v1-casino-self-limit) - Set the daily caps and the lock the member holds themselves to | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/casino/self-limit`](#get--api-v1-casino-self-limit) - Read the daily limits chosen by the current member | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/casino/theme/plays`](#post--api-v1-casino-theme-plays) - Stake WLD on one play of a theme catalog game | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/early-game/claims`](#post--api-v1-early-game-claims) - Claim today’s event, once | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/early-game/first-day`](#get--api-v1-early-game-first-day) - The seven steps of 16.1’s first day, counted from what happened | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/early-game/today`](#get--api-v1-early-game-today) - The event this member is dealt today, and whether it is still theirs | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/engagement`](#get--api-v1-engagement) - Today’s goals, this week’s goals, the next unlock and the preference | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/engagement/early-game`](#get--api-v1-engagement-early-game) - The early-game weekly goals and collection books | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/engagement/npcs/{code}/orders`](#post--api-v1-engagement-npcs--code--orders) - Take an order from an NPC | `🔓 공개 (게스트 허용)`
- [PUT `/api/v1/engagement/preferences`](#put--api-v1-engagement-preferences) - Set whether the member hears about their goals | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/progression`](#get--api-v1-progression) - The caller’s growth stage and what unlocks the next one | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/progression/credit`](#get--api-v1-progression-credit) - The caller’s credit grade, what each grade buys, and their loans | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/progression/early-game`](#get--api-v1-progression-early-game) - The early-game unlock ladder and what the caller has reached | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/progression/refreshes`](#post--api-v1-progression-refreshes) - Recompute the caller’s growth stage from their progress | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/work`](#get--api-v1-work) - Caps, what has been paid against them, and open assignments | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/work/active-job`](#post--api-v1-work-active-job) - Switch active job among 8 specialization careers | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/work/assignments`](#get--api-v1-work-assignments) - The caller’s recent assignments | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/work/assignments`](#post--api-v1-work-assignments) - Take a task | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/work/assignments/{id}/completions`](#post--api-v1-work-assignments--id--completions) - Submit a taken task as done | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/work/assignments/{id}/verify`](#post--api-v1-work-assignments--id--verify) - Verify a submitted task and pay it, within the caps | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/work/profile`](#get--api-v1-work-profile) - Current active job and all job masteries | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/work/receipts`](#get--api-v1-work-receipts) - What the work paid, and the ledger transaction it paid through | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/work/tasks`](#get--api-v1-work-tasks) - Every task on offer, with this member’s standing against each | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/work/tasks/{id}/complete`](#post--api-v1-work-tasks--id--complete) - Directly complete a career task with EXP and instant WLD faucet payout | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`

---

## 🛠️ 엔드포인트 상세 규격

<a id="get--api-v1-casino-clock"></a>
### GET `/api/v1/casino/clock`

**설명:** Read the authoritative accelerated server day and week

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_clock`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/casino/clock" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-casino-coin-fairness"></a>
### GET `/api/v1/casino/coin/fairness`

**설명:** The disclosed win probability and the trial that evidences it

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_fairness`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/casino/coin/fairness" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-casino-coin-plays"></a>
### POST `/api/v1/casino/coin/plays`

**설명:** Stake WLD on one toss of the coin

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_play`
#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**
  - `choice` (`string`) **(필수)**
  - `stake` (`number`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/casino/coin/plays" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-casino-coin-terms"></a>
### GET `/api/v1/casino/coin/terms`

**설명:** The odds, the stake limits, and what today has already used

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_terms`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/casino/coin/terms" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-casino-dice-fairness"></a>
### GET `/api/v1/casino/dice/fairness`

**설명:** Each dice game’s disclosed odds and the trial evidencing them

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_diceFairness`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/casino/dice/fairness" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-casino-dice-plays"></a>
### POST `/api/v1/casino/dice/plays`

**설명:** Stake WLD on one roll of the die

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_playDice`
#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**
  - `game` (`string`) **(필수)**
  - `choice` (`string`) **(필수)**
  - `stake` (`number`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/casino/dice/plays" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-casino-games-terms"></a>
### GET `/api/v1/casino/games/terms`

**설명:** Every game’s odds, payout and remaining exposure for today

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_gameTerms`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/casino/games/terms" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-casino-history"></a>
### GET `/api/v1/casino/history`

**설명:** Read the current member's recent casino plays

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_history`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/casino/history" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-casino-jackpot"></a>
### GET `/api/v1/casino/jackpot`

**설명:** Read current casino house reserve and jackpot pool

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_jackpot`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/casino/jackpot" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-casino-self-limit"></a>
### PUT `/api/v1/casino/self-limit`

**설명:** Set the daily caps and the lock the member holds themselves to

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_setSelfLimit`
#### 📦 요청 본문 (Request Body)

  - `dailyBetLimit` (`number`) **(필수)**
  - `dailyLossLimit` (`number`) **(필수)**
  - `lockedUntil` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/casino/self-limit" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-casino-self-limit"></a>
### GET `/api/v1/casino/self-limit`

**설명:** Read the daily limits chosen by the current member

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_selfLimit`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/casino/self-limit" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-casino-theme-plays"></a>
### POST `/api/v1/casino/theme/plays`

**설명:** Stake WLD on one play of a theme catalog game

- **분류 도메인 (Tag):** `casino`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `CasinoController_playTheme`
#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**
  - `game` (`string`) **(필수)**
  - `choice` (`string`) **(필수)**
  - `stake` (`number`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/casino/theme/plays" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-early-game-claims"></a>
### POST `/api/v1/early-game/claims`

**설명:** Claim today’s event, once

- **분류 도메인 (Tag):** `early-game`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `EarlyGameController_claim`
#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**
  - `eventDate` (`string`) **(필수)** - The Asia/Seoul day the screen is showing

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/early-game/claims" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-early-game-first-day"></a>
### GET `/api/v1/early-game/first-day`

**설명:** The seven steps of 16.1’s first day, counted from what happened

- **분류 도메인 (Tag):** `early-game`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `EarlyGameController_firstDay`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/early-game/first-day" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-early-game-today"></a>
### GET `/api/v1/early-game/today`

**설명:** The event this member is dealt today, and whether it is still theirs

- **분류 도메인 (Tag):** `early-game`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `EarlyGameController_today`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/early-game/today" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-engagement"></a>
### GET `/api/v1/engagement`

**설명:** Today’s goals, this week’s goals, the next unlock and the preference

- **분류 도메인 (Tag):** `engagement`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `EngagementController_dashboard`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/engagement" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-engagement-early-game"></a>
### GET `/api/v1/engagement/early-game`

**설명:** The early-game weekly goals and collection books

- **분류 도메인 (Tag):** `engagement`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `EngagementController_earlyGame`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/engagement/early-game" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-engagement-npcs--code--orders"></a>
### POST `/api/v1/engagement/npcs/{code}/orders`

**설명:** Take an order from an NPC

- **분류 도메인 (Tag):** `engagement`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `EngagementController_recordNpcOrder`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `code` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/engagement/npcs/{code}/orders" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="put--api-v1-engagement-preferences"></a>
### PUT `/api/v1/engagement/preferences`

**설명:** Set whether the member hears about their goals

- **분류 도메인 (Tag):** `engagement`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `EngagementController_setPreferences`
#### 📦 요청 본문 (Request Body)

  - `notificationsEnabled` (`boolean`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/engagement/preferences" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-progression"></a>
### GET `/api/v1/progression`

**설명:** The caller’s growth stage and what unlocks the next one

- **분류 도메인 (Tag):** `progression`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProgressionController_status`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/progression" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-progression-credit"></a>
### GET `/api/v1/progression/credit`

**설명:** The caller’s credit grade, what each grade buys, and their loans

- **분류 도메인 (Tag):** `progression`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProgressionController_credit`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/progression/credit" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-progression-early-game"></a>
### GET `/api/v1/progression/early-game`

**설명:** The early-game unlock ladder and what the caller has reached

- **분류 도메인 (Tag):** `progression`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProgressionController_earlyGameUnlocks`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/progression/early-game" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-progression-refreshes"></a>
### POST `/api/v1/progression/refreshes`

**설명:** Recompute the caller’s growth stage from their progress

- **분류 도메인 (Tag):** `progression`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProgressionController_refresh`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/progression/refreshes" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-work"></a>
### GET `/api/v1/work`

**설명:** Caps, what has been paid against them, and open assignments

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `WorkController_dashboard`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/work" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-work-active-job"></a>
### POST `/api/v1/work/active-job`

**설명:** Switch active job among 8 specialization careers

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `WorkController_switchJob`
#### 📦 요청 본문 (Request Body)

  - `jobType` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/work/active-job" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-work-assignments"></a>
### GET `/api/v1/work/assignments`

**설명:** The caller’s recent assignments

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `WorkController_assignments`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/work/assignments" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-work-assignments"></a>
### POST `/api/v1/work/assignments`

**설명:** Take a task

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `WorkController_assign`
#### 📦 요청 본문 (Request Body)

  - `taskId` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/work/assignments" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-work-assignments--id--completions"></a>
### POST `/api/v1/work/assignments/{id}/completions`

**설명:** Submit a taken task as done

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `WorkController_submit`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**
  - `evidence` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/work/assignments/{id}/completions" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-work-assignments--id--verify"></a>
### POST `/api/v1/work/assignments/{id}/verify`

**설명:** Verify a submitted task and pay it, within the caps

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `WorkController_verify`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**
  - `evidence` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/work/assignments/{id}/verify" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-work-profile"></a>
### GET `/api/v1/work/profile`

**설명:** Current active job and all job masteries

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `WorkController_profile`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/work/profile" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-work-receipts"></a>
### GET `/api/v1/work/receipts`

**설명:** What the work paid, and the ledger transaction it paid through

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `WorkController_receipts`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/work/receipts" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-work-tasks"></a>
### GET `/api/v1/work/tasks`

**설명:** Every task on offer, with this member’s standing against each

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `WorkController_tasks`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/work/tasks" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-work-tasks--id--complete"></a>
### POST `/api/v1/work/tasks/{id}/complete`

**설명:** Directly complete a career task with EXP and instant WLD faucet payout

- **분류 도메인 (Tag):** `work`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `WorkController_completeTask`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/work/tasks/{id}/complete" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

