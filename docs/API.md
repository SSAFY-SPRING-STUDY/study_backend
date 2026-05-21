# API 명세서

> **Base URL**: `/api/v1`
> **인증**: Cookie 기반 JWT (`accessToken`, `refreshToken`)
> **응답 형식**: `{ "status": "SUCCESS"|"ERROR", "message": "...", "data": ... }`

**범례**: 🔓 비인증 가능 &nbsp;|&nbsp; 🔐 로그인 필수

---

## 공통 에러 코드

| HTTP | 상황 |
|------|------|
| 400 | 입력값 오류 |
| 401 | 인증 필요 (토큰 없음·만료) |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 중복 (이미 존재) |

토큰 만료 시 `POST /auth/refresh`로 재발급 후 재시도.

---

## 공통 Enum

| Enum | 값 |
|------|----|
| `StudyType` | `ALGORITHM`, `BACKEND`, `COMPUTER_SCIENCE` |
| `DifficultyLevel` | `BASIC`, `INTERMEDIATE`, `ADVANCED` |
| `StudyMemberRole` | `LEADER`, `MEMBER` |
| `AssignmentProgressStatus` | `APPLIED`, `COMPLETED` |

---

## 1. 인증 (Auth)

### 🔓 POST `/auth/login`
이메일/비밀번호 로그인. 성공 시 Cookie에 토큰 세팅.

**Request**
```json
{ "email": "user@email.com", "password": "password123" }
```
**Response** `200`
```json
{
  "data": {
    "id": 1, "email": "user@email.com", "name": "홍길동",
    "nickname": "gildong", "level": "BASIC", "role": "ROLE_USER",
    "githubUsername": "gildong-gh"
  }
}
```

---

### 🔓 GET `/auth/github`
GitHub OAuth 로그인 페이지로 리다이렉트 (302).

### 🔓 GET `/auth/github/callback?code={code}`
GitHub OAuth 콜백. 신규 회원이면 자동 가입 후 JWT 발급.

---

### 🔓 POST `/auth/refresh`
RefreshToken으로 AccessToken 재발급. Cookie 자동 갱신.
**Response** `200` (data: null)

### 🔓 POST `/auth/logout`
로그아웃. Cookie 삭제.
**Response** `200` (data: null)

---

## 2. 회원 (Member)

### 🔓 POST `/members/signup`
회원가입.

**Request**
```json
{
  "email": "user@email.com",  // 최대 100자
  "password": "password123",  // 8~30자
  "name": "홍길동",            // 최대 50자
  "nickname": "gildong"       // 1~50자
}
```
**Response** `201` (data: null)

---

### 🔐 GET `/members/me`
내 정보 조회. **Response** `200` → MemberInfo (로그인 응답과 동일)

---

### 🔐 GET `/members/me/github/connect`
GitHub 계정 연동 시작. 302 리다이렉트.

### 🔓 GET `/members/me/github/connect/callback?code={code}&state={state}`
GitHub 계정 연동 콜백. **Response** `200` (data: null)

---

## 3. 스터디 (Study)

### 🔓 GET `/studies?studyType={type}&page=0&size=10`
타입별 스터디 목록 (페이지네이션).

**Response** `200`
```json
{
  "data": {
    "content": [
      { "id": 1, "name": "백엔드 스터디 1기", "description": "...",
        "level": "BASIC", "type": "BACKEND", "githubOrgName": "my-org" }
    ],
    "totalElements": 20, "totalPages": 2, "number": 0, "size": 10
  }
}
```

---

### 🔓 GET `/studies/{studyId}`
스터디 상세.

**Response** `200`
```json
{
  "data": {
    "id": 1, "name": "백엔드 스터디 1기", "description": "...",
    "level": "BASIC", "type": "BACKEND", "githubOrgName": "my-org"
  }
}
```

---

### 🔐 POST `/studies/{studyId}/members`
스터디 참여. GitHub 계정 연동 필수. 입력한 레포명으로 GitHub 조직에 레포 자동 생성.

**Request**
```json
{ "githubRepoName": "my-algorithm-repo" }
```
**Response** `201` (data: null)

**에러**
| 코드 | 상황 |
|------|------|
| 409 | 이미 가입된 스터디 |
| 400 | GitHub 계정 미연동 |
| 502 | GitHub 레포 생성 실패 |

---

### 🔐 DELETE `/studies/{studyId}/members/me`
스터디 탈퇴. 과제 진행 기록 함께 삭제.
**Response** `200` (data: null)

**에러**: 400 — 유일한 리더는 탈퇴 불가

---

### 🔐 GET `/studies/{studyId}/members`
스터디원 목록.

**Response** `200`
```json
{
  "data": {
    "members": [
      { "memberId": 1, "nickname": "gildong", "name": "홍길동",
        "githubUsername": "gildong-gh", "role": "LEADER",
        "joinedAt": "2024-01-01T00:00:00", "githubRepoName": "my-repo" }
    ]
  }
}
```

---

## 4. 커리큘럼 (Curriculum)

### 🔓 GET `/studies/{studyId}/curriculums`
스터디의 커리큘럼 목록.

**Response** `200`
```json
{
  "data": [
    { "id": 1, "title": "1주차: Java 기초", "description": "...",
      "order": 1, "postsCount": 3 }
  ]
}
```

---

### 🔓 GET `/curriculums/{curriculumId}`
커리큘럼 상세.

**Response** `200`
```json
{
  "data": { "id": 1, "title": "1주차: Java 기초", "description": "...",
            "order": 1, "postsCount": 3 }
}
```

---

## 5. 게시글 (Post)

