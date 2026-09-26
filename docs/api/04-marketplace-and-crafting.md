# 유저 거래소 & 에스크로 경매·감정·제작 API (Marketplace & Crafting)

> 고정가 장터 출품/구매, 실시간 잉글리시 경매(안티스나이핑 및 에스크로 즉시환불), P2P 1:1 직거래(3단계 원자적 스왑), 디지털 공인 감정소(수수료 소각 및 배지 발급) 및 제작대 레시피 엔드포인트

## 📋 목차 (Table of Contents)

- [POST `/api/v1/crafting/execute`](#post--api-v1-crafting-execute) - Execute crafting recipe: consume materials and WLD fee to mint crafted item | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/crafting/recipes`](#get--api-v1-crafting-recipes) - List all official crafting recipes with required materials and fees | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/marketplace/appraisals`](#get--api-v1-marketplace-appraisals) - List my issued provenance appraisal certificates | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/appraisals`](#post--api-v1-marketplace-appraisals) - Request system provenance appraisal for collectible with fee burn | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/marketplace/auctions`](#get--api-v1-marketplace-auctions) - List active live English auctions | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/auctions`](#post--api-v1-marketplace-auctions) - Create a new live English auction | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/auctions/{id}/bid`](#post--api-v1-marketplace-auctions--id--bid) - Bid on a live English auction with escrow and anti-sniping extension | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/marketplace/listings`](#get--api-v1-marketplace-listings) - List active player marketplace listings with filter and search | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/listings`](#post--api-v1-marketplace-listings) - List an item for sale in player marketplace with escrow lock | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/listings/{id}/buy`](#post--api-v1-marketplace-listings--id--buy) - Buy a marketplace listing item with 1% burn fee and 99% seller settlement | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/listings/{id}/cancel`](#post--api-v1-marketplace-listings--id--cancel) - Cancel active marketplace listing and recover item to inventory | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/marketplace/my-listings`](#get--api-v1-marketplace-my-listings) - List current user marketplace listings and trade history | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/marketplace/trades`](#get--api-v1-marketplace-trades) - List my P2P direct trades | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/trades`](#post--api-v1-marketplace-trades) - Propose a new P2P 1:1 direct trade | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/trades/{id}/accept`](#post--api-v1-marketplace-trades--id--accept) - Accept a P2P 1:1 direct trade proposal (first step) | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/trades/{id}/cancel`](#post--api-v1-marketplace-trades--id--cancel) - Cancel a P2P 1:1 direct trade | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/marketplace/trades/{id}/confirm`](#post--api-v1-marketplace-trades--id--confirm) - Sign-off and execute dual atomic swap for P2P 1:1 trade | `🔓 공개 (게스트 허용)`

---

## 🛠️ 엔드포인트 상세 규격

<a id="post--api-v1-crafting-execute"></a>
### POST `/api/v1/crafting/execute`

**설명:** Execute crafting recipe: consume materials and WLD fee to mint crafted item

- **분류 도메인 (Tag):** `crafting`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `CraftingController_executeCrafting`
#### 📦 요청 본문 (Request Body)

  - `recipeId` (`string`) **(필수)** - 제작 레시피 ID
  - `idempotencyKey` (`string`) **(필수)** - 멱등성 키 (UUID)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/crafting/execute" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-crafting-recipes"></a>
### GET `/api/v1/crafting/recipes`

**설명:** List all official crafting recipes with required materials and fees

- **분류 도메인 (Tag):** `crafting`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `CraftingController_listRecipes`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/crafting/recipes" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-marketplace-appraisals"></a>
### GET `/api/v1/marketplace/appraisals`

**설명:** List my issued provenance appraisal certificates

💡 **감정 수수료 소각 룰**: 감정 시 `max(250 WLD, ceil(0.25%))` WLD가 `SINK_APPRAISAL_FEE`로 영구 소각되고 디지털 공인 인증서가 영구 발급됩니다.

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_listAppraisals`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/marketplace/appraisals" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-marketplace-appraisals"></a>
### POST `/api/v1/marketplace/appraisals`

**설명:** Request system provenance appraisal for collectible with fee burn

💡 **감정 수수료 소각 룰**: 감정 시 `max(250 WLD, ceil(0.25%))` WLD가 `SINK_APPRAISAL_FEE`로 영구 소각되고 디지털 공인 인증서가 영구 발급됩니다.

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_requestAppraisal`
#### 📦 요청 본문 (Request Body)

  - `itemId` (`string`) **(필수)** - 소장품 아이템 ID
  - `itemName` (`string`) **(필수)** - 소장품 아이템명
  - `rarity` (`string`) **(필수)** - 희귀도

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/marketplace/appraisals" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-marketplace-auctions"></a>
### GET `/api/v1/marketplace/auctions`

**설명:** List active live English auctions

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_listAuctions`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/marketplace/auctions" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-marketplace-auctions"></a>
### POST `/api/v1/marketplace/auctions`

**설명:** Create a new live English auction

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_createAuction`
#### 📦 요청 본문 (Request Body)

  - `itemCode` (`string`) **(필수)** - 아이템 코드
  - `itemName` (`string`) **(필수)** - 아이템 이름
  - `category` (`string`) **(필수)** - 카테고리
  - `rarity` (`string`) **(필수)** - 희귀도
  - `startPriceWld` (`string`) **(필수)** - 시작 호가 (WLD)
  - `buyNowPriceWld` (`string`) *(선택)* - 즉시 낙찰가 (WLD)
  - `durationMinutes` (`number`) *(선택)* - 경매 진행 시간(분)
  - `description` (`string`) *(선택)* - 설명

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/marketplace/auctions" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-marketplace-auctions--id--bid"></a>
### POST `/api/v1/marketplace/auctions/{id}/bid`

**설명:** Bid on a live English auction with escrow and anti-sniping extension

💡 **에스크로 & 안티스나이핑 룰**: 신규 입찰 시 기존 최고 입찰자에게 100% 즉시 에스크로 환불(`ESCROW_REFUND`)되며, 마감 30초 이내 입찰 시 60초 자동 연장됩니다. 낙찰 시 2% `SINK_AUCTION_FEE` 영구 소각.

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_bidAuction`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `bidAmountWld` (`string`) **(필수)** - 입찰 호가 (WLD)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/marketplace/auctions/{id}/bid" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-marketplace-listings"></a>
### GET `/api/v1/marketplace/listings`

**설명:** List active player marketplace listings with filter and search

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_listListings`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `category` | `string` | 선택 | 카테고리 필터 |
| `query` | `search` | `string` | 선택 | 검색 키워드 |
| `query` | `limit` | `number` | 선택 | 결과 제한 수 |
| `query` | `offset` | `number` | 선택 | 오프셋 |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/marketplace/listings" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-marketplace-listings"></a>
### POST `/api/v1/marketplace/listings`

**설명:** List an item for sale in player marketplace with escrow lock

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_createListing`
#### 📦 요청 본문 (Request Body)

  - `itemCode` (`string`) **(필수)** - 판매할 아이템 코드
  - `quantity` (`number`) **(필수)** - 판매 수량
  - `price` (`string`) **(필수)** - 총 판매 가격 (WLD)
  - `description` (`string`) *(선택)* - 판매 물품 설명
  - `idempotencyKey` (`string`) **(필수)** - 멱등성 키 (UUID)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/marketplace/listings" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-marketplace-listings--id--buy"></a>
### POST `/api/v1/marketplace/listings/{id}/buy`

**설명:** Buy a marketplace listing item with 1% burn fee and 99% seller settlement

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_buyListing`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `idempotencyKey` (`string`) **(필수)** - 멱등성 키 (UUID)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/marketplace/listings/{id}/buy" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-marketplace-listings--id--cancel"></a>
### POST `/api/v1/marketplace/listings/{id}/cancel`

**설명:** Cancel active marketplace listing and recover item to inventory

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_cancelListing`

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
curl -X POST "https://easy-scraping.com/api/v1/marketplace/listings/{id}/cancel" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-marketplace-my-listings"></a>
### GET `/api/v1/marketplace/my-listings`

**설명:** List current user marketplace listings and trade history

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_listMyListings`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/marketplace/my-listings" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-marketplace-trades"></a>
### GET `/api/v1/marketplace/trades`

**설명:** List my P2P direct trades

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_listTrades`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/marketplace/trades" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-marketplace-trades"></a>
### POST `/api/v1/marketplace/trades`

**설명:** Propose a new P2P 1:1 direct trade

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_createTrade`
#### 📦 요청 본문 (Request Body)

  - `recipientName` (`string`) **(필수)** - 상대방 닉네임
  - `offeredItems` (`array`) *(선택)* - 제공할 물품 목록
  - `offeredWld` (`string`) *(선택)* - 제공할 WLD
  - `requestedItems` (`array`) *(선택)* - 요청할 물품 목록
  - `requestedWld` (`string`) *(선택)* - 요청할 WLD

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/marketplace/trades" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-marketplace-trades--id--accept"></a>
### POST `/api/v1/marketplace/trades/{id}/accept`

**설명:** Accept a P2P 1:1 direct trade proposal (first step)

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_acceptTrade`

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
curl -X POST "https://easy-scraping.com/api/v1/marketplace/trades/{id}/accept" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-marketplace-trades--id--cancel"></a>
### POST `/api/v1/marketplace/trades/{id}/cancel`

**설명:** Cancel a P2P 1:1 direct trade

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_cancelTrade`

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
curl -X POST "https://easy-scraping.com/api/v1/marketplace/trades/{id}/cancel" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-marketplace-trades--id--confirm"></a>
### POST `/api/v1/marketplace/trades/{id}/confirm`

**설명:** Sign-off and execute dual atomic swap for P2P 1:1 trade

- **분류 도메인 (Tag):** `marketplace`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MarketplaceController_confirmTrade`

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
curl -X POST "https://easy-scraping.com/api/v1/marketplace/trades/{id}/confirm" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

