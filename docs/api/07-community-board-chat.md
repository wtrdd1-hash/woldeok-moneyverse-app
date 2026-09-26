# 커뮤니티 게시판 & 실시간 채팅·미디어 API (Community, Board & Chat)

> 자유게시판 및 종목 토론방 글 작성/수정/삭제, 댓글, 좋아요, 광고 게재, 1:1 및 그룹 실시간 채팅, 메시지 읽음 처리, 안전 뮤트/차단, 갤러리 사진 업로드 및 미디어 스트리밍 엔드포인트

## 📋 목차 (Table of Contents)

- [POST `/api/v1/activity/events`](#post--api-v1-activity-events) - Ingest client activity telemetry events (page view, dwell, clicks) | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/activity/logs`](#get--api-v1-admin-activity-logs) - List user activity logs for administrators | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/admin/activity/traffic`](#get--api-v1-admin-activity-traffic) - Privacy-safe day/month/year traffic analytics for administrators | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [POST `/api/v1/admin/announcements`](#post--api-v1-admin-announcements) - Create or edit an announcement | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [GET `/api/v1/admin/announcements`](#get--api-v1-admin-announcements) - List all announcements for administrators | `🔓 공개 (게스트 허용)`
- [PUT `/api/v1/admin/announcements/{id}`](#put--api-v1-admin-announcements--id-) - Update an announcement | `🔓 공개 (게스트 허용)`
- [DELETE `/api/v1/admin/announcements/{id}`](#delete--api-v1-admin-announcements--id-) - Delete an announcement | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [PUT `/api/v1/admin/announcements/{id}/image`](#put--api-v1-admin-announcements--id--image) - Attach an uploaded image to a draft announcement | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- [PUT `/api/v1/admin/announcements/{id}/publication`](#put--api-v1-admin-announcements--id--publication) - Publish or unpublish an announcement | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/photos`](#get--api-v1-admin-photos) - List all gallery photos for administrators | `🔓 공개 (게스트 허용)`
- [DELETE `/api/v1/admin/photos/{id}`](#delete--api-v1-admin-photos--id-) - Delete a draft or published photo | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/photos/{id}/approval`](#post--api-v1-admin-photos--id--approval) - Approve and publish a pending member photo | `🔓 공개 (게스트 허용)`
- [PUT `/api/v1/admin/photos/{id}/publication`](#put--api-v1-admin-photos--id--publication) - Publish or unpublish a photo | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/admin/photos/metadata`](#post--api-v1-admin-photos-metadata) - Create or edit a photo record | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/admin/photos/submissions`](#get--api-v1-admin-photos-submissions) - List pending photo submissions awaiting review | `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- [GET `/api/v1/announcements`](#get--api-v1-announcements) - Published announcements | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/board/images/{key}`](#get--api-v1-board-images--key-) - Read an image attached to a visible board post | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/board/images/uploads`](#post--api-v1-board-images-uploads) - Upload one image for a board post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/board/posts`](#get--api-v1-board-posts) - Recent member board posts | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/board/posts`](#post--api-v1-board-posts) - Write a post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/board/posts/{id}`](#get--api-v1-board-posts--id-) - One post, with its body | `🔒 로그인 필수` `📜 약관동의 필수`
- [PUT `/api/v1/board/posts/{id}`](#put--api-v1-board-posts--id-) - Rewrite your own post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [DELETE `/api/v1/board/posts/{id}`](#delete--api-v1-board-posts--id-) - Delete your own post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/board/posts/{id}/comments`](#get--api-v1-board-posts--id--comments) - The replies on a post, oldest first | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/board/posts/{id}/comments`](#post--api-v1-board-posts--id--comments) - Reply to a post | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [DELETE `/api/v1/board/posts/{id}/comments/{commentId}`](#delete--api-v1-board-posts--id--comments--commentid-) - Delete your own reply | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/board/public/images/{key}`](#get--api-v1-board-public-images--key-) - Public image attached to a visible board post | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/board/public/posts`](#get--api-v1-board-public-posts) - Public recent board posts | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/board/public/posts/{id}`](#get--api-v1-board-public-posts--id-) - Public board post | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/board/public/posts/{id}/comments`](#get--api-v1-board-public-posts--id--comments) - Public replies on a board post | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/board/public/stock-posts`](#get--api-v1-board-public-stock-posts) - 엔드포인트 상세 | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/board/stock-posts`](#post--api-v1-board-stock-posts) - 엔드포인트 상세 | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/chat/conversations`](#post--api-v1-chat-conversations) - 1:1 대화방 생성 또는 기존 대화방 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/chat/conversations`](#get--api-v1-chat-conversations) - 참여 중인 1:1 대화방 목록 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/chat/conversations/{id}/archive`](#post--api-v1-chat-conversations--id--archive) - 대화방 보관 또는 보관 해제 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/chat/conversations/{id}/messages`](#get--api-v1-chat-conversations--id--messages) - 대화방 메시지 이력 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/chat/conversations/{id}/messages`](#post--api-v1-chat-conversations--id--messages) - 대화방에 1:1 쪽지 메시지 전송 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/chat/conversations/{id}/mute`](#post--api-v1-chat-conversations--id--mute) - 대화방 알림 음소거 또는 해제 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/chat/conversations/{id}/read`](#post--api-v1-chat-conversations--id--read) - 대화방 메시지 읽음 처리 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/chat/conversations/{id}/report`](#post--api-v1-chat-conversations--id--report) - 부적절한 대화 내용 신고 및 증거 스냅샷 접수 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/chat/unread-count`](#get--api-v1-chat-unread-count) - 안 읽은 전체 쪽지 개수 조회 | `🔒 로그인 필수` `📜 약관동의 필수`
- [POST `/api/v1/chat/users/{id}/block`](#post--api-v1-chat-users--id--block) - 특정 회원 1:1 쪽지 차단 | `🔒 로그인 필수` `📜 약관동의 필수`
- [DELETE `/api/v1/chat/users/{id}/block`](#delete--api-v1-chat-users--id--block) - 특정 회원 1:1 쪽지 차단 해제 | `🔒 로그인 필수` `📜 약관동의 필수`
- [GET `/api/v1/content/announcements`](#get--api-v1-content-announcements) - App API: published announcements | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/content/photos`](#get--api-v1-content-photos) - App API: published gallery photos | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/content/status`](#get--api-v1-content-status) - App API: service status board | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/integrations/discord/interactions`](#post--api-v1-integrations-discord-interactions) - Discord interaction webhook | `🔓 공개 (게스트 허용)`
- [GET `/api/v1/photos`](#get--api-v1-photos) - Published gallery photos | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/photos`](#post--api-v1-photos) - Send an uploaded photo to the gallery for review | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/photos/mine`](#get--api-v1-photos-mine) - The caller’s own submissions and where each one got to | `🔓 공개 (게스트 허용)`
- [POST `/api/v1/photos/uploads`](#post--api-v1-photos-uploads) - Upload image bytes and receive a storage key | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/profile`](#get--api-v1-profile) - The caller’s own profile, with every field | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [PUT `/api/v1/profile`](#put--api-v1-profile) - Replace the caller’s profile and its per-field visibility | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/profile/{userId}`](#get--api-v1-profile--userid-) - Another member’s profile, as they have chosen to show it | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [POST `/api/v1/profile/image`](#post--api-v1-profile-image) - Upload a profile picture, replacing the current one | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [DELETE `/api/v1/profile/image`](#delete--api-v1-profile-image) - Remove the profile picture | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/profile/settings`](#get--api-v1-profile-settings) - The caller’s own profile settings, as stored | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/profile/titles`](#get--api-v1-profile-titles) - Profile titles actually awarded to the caller | `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- [GET `/api/v1/status`](#get--api-v1-status) - Server status board | `🔓 공개 (게스트 허용)`
- [GET `/media/{key}`](#get--media--key-) - Bytes of a published gallery photo | `🔓 공개 (게스트 허용)`
- [GET `/media/profile/{key}`](#get--media-profile--key-) - Bytes of a member’s profile picture, on their terms | `🔓 공개 (게스트 허용)`

---

## 🛠️ 엔드포인트 상세 규격

<a id="post--api-v1-activity-events"></a>
### POST `/api/v1/activity/events`

**설명:** Ingest client activity telemetry events (page view, dwell, clicks)

- **분류 도메인 (Tag):** `activity`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ActivityController_ingestEvents`
#### 📦 요청 본문 (Request Body)

  - `events` (`array`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/activity/events" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-activity-logs"></a>
### GET `/api/v1/admin/activity/logs`

**설명:** List user activity logs for administrators

- **분류 도메인 (Tag):** `activity`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `ActivityController_listLogs`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `limit` | `number` | 선택 | - |
| `query` | `offset` | `number` | 선택 | - |
| `query` | `eventType` | `string` | 선택 | Filter by event type |
| `query` | `userId` | `string` | 선택 | Filter by user UUID |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/activity/logs" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-admin-activity-traffic"></a>
### GET `/api/v1/admin/activity/traffic`

**설명:** Privacy-safe day/month/year traffic analytics for administrators

- **분류 도메인 (Tag):** `activity`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `ActivityController_traffic`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `granularity` | `string` | 선택 | - |
| `query` | `periods` | `number` | 선택 | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/activity/traffic" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-announcements"></a>
### POST `/api/v1/admin/announcements`

**설명:** Create or edit an announcement

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ContentController_saveAnnouncement`
#### 📦 요청 본문 (Request Body)

  - `announcementId` (`string`) *(선택)* - Omit to create
  - `title` (`string`) **(필수)**
  - `body` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/announcements" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-announcements"></a>
### GET `/api/v1/admin/announcements`

**설명:** List all announcements for administrators

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_adminAnnouncements`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/announcements" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-announcements--id-"></a>
### PUT `/api/v1/admin/announcements/{id}`

**설명:** Update an announcement

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_updateAnnouncement`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `title` (`string`) **(필수)**
  - `body` (`string`) **(필수)**
  - `isPinned` (`boolean`) *(선택)*
  - `contentState` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/announcements/{id}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="delete--api-v1-admin-announcements--id-"></a>
### DELETE `/api/v1/admin/announcements/{id}`

**설명:** Delete an announcement

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ContentController_deleteAnnouncement`

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
curl -X DELETE "https://easy-scraping.com/api/v1/admin/announcements/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-announcements--id--image"></a>
### PUT `/api/v1/admin/announcements/{id}/image`

**설명:** Attach an uploaded image to a draft announcement

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ContentController_setAnnouncementImage`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `storageKey` (`string`) **(필수)** - Key in the private image store
  - `altText` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/announcements/{id}/image" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="put--api-v1-admin-announcements--id--publication"></a>
### PUT `/api/v1/admin/announcements/{id}/publication`

**설명:** Publish or unpublish an announcement

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_publishAnnouncement`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `publish` (`boolean`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/announcements/{id}/publication" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-photos"></a>
### GET `/api/v1/admin/photos`

**설명:** List all gallery photos for administrators

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_listAllPhotos`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/photos" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="delete--api-v1-admin-photos--id-"></a>
### DELETE `/api/v1/admin/photos/{id}`

**설명:** Delete a draft or published photo

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_rejectPhoto`

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
curl -X DELETE "https://easy-scraping.com/api/v1/admin/photos/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-admin-photos--id--approval"></a>
### POST `/api/v1/admin/photos/{id}/approval`

**설명:** Approve and publish a pending member photo

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_approveMemberPhoto`

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
curl -X POST "https://easy-scraping.com/api/v1/admin/photos/{id}/approval" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-admin-photos--id--publication"></a>
### PUT `/api/v1/admin/photos/{id}/publication`

**설명:** Publish or unpublish a photo

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_publishPhoto`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `publish` (`boolean`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/admin/photos/{id}/publication" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-admin-photos-metadata"></a>
### POST `/api/v1/admin/photos/metadata`

**설명:** Create or edit a photo record

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_savePhoto`
#### 📦 요청 본문 (Request Body)

  - `photoId` (`string`) *(선택)*
  - `storageKey` (`string`) **(필수)** - Key in the private image store
  - `imageUrl` (`string`) **(필수)** - Source URL, validated against the allowed host list
  - `altText` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/admin/photos/metadata" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-admin-photos-submissions"></a>
### GET `/api/v1/admin/photos/submissions`

**설명:** List pending photo submissions awaiting review

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `👑 운영진 전용`
- **엔드포인트 핸들러 ID:** `ContentController_listPendingPhotos`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/admin/photos/submissions" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-announcements"></a>
### GET `/api/v1/announcements`

**설명:** Published announcements

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_announcements`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/announcements" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-board-images--key-"></a>
### GET `/api/v1/board/images/{key}`

**설명:** Read an image attached to a visible board post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `BoardImageController_image`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `key` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/board/images/{key}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-board-images-uploads"></a>
### POST `/api/v1/board/images/uploads`

**설명:** Upload one image for a board post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `BoardImageController_upload`
#### 📦 요청 본문 (Request Body)

*(빈 객체)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/board/images/uploads" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-board-posts"></a>
### GET `/api/v1/board/posts`

**설명:** Recent member board posts

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `BoardController_list`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/board/posts" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-board-posts"></a>
### POST `/api/v1/board/posts`

**설명:** Write a post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `BoardController_create`
#### 📦 요청 본문 (Request Body)

  - `title` (`string`) **(필수)**
  - `body` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**
  - `imageStorageKey` (`string`) *(선택)*
  - `imageAltText` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/board/posts" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-board-posts--id-"></a>
### GET `/api/v1/board/posts/{id}`

**설명:** One post, with its body

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `BoardController_read`

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
curl -X GET "https://easy-scraping.com/api/v1/board/posts/{id}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-board-posts--id-"></a>
### PUT `/api/v1/board/posts/{id}`

**설명:** Rewrite your own post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `BoardController_update`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `title` (`string`) **(필수)**
  - `body` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**
  - `imageStorageKey` (`string`) *(선택)*
  - `imageAltText` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/board/posts/{id}" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="delete--api-v1-board-posts--id-"></a>
### DELETE `/api/v1/board/posts/{id}`

**설명:** Delete your own post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `BoardController_remove`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **204** | - | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X DELETE "https://easy-scraping.com/api/v1/board/posts/{id}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-board-posts--id--comments"></a>
### GET `/api/v1/board/posts/{id}/comments`

**설명:** The replies on a post, oldest first

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `BoardController_comments`

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
curl -X GET "https://easy-scraping.com/api/v1/board/posts/{id}/comments" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-board-posts--id--comments"></a>
### POST `/api/v1/board/posts/{id}/comments`

**설명:** Reply to a post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `BoardController_reply`

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
curl -X POST "https://easy-scraping.com/api/v1/board/posts/{id}/comments" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="delete--api-v1-board-posts--id--comments--commentid-"></a>
### DELETE `/api/v1/board/posts/{id}/comments/{commentId}`

**설명:** Delete your own reply

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `BoardController_removeComment`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |
| `path` | `commentId` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **204** | - | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X DELETE "https://easy-scraping.com/api/v1/board/posts/{id}/comments/{commentId}" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-board-public-images--key-"></a>
### GET `/api/v1/board/public/images/{key}`

**설명:** Public image attached to a visible board post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `PublicBoardImageController_image`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `key` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/board/public/images/{key}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-board-public-posts"></a>
### GET `/api/v1/board/public/posts`

**설명:** Public recent board posts

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `PublicBoardController_list`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/board/public/posts" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-board-public-posts--id-"></a>
### GET `/api/v1/board/public/posts/{id}`

**설명:** Public board post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `PublicBoardController_read`

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
curl -X GET "https://easy-scraping.com/api/v1/board/public/posts/{id}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-board-public-posts--id--comments"></a>
### GET `/api/v1/board/public/posts/{id}/comments`

**설명:** Public replies on a board post

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `PublicBoardController_comments`

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
curl -X GET "https://easy-scraping.com/api/v1/board/public/posts/{id}/comments" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-board-public-stock-posts"></a>
### GET `/api/v1/board/public/stock-posts`

**설명:** 엔드포인트 상세

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `PublicStockCommunityController_list`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `query` | `stock` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/board/public/stock-posts" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-board-stock-posts"></a>
### POST `/api/v1/board/stock-posts`

**설명:** 엔드포인트 상세

- **분류 도메인 (Tag):** `board`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `StockCommunityController_create`
#### 📦 요청 본문 (Request Body)

  - `title` (`string`) **(필수)**
  - `body` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**
  - `imageStorageKey` (`string`) *(선택)*
  - `imageAltText` (`string`) *(선택)*
  - `stockSymbol` (`string`) **(필수)**
  - `category` (`string`) **(필수)**
  - `stance` (`string`) **(필수)**
  - `positionDisclosure` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/board/stock-posts" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-chat-conversations"></a>
### POST `/api/v1/chat/conversations`

**설명:** 1:1 대화방 생성 또는 기존 대화방 조회

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_openConversation`
#### 📦 요청 본문 (Request Body)

  - `peerUserId` (`string`) **(필수)** - 상대방 회원 ID

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/chat/conversations" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-chat-conversations"></a>
### GET `/api/v1/chat/conversations`

**설명:** 참여 중인 1:1 대화방 목록 조회

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_listConversations`

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
curl -X GET "https://easy-scraping.com/api/v1/chat/conversations" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-chat-conversations--id--archive"></a>
### POST `/api/v1/chat/conversations/{id}/archive`

**설명:** 대화방 보관 또는 보관 해제

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_archiveConversation`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `archived` (`boolean`) **(필수)** - 대화방 보관 여부

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/chat/conversations/{id}/archive" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-chat-conversations--id--messages"></a>
### GET `/api/v1/chat/conversations/{id}/messages`

**설명:** 대화방 메시지 이력 조회

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_listMessages`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |
| `query` | `limit` | `string` | **필수** | - |
| `query` | `beforeSequence` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/chat/conversations/{id}/messages" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-chat-conversations--id--messages"></a>
### POST `/api/v1/chat/conversations/{id}/messages`

**설명:** 대화방에 1:1 쪽지 메시지 전송

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_sendMessage`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `body` (`string`) **(필수)** - 메시지 본문
  - `idempotencyKey` (`string`) *(선택)* - 클라이언트 멱등성 키 (미제공시 서버 자동생성)

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/chat/conversations/{id}/messages" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-chat-conversations--id--mute"></a>
### POST `/api/v1/chat/conversations/{id}/mute`

**설명:** 대화방 알림 음소거 또는 해제

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_muteConversation`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `muted` (`boolean`) **(필수)** - 대화방 알림 음소거 여부

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/chat/conversations/{id}/mute" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-chat-conversations--id--read"></a>
### POST `/api/v1/chat/conversations/{id}/read`

**설명:** 대화방 메시지 읽음 처리

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_markAsRead`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `sequence` (`number`) **(필수)** - 읽은 마지막 메시지 시퀀스 번호

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/chat/conversations/{id}/read" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="post--api-v1-chat-conversations--id--report"></a>
### POST `/api/v1/chat/conversations/{id}/report`

**설명:** 부적절한 대화 내용 신고 및 증거 스냅샷 접수

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_reportConversation`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `id` | `string` | **필수** | - |

#### 📦 요청 본문 (Request Body)

  - `reason` (`string`) **(필수)** - 신고 사유 코드
  - `details` (`string`) **(필수)** - 구체적 신고 상세 사유

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/chat/conversations/{id}/report" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-chat-unread-count"></a>
### GET `/api/v1/chat/unread-count`

**설명:** 안 읽은 전체 쪽지 개수 조회

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_unreadCount`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/chat/unread-count" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-chat-users--id--block"></a>
### POST `/api/v1/chat/users/{id}/block`

**설명:** 특정 회원 1:1 쪽지 차단

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_blockUser`

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
curl -X POST "https://easy-scraping.com/api/v1/chat/users/{id}/block" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="delete--api-v1-chat-users--id--block"></a>
### DELETE `/api/v1/chat/users/{id}/block`

**설명:** 특정 회원 1:1 쪽지 차단 해제

- **분류 도메인 (Tag):** `chat`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수`
- **엔드포인트 핸들러 ID:** `ChatController_unblockUser`

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
curl -X DELETE "https://easy-scraping.com/api/v1/chat/users/{id}/block" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-content-announcements"></a>
### GET `/api/v1/content/announcements`

**설명:** App API: published announcements

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AppContentController_announcements`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/content/announcements" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-content-photos"></a>
### GET `/api/v1/content/photos`

**설명:** App API: published gallery photos

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AppContentController_photos`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/content/photos" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-content-status"></a>
### GET `/api/v1/content/status`

**설명:** App API: service status board

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `AppContentController_status`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/content/status" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-integrations-discord-interactions"></a>
### POST `/api/v1/integrations/discord/interactions`

**설명:** Discord interaction webhook

- **분류 도메인 (Tag):** `discord`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `DiscordController_interactions`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/integrations/discord/interactions" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-photos"></a>
### GET `/api/v1/photos`

**설명:** Published gallery photos

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_photos`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/photos" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-photos"></a>
### POST `/api/v1/photos`

**설명:** Send an uploaded photo to the gallery for review

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `MemberPhotoController_submit`
#### 📦 요청 본문 (Request Body)

  - `storageKey` (`string`) **(필수)** - A key this server issued from POST /photos/uploads
  - `altText` (`string`) **(필수)**
  - `idempotencyKey` (`string`) **(필수)**

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/photos" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-photos-mine"></a>
### GET `/api/v1/photos/mine`

**설명:** The caller’s own submissions and where each one got to

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MemberPhotoController_mine`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/photos/mine" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-photos-uploads"></a>
### POST `/api/v1/photos/uploads`

**설명:** Upload image bytes and receive a storage key

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `MemberPhotoController_upload`
#### 📦 요청 본문 (Request Body)

*(빈 객체)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/photos/uploads" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-profile"></a>
### GET `/api/v1/profile`

**설명:** The caller’s own profile, with every field

- **분류 도메인 (Tag):** `profile`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProfileController_mine`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/profile" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="put--api-v1-profile"></a>
### PUT `/api/v1/profile`

**설명:** Replace the caller’s profile and its per-field visibility

- **분류 도메인 (Tag):** `profile`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProfileController_replace`
#### 📦 요청 본문 (Request Body)

  - `visibility` (`string`) **(필수)**
  - `displayName` (`string`) *(선택)*
  - `imageUrl` (`string`) *(선택)*
  - `fieldVisibility` (`object`) *(선택)*
  - `featuredTitle` (`string`) *(선택)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X PUT "https://easy-scraping.com/api/v1/profile" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="get--api-v1-profile--userid-"></a>
### GET `/api/v1/profile/{userId}`

**설명:** Another member’s profile, as they have chosen to show it

- **분류 도메인 (Tag):** `profile`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProfileController_member`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `userId` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/profile/{userId}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="post--api-v1-profile-image"></a>
### POST `/api/v1/profile/image`

**설명:** Upload a profile picture, replacing the current one

- **분류 도메인 (Tag):** `profile`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProfileController_uploadImage`
#### 📦 요청 본문 (Request Body)

*(빈 객체)*

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **201** | 생성 완료 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X POST "https://easy-scraping.com/api/v1/profile/image" \
  -H "Content-Type: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN" \
  -d '{
    "idempotencyKey": "00000000-0000-4000-8000-000000000000"
  }'
```

---

<a id="delete--api-v1-profile-image"></a>
### DELETE `/api/v1/profile/image`

**설명:** Remove the profile picture

- **분류 도메인 (Tag):** `profile`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProfileController_removeImage`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **204** | - | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X DELETE "https://easy-scraping.com/api/v1/profile/image" \
  -H "Accept: application/json" \
  -H "x-csrf-token: YOUR_CSRF_TOKEN" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-profile-settings"></a>
### GET `/api/v1/profile/settings`

**설명:** The caller’s own profile settings, as stored

- **분류 도메인 (Tag):** `profile`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProfileController_settings`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/profile/settings" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-profile-titles"></a>
### GET `/api/v1/profile/titles`

**설명:** Profile titles actually awarded to the caller

- **분류 도메인 (Tag):** `profile`
- **보안 및 권한 계층 (Guards):** `🔒 로그인 필수` `📜 약관동의 필수` `🛡️ CSRF 검증`
- **엔드포인트 핸들러 ID:** `ProfileController_titles`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/profile/titles" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--api-v1-status"></a>
### GET `/api/v1/status`

**설명:** Server status board

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ContentController_status`
#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/api/v1/status" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--media--key-"></a>
### GET `/media/{key}`

**설명:** Bytes of a published gallery photo

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `MediaController_media`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `key` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/media/{key}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

<a id="get--media-profile--key-"></a>
### GET `/media/profile/{key}`

**설명:** Bytes of a member’s profile picture, on their terms

- **분류 도메인 (Tag):** `content`
- **보안 및 권한 계층 (Guards):** `🔓 공개 (게스트 허용)`
- **엔드포인트 핸들러 ID:** `ProfileImageController_image`

#### 📌 매개변수 (Parameters)

| 위치 | 이름 | 타입 | 필수 여부 | 설명 |
| :--- | :--- | :--- | :---: | :--- |
| `path` | `key` | `string` | **필수** | - |

#### 📤 응답 스키마 (Responses)

| HTTP 상태 코드 | 의미 | 응답 형식 |
| :---: | :--- | :--- |
| **200** | 성공 | JSON Object |

#### 💻 실제 호출 예시 (Example cURL)

```bash
curl -X GET "https://easy-scraping.com/media/profile/{key}" \
  -H "Accept: application/json" \
  -H "Cookie: __Host-session=YOUR_SESSION_TOKEN"
```

---