### 🔓 GET `/curriculums/{curriculumId}/posts`
커리큘럼의 게시글 목록.

**Response** `200`
```json
{
  "data": [
    { "postId": 1, "title": "Java 입문", "authorId": 1,
      "authorName": "홍길동", "curriculumId": 1, "orderInCurriculum": 1 }
  ]
}
```

---

### 🔓 GET `/posts/{postId}`
게시글 상세. `content`에 마크다운 포함.

**Response** `200`
```json
{
  "data": {
    "postId": 1, "title": "Java 입문", "content": "## 본문 내용...",
    "authorId": 1, "authorName": "홍길동",
    "curriculumId": 1, "orderInCurriculum": 1
  }
}
```

---

## 6. 댓글 / 대댓글 (Comment)

### 🔓 GET `/posts/{postId}/comments`
댓글 목록.

**Response** `200`
```json
{
  "data": [
    { "commentId": 1, "content": "좋은 글이네요", "authorId": 1,
      "authorName": "홍길동", "authorNickname": "gildong",
      "postId": 1, "reCommentCount": 2,
      "createdAt": "2024-01-01T00:00:00", "updatedAt": "2024-01-01T00:00:00" }
  ]
}
```

---

### 🔓 GET `/comments/{commentId}/recomments`
대댓글 목록.

**Response** `200`
```json
{
  "data": [
    { "reCommentId": 1, "content": "동의합니다", "authorId": 2,
      "authorName": "김철수", "authorNickname": "chulsu",
      "parentCommentId": 1,
      "createdAt": "2024-01-01T00:00:00", "updatedAt": "2024-01-01T00:00:00" }
  ]
}
```

---

### 🔐 POST `/posts/{postId}/comments`
댓글 작성.

**Request** `{ "content": "댓글 내용" }`
**Response** `201` → CommentResponse

---

### 🔐 PATCH `/comments/{commentId}`
댓글 수정 (본인만).

**Request** `{ "content": "수정된 내용" }`
**Response** `200` → CommentResponse

---

### 🔐 DELETE `/comments/{commentId}`
댓글 삭제 (본인만). **Response** `200` (data: null)

---

### 🔐 POST `/comments/{commentId}/recomments`
대댓글 작성.

**Request** `{ "content": "대댓글 내용" }`
**Response** `201` → ReCommentResponse

---

### 🔐 PATCH `/recomments/{reCommentId}`
대댓글 수정 (본인만).

**Request** `{ "content": "수정된 내용" }`
**Response** `200` → ReCommentResponse

---

### 🔐 DELETE `/recomments/{reCommentId}`
대댓글 삭제 (본인만). **Response** `200` (data: null)

---

## 7. 과제 (Assignment)

> 과제 기능 전체는 스터디 멤버 전용입니다.

### 🔐 GET `/studies/{studyId}/assignments`
과제 목록. `myStatus`는 본인의 진행 상태 (미신청 시 `null`).

**Response** `200`
```json
{
  "data": [
    { "assignmentId": 1, "title": "Hello World", "orderInStudy": 1,
      "myStatus": "APPLIED" }
  ]
}
```

---

### 🔐 GET `/assignments/{assignmentId}`
과제 상세.

**Response** `200`
```json
{
  "data": {
    "assignmentId": 1, "studyId": 1, "title": "Hello World",
    "content": "## 과제 내용...", "prTemplate": "## PR 템플릿...",
    "orderInStudy": 1,
    "createdAt": "2024-01-01T00:00:00", "updatedAt": "2024-01-01T00:00:00"
  }
}
```

---

### 🔐 POST `/assignments/{assignmentId}/apply`
과제 신청. GitHub Issue 자동 생성. 이전 순서 과제 완료 필수.

**Response** `201`
```json
{
  "data": { "progressId": 1, "githubIssueNumber": 42, "status": "APPLIED" }
}
```

**에러**
| 코드 | 상황 |
|------|------|
| 409 | 이미 신청한 과제 |
| 403 | 이전 과제 미완료 |
| 400 | GitHub 레포 미등록 |
| 502 | GitHub Issue 생성 실패 |

---

### 🔐 GET `/studies/{studyId}/progress/me`
내 과제 진행 현황.

**Response** `200`
```json
{
  "data": [
    { "progressId": 1, "assignmentId": 1, "title": "Hello World",
      "orderInStudy": 1, "status": "COMPLETED", "githubIssueNumber": 42,
      "appliedAt": "2024-01-01T00:00:00", "completedAt": "2024-01-02T00:00:00" }
  ]
}
```

---

## 8. GitHub 연동

> GitHub 연동 흐름: **회원 계정 연결** → **스터디 조직 설정(ADMIN)** → **스터디 참여 시 레포 자동 생성**

### 🔐 GET `/members/me/github/connect`
현재 계정에 GitHub 연동 시작. 302 리다이렉트.

### 🔓 GET `/members/me/github/connect/callback`
GitHub 연동 콜백 (state로 인증). **Response** `200` (data: null)

---

### 🔐 PATCH `/studies/{studyId}/members/{targetMemberId}/repo`
스터디원 GitHub 레포 등록·수정. 본인, 스터디 LEADER, ADMIN만 가능.

**Request** `{ "githubRepoName": "my-new-repo" }`
**Response** `200` (data: null)

---

### 이미지 업로드 (마크다운 에디터용)

### 🔐 POST `/images`
이미지 업로드. `multipart/form-data` (`file` 필드).

**Response** `201`
```json
{ "data": { "imageUrl": "/api/v1/images/abc123.png" } }
```

### 🔓 GET `/images/{filename}`
이미지 조회. `<img>` 태그 렌더링 시 인증 불필요.
