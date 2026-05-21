# GitHub 연동 API 명세서

> Base URL: `/api/v1`
> 인증: Cookie 기반 JWT (`accessToken`)
> 응답 공통 형식: `{ "status": "SUCCESS"|"ERROR", "message": "...", "data": ... }`

---

## 개요

GitHub 연동은 **3단계 구조**로 이루어집니다.

```
① 회원 GitHub 계정 연결 (개인)
    GET /members/me/github/connect

② 스터디에 GitHub 조직 설정 (ADMIN)
    POST /studies  또는  PUT /studies/{studyId}
    → githubOrgName, githubWebhookSecret 입력

③ 스터디 참여 시 개인 레포지토리 자동 생성 (멤버)
    POST /studies/{studyId}/members
    → githubRepoName 입력 → 조직에 Public 레포 자동 생성
```

---

## 1. 회원 GitHub 계정 연결

스터디 참여 및 과제 신청을 위해 **반드시 선행**되어야 합니다.

### GET `/members/me/github/connect`

GitHub OAuth 인증 페이지로 리다이렉트합니다. (인증 필요)

- 응답: `302 Redirect` → GitHub 인증 페이지
- 프론트엔드에서 이 URL로 단순 이동 처리

---

### GET `/members/me/github/connect/callback`

GitHub OAuth 콜백. 계정 연결 완료 후 프론트엔드로 리다이렉트합니다.

> GitHub에서 자동 호출. 프론트엔드가 직접 호출하지 않음.

**Query Parameters**

| 파라미터 | 설명 |
|---------|------|
| `code` | GitHub에서 전달한 인증 코드 |
| `state` | CSRF 방지용 상태값 (서버가 자동 검증) |

- 응답: `302 Redirect` → 프론트엔드 설정 URL

**에러 코드**

| 에러 코드 | HTTP | 메시지 |
|-----------|------|--------|
| `GITHUB_OAUTH_FAILED` | 502 | GitHub OAuth 처리에 실패했습니다. |
| `GITHUB_ACCOUNT_ALREADY_LINKED` | 409 | 이미 연동된 GitHub 계정입니다. |

---

## 2. 스터디 GitHub 조직 연동

스터디 생성 또는 수정 시 GitHub 조직 정보를 함께 설정합니다. **ADMIN만 가능.**

### POST `/studies` / PUT `/studies/{studyId}`

**Request Body** (GitHub 관련 필드)

