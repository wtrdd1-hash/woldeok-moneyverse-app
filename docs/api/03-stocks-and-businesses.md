# 가상 주식 & 사업체 공급망 API (Stocks & Businesses)

> 가상 주식 시장 시세/차트, 매수/매도 호가 주문, 포트폴리오 분석, 증시 뉴스 피드, 가상 사업체 창업/인수, 일일 정산 및 원자재 공급망 조달(2% 소각) 엔드포인트

## 📋 목차 (Table of Contents)

- [GET `/api/v1/business-equity`](#get--api-v1-business-equity) - The own capital the caller can put behind a purchase | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/business-types`](#get--api-v1-business-types) - Business types available to buy, excluding types already owned | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/business-types/{id}/purchases`](#post--api-v1-business-types--id--purchases) - Buy a business of this type | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/businesses`](#get--api-v1-businesses) - Businesses the caller owns | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/businesses/{id}/boost`](#post--api-v1-businesses--id--boost) - Equip a boost item from inventory to business | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/businesses/{id}/procure`](#post--api-v1-businesses--id--procure) - Procure raw materials for business with WLD payment | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/businesses/{id}/settle-v2`](#post--api-v1-businesses--id--settle-v2) - Settle a day of revenue with active boosts and double-entry ledger sink | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/businesses/{id}/settlements`](#post--api-v1-businesses--id--settlements) - Settle a day of revenue | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/businesses/{id}/storage/upgrade`](#post--api-v1-businesses--id--storage-upgrade) - Upgrade business storage capacity | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/businesses/{id}/supply-chain`](#get--api-v1-businesses--id--supply-chain) - Get supply chain inventory, storage capacity and demand factors | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/businesses/activate-license`](#post--api-v1-businesses-activate-license) - Activate a business using a purchased license item from inventory | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/businesses/catalog`](#get--api-v1-businesses-catalog) - App API alias: business types available to buy, excluding owned types | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/businesses/catalog/{id}/purchases`](#post--api-v1-businesses-catalog--id--purchases) - App API alias: buy a business from the catalogue | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/businesses/equity`](#get--api-v1-businesses-equity) - App API alias: own capital available for a business purchase | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/businesses/my-v2`](#get--api-v1-businesses-my-v2) - Enhanced businesses the caller owns with boosts and settlement status | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/newspaper/lore`](#get--api-v1-newspaper-lore) - 주간 금융 개념 배움터 아티클 목록 조회 | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/newspaper/poll`](#get--api-v1-newspaper-poll) - 주간 독자 여론조사 현황 조회 | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/newspaper/poll/vote`](#post--api-v1-newspaper-poll-vote) - 주간 독자 여론조사 투표 참여 | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/newspaper/pulse`](#get--api-v1-newspaper-pulse) - 실시간 월드 펄스 및 시장 심리 조회 | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/stocks`](#get--api-v1-stocks) - Listed stocks and their current prices | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/stocks/{id}/candles`](#get--api-v1-stocks--id--candles) - Open/high/low/close for one stock at a given interval | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/stocks/{id}/orders`](#post--api-v1-stocks--id--orders) - Buy or sell a stock | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/stocks/{id}/prices`](#get--api-v1-stocks--id--prices) - Recorded price history for one stock | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/stocks/{id}/watchlist`](#post--api-v1-stocks--id--watchlist) - Add or remove a stock from the caller watchlist | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/stocks/alerts`](#get--api-v1-stocks-alerts) - Conditional virtual-stock alerts belonging to the caller | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/stocks/alerts`](#post--api-v1-stocks-alerts) - Create a server-evaluated virtual-stock alert | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [DELETE `/api/v1/stocks/alerts/{id}`](#delete--api-v1-stocks-alerts--id-) - Delete one virtual-stock alert belonging to the caller | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/stocks/alerts/events`](#get--api-v1-stocks-alerts-events) - Recent virtual-stock alert events belonging to the caller | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/stocks/halt-receipts`](#get--api-v1-stocks-halt-receipts) - Stock halt cost-basis settlement receipts for caller | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/stocks/history`](#get--api-v1-stocks-history) - Trades made by the caller | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/stocks/market-events`](#get--api-v1-stocks-market-events) - Market events currently in effect | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/stocks/portfolio`](#get--api-v1-stocks-portfolio) - Holdings of the caller | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/stocks/sparklines`](#get--api-v1-stocks-sparklines) - Recent prices for every listed stock | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/stocks/watchlist`](#get--api-v1-stocks-watchlist) - Stocks watched by the caller | `🔒 로그인 필수` `📜 약관동의 필수`

---

## 🛠️ 엔드포인트 상세 규격

<a id="get--api-v1-business-equity"></a>
### GET `/api/v1/business-equity`

**설명:** The own capital the caller can put behind a purchase

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `BusinessController_equity`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/business-equity" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-business-types"></a>
### GET `/api/v1/business-types`

**설명:** Business types available to buy, excluding types already owned

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `BusinessController_catalog`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/business-types" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-business-types--id--purchases"></a>
### POST `/api/v1/business-types/{id}/purchases`

**설명:** Buy a business of this type

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_purchase`

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
curl -X POST "https://easy-scraping.com/api/v1/business-types/{id}/purchases" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-businesses"></a>
### GET `/api/v1/businesses`

**설명:** Businesses the caller owns

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_mine`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/businesses" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-businesses--id--boost"></a>
### POST `/api/v1/businesses/{id}/boost`

**설명:** Equip a boost item from inventory to business

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_applyBoost`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `boostCode` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/businesses/{id}/boost" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-businesses--id--procure"></a>
### POST `/api/v1/businesses/{id}/procure`

**설명:** Procure raw materials for business with WLD payment

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_procureMaterials`

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
curl -X POST "https://easy-scraping.com/api/v1/businesses/{id}/procure" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-businesses--id--settle-v2"></a>
### POST `/api/v1/businesses/{id}/settle-v2`

**설명:** Settle a day of revenue with active boosts and double-entry ledger sink

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_settleV2`

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
curl -X POST "https://easy-scraping.com/api/v1/businesses/{id}/settle-v2" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-businesses--id--settlements"></a>
### POST `/api/v1/businesses/{id}/settlements`

**설명:** Settle a day of revenue

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_settle`

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
curl -X POST "https://easy-scraping.com/api/v1/businesses/{id}/settlements" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-businesses--id--storage-upgrade"></a>
### POST `/api/v1/businesses/{id}/storage/upgrade`

**설명:** Upgrade business storage capacity

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_upgradeStorage`

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
curl -X POST "https://easy-scraping.com/api/v1/businesses/{id}/storage/upgrade" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-businesses--id--supply-chain"></a>
### GET `/api/v1/businesses/{id}/supply-chain`

**설명:** Get supply chain inventory, storage capacity and demand factors

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_supplyChainOverview`

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
curl -X GET "https://easy-scraping.com/api/v1/businesses/{id}/supply-chain" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-businesses-activate-license"></a>
### POST `/api/v1/businesses/activate-license`

**설명:** Activate a business using a purchased license item from inventory

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_activateLicense`
#### 📦 요청 본문 (Request Body)

  - `catalogCode` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/businesses/activate-license" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-businesses-catalog"></a>
### GET `/api/v1/businesses/catalog`

**설명:** App API alias: business types available to buy, excluding owned types

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `BusinessController_catalogForApp`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/businesses/catalog" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-businesses-catalog--id--purchases"></a>
### POST `/api/v1/businesses/catalog/{id}/purchases`

**설명:** App API alias: buy a business from the catalogue

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_purchaseForApp`

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
curl -X POST "https://easy-scraping.com/api/v1/businesses/catalog/{id}/purchases" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-businesses-equity"></a>
### GET `/api/v1/businesses/equity`

**설명:** App API alias: own capital available for a business purchase

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `BusinessController_equityForApp`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/businesses/equity" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-businesses-my-v2"></a>
### GET `/api/v1/businesses/my-v2`

**설명:** Enhanced businesses the caller owns with boosts and settlement status

- **분류 도메인 (Tag):** `businesses`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `BusinessController_mineV2`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/businesses/my-v2" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-newspaper-lore"></a>
### GET `/api/v1/newspaper/lore`

**설명:** 주간 금융 개념 배움터 아티클 목록 조회

- **분류 도메인 (Tag):** `newspaper`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `NewspaperController_getFinancialLore`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/newspaper/lore" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-newspaper-poll"></a>
### GET `/api/v1/newspaper/poll`

**설명:** 주간 독자 여론조사 현황 조회

- **분류 도메인 (Tag):** `newspaper`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `NewspaperController_getWeeklyPoll`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/newspaper/poll" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-newspaper-poll-vote"></a>
### POST `/api/v1/newspaper/poll/vote`

**설명:** 주간 독자 여론조사 투표 참여

- **분류 도메인 (Tag):** `newspaper`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `NewspaperController_voteWeeklyPoll`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/newspaper/poll/vote" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-newspaper-pulse"></a>
### GET `/api/v1/newspaper/pulse`

**설명:** 실시간 월드 펄스 및 시장 심리 조회

- **분류 도메인 (Tag):** `newspaper`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `NewspaperController_getMarketPulse`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/newspaper/pulse" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks"></a>
### GET `/api/v1/stocks`

**설명:** Listed stocks and their current prices

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_list`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks--id--candles"></a>
### GET `/api/v1/stocks/{id}/candles`

**설명:** Open/high/low/close for one stock at a given interval

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_candles`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |
| `query` | `interval` | `string` | **필수** | - |
| `query` | `limit` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks/{id}/candles" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-stocks--id--orders"></a>
### POST `/api/v1/stocks/{id}/orders`

**설명:** Buy or sell a stock

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `StockController_order`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `side` (`string`) **(필수)**
  - `quantity` (`number`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/stocks/{id}/orders" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-stocks--id--prices"></a>
### GET `/api/v1/stocks/{id}/prices`

**설명:** Recorded price history for one stock

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_prices`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |
| `query` | `limit` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks/{id}/prices" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-stocks--id--watchlist"></a>
### POST `/api/v1/stocks/{id}/watchlist`

**설명:** Add or remove a stock from the caller watchlist

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `StockController_setWatchlist`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `watching` (`boolean`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/stocks/{id}/watchlist" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-stocks-alerts"></a>
### GET `/api/v1/stocks/alerts`

**설명:** Conditional virtual-stock alerts belonging to the caller

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockAlertController_list`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks/alerts" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-stocks-alerts"></a>
### POST `/api/v1/stocks/alerts`

**설명:** Create a server-evaluated virtual-stock alert

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `StockAlertController_create`
#### 📦 요청 본문 (Request Body)

  - `stockId` (`string`) **(필수)**
  - `conditionKind` (`string`) **(필수)**
  - `thresholdAmount` (`string`) *(선택)* - Integer WLD threshold for price conditions
  - `thresholdBps` (`number`) *(선택)*
  - `cooldownSeconds` (`number`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/stocks/alerts" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="delete--api-v1-stocks-alerts--id-"></a>
### DELETE `/api/v1/stocks/alerts/{id}`

**설명:** Delete one virtual-stock alert belonging to the caller

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `StockAlertController_remove`

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
curl -X DELETE "https://easy-scraping.com/api/v1/stocks/alerts/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks-alerts-events"></a>
### GET `/api/v1/stocks/alerts/events`

**설명:** Recent virtual-stock alert events belonging to the caller

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockAlertController_events`

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
curl -X GET "https://easy-scraping.com/api/v1/stocks/alerts/events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks-halt-receipts"></a>
### GET `/api/v1/stocks/halt-receipts`

**설명:** Stock halt cost-basis settlement receipts for caller

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_haltReceipts`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks/halt-receipts" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks-history"></a>
### GET `/api/v1/stocks/history`

**설명:** Trades made by the caller

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_history`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks/history" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks-market-events"></a>
### GET `/api/v1/stocks/market-events`

**설명:** Market events currently in effect

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_marketEvents`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks/market-events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks-portfolio"></a>
### GET `/api/v1/stocks/portfolio`

**설명:** Holdings of the caller

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_portfolio`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks/portfolio" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks-sparklines"></a>
### GET `/api/v1/stocks/sparklines`

**설명:** Recent prices for every listed stock

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_sparklines`

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
curl -X GET "https://easy-scraping.com/api/v1/stocks/sparklines" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-stocks-watchlist"></a>
### GET `/api/v1/stocks/watchlist`

**설명:** Stocks watched by the caller

- **분류 도메인 (Tag):** `stocks`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `StockController_watchlist`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/stocks/watchlist" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

