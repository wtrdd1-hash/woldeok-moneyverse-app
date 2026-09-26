# 개인 공간·세무 구청 & 클럽·시즌 API (Spaces, Clubs & Seasons)

> 가상 부동산 분양/인테리어 레이아웃, 세무 구청 일일 부동산세 납부(SINK_PROPERTY_TAX 100% 소각), 체납 공매 모니터링, 공공 도시 프로젝트 크라우드펀딩, 클럽하우스 12x12 공유 캔버스, 유저 컬렉션 큐레이션 사다리, 시즌 랭킹 및 명예의 전당 보상 분배 엔드포인트

## 📋 목차 (Table of Contents)

- [GET `/api/v1/clubs`](#get--api-v1-clubs) - 클럽 목록 탐색 및 검색 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/clubs`](#post--api-v1-clubs) - 신규 클럽 창설 (10,000 WLD 소각) | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/clubs/{id}`](#get--api-v1-clubs--id-) - 클럽 상세 정보 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/clubs/{id}/canvas`](#get--api-v1-clubs--id--canvas) - 소속 클럽하우스 12x12 가구 배치 그리드 및 장식 점수 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [PUT `/api/v1/clubs/{id}/canvas`](#put--api-v1-clubs--id--canvas) - 클럽하우스 12x12 공유 캔버스 레이아웃 및 장식 점수 서버 영구 저장 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/clubs/{id}/feed`](#get--api-v1-clubs--id--feed) - 클럽 피드 글 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/clubs/{id}/feed`](#post--api-v1-clubs--id--feed) - 클럽 피드 또는 공지사항 작성 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/clubs/{id}/join`](#post--api-v1-clubs--id--join) - 클럽 공개 가입 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/clubs/{id}/leave`](#post--api-v1-clubs--id--leave) - 클럽 탈퇴 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/clubs/{id}/members`](#get--api-v1-clubs--id--members) - 클럽 회원 명부 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [PATCH `/api/v1/clubs/{id}/members/{userId}/role`](#patch--api-v1-clubs--id--members--userid--role) - 클럽 회원 역할 변경 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/clubs/{id}/projects`](#get--api-v1-clubs--id--projects) - 협동 프로젝트 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/clubs/{id}/projects/{projectId}/contributions`](#post--api-v1-clubs--id--projects--projectid--contributions) - 협동 프로젝트 WLD 펀딩 기여 (영구 소각) | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/collections`](#get--api-v1-collections) - 내 수집품 조각 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [PUT `/api/v1/collections/{id}`](#put--api-v1-collections--id-) - 수집품 유저 메모 및 즐겨찾기 수정 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/collections/curation/advance`](#post--api-v1-collections-curation-advance) - 소유권 큐레이션 사다리 단계 진척 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/collections/curation/status`](#get--api-v1-collections-curation-status) - D1~D7 소유권 큐레이션 사다리 상태 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/seasons/claim-rewards`](#post--api-v1-seasons-claim-rewards) - 시즌 보상 청구 및 수령 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/seasons/current`](#get--api-v1-seasons-current) - 현재 시즌 정보 및 내 티어/랭킹 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/seasons/events`](#get--api-v1-seasons-events) - Active season events | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/seasons/events/{id}/consumptions`](#post--api-v1-seasons-events--id--consumptions) - Spend on a season event | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/seasons/events/{id}/leaderboard`](#get--api-v1-seasons-events--id--leaderboard) - Leaderboard for one event | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/seasons/hall-of-fame`](#get--api-v1-seasons-hall-of-fame) - 역대 시즌 명예의 전당 헌액자 목록 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/seasons/settle`](#post--api-v1-seasons-settle) - 시즌 종료 정산 엔진 (명예의 전당 및 6대 티어 보상 분배) | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/spaces`](#get--api-v1-spaces) - 사용자가 분양받아 보유 중인 나만의 개인 공간 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/spaces/{id}`](#get--api-v1-spaces--id-) - 지정한 개인 공간의 상세 스펙 및 소유권 정보 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [PUT `/api/v1/spaces/{id}/layout`](#put--api-v1-spaces--id--layout) - 개인 공간 8x8 인터랙티브 가구 배치 및 인테리어 레이아웃 저장 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/spaces/{id}/tax/pay`](#post--api-v1-spaces--id--tax-pay) - 개인 공간 일일 부동산세 자진 납부 (100% 영구 소각 SINK_PROPERTY_TAX) | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/spaces/{id}/tax/status`](#get--api-v1-spaces--id--tax-status) - 공간별 일일 보유세율, 완납 기한, 체납 일수 및 공매 상태 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/spaces/city/projects`](#get--api-v1-spaces-city-projects) - 머니버스 시민 공동 출자 공공 인프라 크라우드펀딩 프로젝트 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/spaces/city/projects/{id}/contributions`](#post--api-v1-spaces-city-projects--id--contributions) - 공공 도시 인프라 크라우드펀딩 WLD 출자 기여 (100% 소각 SINK_PROJECT_DONATION) | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/spaces/purchase`](#post--api-v1-spaces-purchase) - 신규 개인 공간 분양 신청 (대금 100% 영구 소각 SINK_HOUSING_PURCHASE) | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/spaces/tax/delinquencies`](#get--api-v1-spaces-tax-delinquencies) - 부동산세 7일 이상 체납으로 법정 유예 경과한 시청 강제 공매 매물 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수`

---

## 🛠️ 엔드포인트 상세 규격

<a id="get--api-v1-clubs"></a>
### GET `/api/v1/clubs`

**설명:** 클럽 목록 탐색 및 검색

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_listClubs`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `search` | `string` | **필수** | - |
| `query` | `limit` | `string` | **필수** | - |
| `query` | `offset` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/clubs" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-clubs"></a>
### POST `/api/v1/clubs`

**설명:** 신규 클럽 창설 (10,000 WLD 소각)

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_createClub`
#### 📦 요청 본문 (Request Body)

  - `tag` (`string`) **(필수)** - 클럽 태그 (2-8자 대문자 영문/숫자)
  - `name` (`string`) **(필수)** - 클럽 이름 (2-30자)
  - `description` (`string`) *(선택)* - 클럽 소개글 (최대 500자)
  - `charter` (`string`) *(선택)* - 클럽 헌장 (최대 2000자)
  - `joinMode` (`string`) *(선택)* - 가입 방식 (public, invite, request)
  - `idempotencyKey` (`string`) **(필수)** - 클라이언트 멱등성 키

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/clubs" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-clubs--id-"></a>
### GET `/api/v1/clubs/{id}`

**설명:** 클럽 상세 정보 조회

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_getClubById`

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
curl -X GET "https://easy-scraping.com/api/v1/clubs/{id}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-clubs--id--canvas"></a>
### GET `/api/v1/clubs/{id}/canvas`

**설명:** 소속 클럽하우스 12x12 가구 배치 그리드 및 장식 점수 조회

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_getClubCanvas`

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
curl -X GET "https://easy-scraping.com/api/v1/clubs/{id}/canvas" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-clubs--id--canvas"></a>
### PUT `/api/v1/clubs/{id}/canvas`

**설명:** 클럽하우스 12x12 공유 캔버스 레이아웃 및 장식 점수 서버 영구 저장

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_updateClubCanvas`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `grid` (`array`) **(필수)** - 12x12 가구 배치 그리드 배열
  - `totalScore` (`number`) **(필수)** - 장식 점수

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/clubs/{id}/canvas" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-clubs--id--feed"></a>
### GET `/api/v1/clubs/{id}/feed`

**설명:** 클럽 피드 글 목록 조회

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_listClubFeed`

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
curl -X GET "https://easy-scraping.com/api/v1/clubs/{id}/feed" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-clubs--id--feed"></a>
### POST `/api/v1/clubs/{id}/feed`

**설명:** 클럽 피드 또는 공지사항 작성

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_createClubFeedPost`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `title` (`string`) **(필수)** - 게시글 제목 (1-100자)
  - `body` (`string`) **(필수)** - 게시글 본문 (1-2000자)
  - `isAnnouncement` (`boolean`) *(선택)* - 공지사항 여부 (운영진만 가능)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/clubs/{id}/feed" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-clubs--id--join"></a>
### POST `/api/v1/clubs/{id}/join`

**설명:** 클럽 공개 가입

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_joinClub`

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
curl -X POST "https://easy-scraping.com/api/v1/clubs/{id}/join" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-clubs--id--leave"></a>
### POST `/api/v1/clubs/{id}/leave`

**설명:** 클럽 탈퇴

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_leaveClub`

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
curl -X POST "https://easy-scraping.com/api/v1/clubs/{id}/leave" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-clubs--id--members"></a>
### GET `/api/v1/clubs/{id}/members`

**설명:** 클럽 회원 명부 조회

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_listClubMembers`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |
| `query` | `limit` | `string` | **필수** | - |
| `query` | `offset` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/clubs/{id}/members" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="patch--api-v1-clubs--id--members--userid--role"></a>
### PATCH `/api/v1/clubs/{id}/members/{userId}/role`

**설명:** 클럽 회원 역할 변경

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_updateMemberRole`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |
| `path` | `userId` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `role` (`string`) **(필수)** - 변경할 역할

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PATCH "https://easy-scraping.com/api/v1/clubs/{id}/members/{userId}/role" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-clubs--id--projects"></a>
### GET `/api/v1/clubs/{id}/projects`

**설명:** 협동 프로젝트 목록 조회

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_listClubProjects`

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
curl -X GET "https://easy-scraping.com/api/v1/clubs/{id}/projects" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-clubs--id--projects--projectid--contributions"></a>
### POST `/api/v1/clubs/{id}/projects/{projectId}/contributions`

**설명:** 협동 프로젝트 WLD 펀딩 기여 (영구 소각)

- **분류 도메인 (Tag):** `clubs`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ClubController_contributeToProject`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |
| `path` | `projectId` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `amountWld` (`number`) **(필수)** - 기여할 WLD 금액
  - `idempotencyKey` (`string`) **(필수)** - 클라이언트 멱등성 키

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/clubs/{id}/projects/{projectId}/contributions" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-collections"></a>
### GET `/api/v1/collections`

**설명:** 내 수집품 조각 목록 조회

- **분류 도메인 (Tag):** `collections`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `CollectionController_listCollections`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/collections" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-collections--id-"></a>
### PUT `/api/v1/collections/{id}`

**설명:** 수집품 유저 메모 및 즐겨찾기 수정

- **분류 도메인 (Tag):** `collections`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `CollectionController_updatePiece`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `userNote` (`string`) *(선택)* - 수집품 유저 애착 메모 (최대 500자)
  - `isFavorite` (`boolean`) *(선택)* - 즐겨찾기 여부

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/collections/{id}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-collections-curation-advance"></a>
### POST `/api/v1/collections/curation/advance`

**설명:** 소유권 큐레이션 사다리 단계 진척

- **분류 도메인 (Tag):** `collections`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `CollectionController_advanceCuration`
#### 📦 요청 본문 (Request Body)

  - `targetStep` (`number`) *(선택)* - 소유권 7단계 사다리 목표 단계 (1-7)
  - `timelineDay` (`string`) *(선택)* - D1~D7 타임라인 일차

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/collections/curation/advance" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-collections-curation-status"></a>
### GET `/api/v1/collections/curation/status`

**설명:** D1~D7 소유권 큐레이션 사다리 상태 조회

- **분류 도메인 (Tag):** `collections`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `CollectionController_getCurationStatus`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/collections/curation/status" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-seasons-claim-rewards"></a>
### POST `/api/v1/seasons/claim-rewards`

**설명:** 시즌 보상 청구 및 수령

- **분류 도메인 (Tag):** `seasons`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `SeasonController_claimReward`
#### 📦 요청 본문 (Request Body)

  - `seasonId` (`string`) **(필수)** - 시즌 ID
  - `idempotencyKey` (`string`) **(필수)** - 클라이언트 멱등성 키

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/seasons/claim-rewards" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-seasons-current"></a>
### GET `/api/v1/seasons/current`

**설명:** 현재 시즌 정보 및 내 티어/랭킹 조회

- **분류 도메인 (Tag):** `seasons`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SeasonController_current`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/seasons/current" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-seasons-events"></a>
### GET `/api/v1/seasons/events`

**설명:** Active season events

- **분류 도메인 (Tag):** `seasons`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SeasonController_events`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/seasons/events" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-seasons-events--id--consumptions"></a>
### POST `/api/v1/seasons/events/{id}/consumptions`

**설명:** Spend on a season event

- **분류 도메인 (Tag):** `seasons`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `SeasonController_consume`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `quantity` (`number`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/seasons/events/{id}/consumptions" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-seasons-events--id--leaderboard"></a>
### GET `/api/v1/seasons/events/{id}/leaderboard`

**설명:** Leaderboard for one event

- **분류 도메인 (Tag):** `seasons`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SeasonController_leaderboard`

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
curl -X GET "https://easy-scraping.com/api/v1/seasons/events/{id}/leaderboard" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-seasons-hall-of-fame"></a>
### GET `/api/v1/seasons/hall-of-fame`

**설명:** 역대 시즌 명예의 전당 헌액자 목록

- **분류 도메인 (Tag):** `seasons`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SeasonController_hallOfFame`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/seasons/hall-of-fame" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-seasons-settle"></a>
### POST `/api/v1/seasons/settle`

**설명:** 시즌 종료 정산 엔진 (명예의 전당 및 6대 티어 보상 분배)

- **분류 도메인 (Tag):** `seasons`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `SeasonController_settle`
#### 📦 요청 본문 (Request Body)

  - `seasonId` (`string`) *(선택)* - 정산 대상 시즌 ID (생략 시 현재 활성 시즌)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/seasons/settle" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-spaces"></a>
### GET `/api/v1/spaces`

**설명:** 사용자가 분양받아 보유 중인 나만의 개인 공간 목록 조회

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_listMySpaces`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/spaces" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-spaces--id-"></a>
### GET `/api/v1/spaces/{id}`

**설명:** 지정한 개인 공간의 상세 스펙 및 소유권 정보 조회

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_getSpaceById`

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
curl -X GET "https://easy-scraping.com/api/v1/spaces/{id}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-spaces--id--layout"></a>
### PUT `/api/v1/spaces/{id}/layout`

**설명:** 개인 공간 8x8 인터랙티브 가구 배치 및 인테리어 레이아웃 저장

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_updateLayout`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `layout` (`object`) **(필수)** - 공간 가구/인테리어 레이아웃 JSON

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/spaces/{id}/layout" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-spaces--id--tax-pay"></a>
### POST `/api/v1/spaces/{id}/tax/pay`

**설명:** 개인 공간 일일 부동산세 자진 납부 (100% 영구 소각 SINK_PROPERTY_TAX)

💡 **통화 소각 룰**: 납부된 부동산세는 `SINK_PROPERTY_TAX` 사유로 100% 전액 원천 소각됩니다. 미납 시 7일 유예 후 강제 공매 회부됩니다.

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_payPropertyTax`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `days` (`number`) **(필수)** - 납부할 일수 (1-30일)
  - `idempotencyKey` (`string`) **(필수)** - 클라이언트 소유 멱등성 키

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/spaces/{id}/tax/pay" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-spaces--id--tax-status"></a>
### GET `/api/v1/spaces/{id}/tax/status`

**설명:** 공간별 일일 보유세율, 완납 기한, 체납 일수 및 공매 상태 조회

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_getSpaceTaxStatus`

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
curl -X GET "https://easy-scraping.com/api/v1/spaces/{id}/tax/status" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-spaces-city-projects"></a>
### GET `/api/v1/spaces/city/projects`

**설명:** 머니버스 시민 공동 출자 공공 인프라 크라우드펀딩 프로젝트 목록 조회

💡 **통화 소각 룰**: 출자된 WLD는 `SINK_PROJECT_DONATION` 사유로 100% 영구 소각되며, 공공 랜드마크 건립 기여도로 영구 아카이빙됩니다.

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_listCityProjects`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/spaces/city/projects" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-spaces-city-projects--id--contributions"></a>
### POST `/api/v1/spaces/city/projects/{id}/contributions`

**설명:** 공공 도시 인프라 크라우드펀딩 WLD 출자 기여 (100% 소각 SINK_PROJECT_DONATION)

💡 **통화 소각 룰**: 출자된 WLD는 `SINK_PROJECT_DONATION` 사유로 100% 영구 소각되며, 공공 랜드마크 건립 기여도로 영구 아카이빙됩니다.

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_contributeCityProject`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `amountWld` (`number`) **(필수)** - 기여할 WLD 금액
  - `idempotencyKey` (`string`) **(필수)** - 클라이언트 소유 멱등성 키

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/spaces/city/projects/{id}/contributions" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-spaces-purchase"></a>
### POST `/api/v1/spaces/purchase`

**설명:** 신규 개인 공간 분양 신청 (대금 100% 영구 소각 SINK_HOUSING_PURCHASE)

💡 **통화 소각 룰**: 분양 대금은 `SINK_HOUSING_PURCHASE` 사유로 100% 영구 소각 처리되어 게임 밸런스에 인플레이션을 유발하지 않습니다.

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_purchaseSpace`
#### 📦 요청 본문 (Request Body)

  - `spaceType` (`string`) **(필수)** - 공간 유형
  - `name` (`string`) **(필수)** - 공간 이름 (1-50자)
  - `idempotencyKey` (`string`) **(필수)** - 클라이언트 소유 멱등성 키

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/spaces/purchase" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-spaces-tax-delinquencies"></a>
### GET `/api/v1/spaces/tax/delinquencies`

**설명:** 부동산세 7일 이상 체납으로 법정 유예 경과한 시청 강제 공매 매물 목록 조회

- **분류 도메인 (Tag):** `spaces`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `SpaceController_listTaxDelinquencies`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/spaces/tax/delinquencies" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