```json
{
  "name": "백엔드 스터디 1기",
  "description": "스프링 심화 과정",
  "level": "BASIC",
  "type": "BACKEND",
  "githubOrgName": "my-org",
  "githubWebhookSecret": "your-webhook-secret"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `githubOrgName` | String | 선택 | GitHub 조직명 (Organization). 없으면 GitHub 기능 비활성화 |
| `githubWebhookSecret` | String | 선택 | Webhook 서명 검증용 시크릿. GitHub Webhook 설정 시 동일 값 입력 |

> `githubOrgName`이 없는 스터디에 멤버가 가입을 시도하면 `400 GITHUB_ORG_NOT_CONFIGURED` 에러가 반환됩니다.

**Response** `201` / `200`

```json
{
  "status": "SUCCESS",
  "message": "스터디가 성공적으로 생성되었습니다.",
  "data": {
    "id": 1,
    "name": "백엔드 스터디 1기",
    "description": "스프링 심화 과정",
    "level": "BASIC",
    "type": "BACKEND",
    "githubOrgName": "my-org"
  }
}
```

> `githubWebhookSecret`은 보안상 응답에 포함되지 않습니다.

---

## 3. 스터디 멤버 GitHub 레포지토리

### POST `/studies/{studyId}/members` — 스터디 참여 (레포 자동 생성)

스터디에 참여하면서 GitHub 레포지토리명을 입력하면, **스터디 조직에 Public 레포가 자동 생성**됩니다.

**사전 조건**
- GitHub 계정이 연동되어 있어야 함 (`/members/me/github/connect` 완료)
- 스터디에 `githubOrgName`이 설정되어 있어야 함

**Request Body**

```json
{
  "githubRepoName": "my-study-repo"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `githubRepoName` | String | ✅ | 생성할 레포지토리명 (영문, GitHub 명명 규칙 준수) |

**Response** `201` (data: null)

```json
{
  "status": "SUCCESS",
  "message": "스터디에 참여했습니다.",
  "data": null
}
```

**에러 코드**

| 에러 코드 | HTTP | 메시지 |
|-----------|------|--------|
| `GITHUB_ACCOUNT_NOT_LINKED` | 400 | GitHub 계정이 연동되어 있지 않습니다. |
| `GITHUB_ORG_NOT_CONFIGURED` | 400 | 스터디에 GitHub 조직이 설정되어 있지 않습니다. |
| `GITHUB_REPO_CREATION_FAILED` | 502 | GitHub 레포지토리 생성에 실패했습니다. (중복 레포명, 권한 부족 등) |
| `STUDY_MEMBER_ALREADY_EXISTS` | 409 | 이미 가입된 스터디입니다. |

---

### PATCH `/studies/{studyId}/members/{targetMemberId}/repo` — GitHub 레포 수정

스터디 멤버의 GitHub 레포지토리명을 수정합니다. **DB 값만 변경되며 GitHub에서 레포가 rename되지는 않습니다.**

**권한**: 본인 / 스터디 LEADER / 전역 ADMIN

**Request Body**

```json
{
  "githubRepoName": "updated-repo-name"
}
```

**Response** `200` (data: null)

```json
{
  "status": "SUCCESS",
  "message": "GitHub 레포가 등록되었습니다.",
  "data": null
}
```

**에러 코드**

| 에러 코드 | HTTP | 메시지 |
|-----------|------|--------|
| `FORBIDDEN` | 403 | 권한이 없습니다. (본인·LEADER·ADMIN이 아닌 경우) |
| `STUDY_MEMBER_NOT_FOUND` | 404 | 스터디 멤버가 아닙니다. |

---

## 4. 스터디원 목록 조회 (레포 정보 포함)

### GET `/studies/{studyId}/members`

스터디원 목록과 각 멤버의 GitHub 레포지토리명을 조회합니다.

**Response** `200`

```json
{
  "status": "SUCCESS",
  "message": "스터디원 목록을 조회했습니다.",
  "data": {
    "members": [
      {
        "memberId": 1,
        "nickname": "testUser",
        "name": "테스트유저",
        "githubUsername": "github-user",
        "role": "LEADER",
        "joinedAt": "2025-01-01T00:00:00",
        "githubRepoName": "my-study-repo"
      },
      {
        "memberId": 2,
        "nickname": "member2",
        "name": "멤버둘",
        "githubUsername": "github-user2",
        "role": "MEMBER",
        "joinedAt": "2025-01-02T00:00:00",
        "githubRepoName": null
      }
    ]
  }
}
```

- `githubRepoName`: 레포 미등록 시 `null`

---

## 5. GitHub Webhook

> 프론트엔드에서 직접 호출하지 않습니다. GitHub에서 자동 호출됩니다.

### POST `/webhook/github/{studyId}`

GitHub PR Merge 이벤트를 수신하여 과제를 자동 완료 처리합니다.

**Headers**
```
X-GitHub-Event: pull_request
X-Hub-Signature-256: sha256={HMAC-SHA256 서명}
```

**동작 방식**
1. PR body에 `<!-- progress_id: {id} -->` 포함 여부 확인
2. PR이 `closed` + `merged: true` 이면 해당 과제 진행 기록을 `COMPLETED` 처리
3. `X-Hub-Signature-256` 서명 불일치 시 `403` 반환

**Webhook 설정 방법** (GitHub 조직 설정)
- Payload URL: `https://{서버도메인}/api/v1/webhook/github/{studyId}`
- Content type: `application/json`
- Secret: 스터디 생성 시 입력한 `githubWebhookSecret`과 동일하게 설정
- 이벤트: `Pull requests`

---

## 전체 연동 흐름

```
[회원] GitHub 계정 연결
  GET /members/me/github/connect → GitHub OAuth → callback

[ADMIN] 스터디 생성 (조직 설정)
  POST /studies { githubOrgName, githubWebhookSecret }

[ADMIN] GitHub Webhook 설정
  GitHub Org → Settings → Webhooks → 위 Webhook 설정

[회원] 스터디 참여 (레포 자동 생성)
  POST /studies/{studyId}/members { githubRepoName }
  → GitHub Org에 Public 레포 자동 생성

[회원] 과제 신청 (Issue 자동 생성)
  POST /assignments/{assignmentId}/apply
  → GitHub Org 레포에 Issue 자동 생성

[회원] PR 작성 및 Merge
  → GitHub Webhook → 과제 자동 완료 처리
```
