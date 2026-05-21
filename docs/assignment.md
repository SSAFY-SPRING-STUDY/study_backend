# Assignment (과제) API 명세서

> Base URL: `/api/v1`
> 인증: Cookie 기반 JWT (`accessToken`)
> 응답 공통 형식: `{ "status": "SUCCESS"|"ERROR", "message": "...", "data": ... }`

---

## 공통 타입

### AssignmentProgressStatus

| 값 | 설명 |
|----|------|
| `APPLIED` | 신청됨 (GitHub Issue 생성 완료) |
| `COMPLETED` | 완료됨 (PR Merge → Webhook 자동 처리) |

---

## API 목록

| 메서드 | 경로 | 설명 | 권한 |
|--------|------|------|------|
| POST | `/studies/{studyId}/assignments` | 과제 생성 | LEADER 또는 ADMIN |
| GET | `/studies/{studyId}/assignments` | 과제 목록 조회 | 스터디 멤버 |
| GET | `/assignments/{assignmentId}` | 과제 상세 조회 | 스터디 멤버 |
| PUT | `/assignments/{assignmentId}` | 과제 수정 | LEADER 또는 ADMIN |
| DELETE | `/assignments/{assignmentId}` | 과제 삭제 | LEADER 또는 ADMIN |
| POST | `/assignments/{assignmentId}/apply` | 과제 신청 | 스터디 멤버 |
| GET | `/studies/{studyId}/progress/me` | 내 진행현황 조회 | 스터디 멤버 |

---

## 상세 명세

### POST `/studies/{studyId}/assignments`

과제를 생성합니다. **LEADER 또는 ADMIN만 가능.**

**Request Body**
```json
{
  "title": "과제 1: 스프링 CRUD 구현",
  "content": "과제 상세 설명 (마크다운 가능)",
  "prTemplate": "## 구현 내용\n\n## 테스트 결과\n\n## 참고 사항",
  "orderInStudy": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | String | ✅ | 과제 제목 |
| `content` | String | ✅ | 과제 상세 설명 (마크다운) |
| `prTemplate` | String | ✅ | PR 작성 가이드 템플릿 (마크다운) |
| `orderInStudy` | Integer | ✅ | 과제 순서 (1 이상) |

**Response** `201`
```json
{
  "status": "SUCCESS",
  "message": "과제가 생성되었습니다.",
  "data": {
    "assignmentId": 1,
    "studyId": 1,
    "title": "과제 1: 스프링 CRUD 구현",
    "content": "과제 상세 설명 (마크다운 가능)",
    "prTemplate": "## 구현 내용\n\n## 테스트 결과\n\n## 참고 사항",
    "orderInStudy": 1,
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-01-01T00:00:00"
  }
}
```

---

### GET `/studies/{studyId}/assignments`

스터디의 과제 목록을 조회합니다. 본인의 진행 상태(`myStatus`)가 함께 반환됩니다.

**Response** `200`
```json
{
  "status": "SUCCESS",
  "message": "과제 목록을 조회했습니다.",
  "data": [
    {
      "assignmentId": 1,
      "title": "과제 1: 스프링 CRUD 구현",
      "orderInStudy": 1,
      "myStatus": "COMPLETED"
    },
    {
      "assignmentId": 2,
      "title": "과제 2: JPA 연관관계 매핑",
      "orderInStudy": 2,
      "myStatus": "APPLIED"
    },
    {
      "assignmentId": 3,
      "title": "과제 3: 스프링 시큐리티",
      "orderInStudy": 3,
      "myStatus": null
    }
  ]
}
```

- `myStatus`: `null`(미신청) | `APPLIED`(신청 중) | `COMPLETED`(완료)
- `orderInStudy` 오름차순 정렬

---

### GET `/assignments/{assignmentId}`

과제 상세 정보를 조회합니다.

**Response** `200`
```json
{
  "status": "SUCCESS",
  "message": "과제를 조회했습니다.",
  "data": {
    "assignmentId": 1,
    "studyId": 1,
    "title": "과제 1: 스프링 CRUD 구현",
    "content": "과제 상세 설명 (마크다운 가능)",
    "prTemplate": "## 구현 내용\n\n## 테스트 결과\n\n## 참고 사항",
    "orderInStudy": 1,
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-01-01T00:00:00"
  }
}
```

---

### PUT `/assignments/{assignmentId}`

과제를 수정합니다. **LEADER 또는 ADMIN만 가능.**

**Request Body** → 생성 요청과 동일한 구조 (`AssignmentRequest`)

**Response** `200`
```json
{
  "status": "SUCCESS",
  "message": "과제가 수정되었습니다.",
  "data": { /* AssignmentResponse */ }
}
```

---

### DELETE `/assignments/{assignmentId}`

과제를 삭제합니다. **LEADER 또는 ADMIN만 가능.**

삭제된 과제보다 뒤 순번의 과제들은 `orderInStudy`가 자동으로 1씩 감소합니다.

**Response** `200`
```json
{
  "status": "SUCCESS",
  "message": "과제가 삭제되었습니다.",
  "data": null
}
```

---

### POST `/assignments/{assignmentId}/apply`

과제를 신청합니다. **GitHub Issue가 자동 생성됩니다.**

> 사전 조건:
> - GitHub 계정 연동 필수
> - 스터디 멤버의 GitHub 레포지토리 등록 필수
> - `orderInStudy > 1`인 경우, 이전 순번의 과제가 모두 `COMPLETED` 상태여야 함

**Request Body**: 없음

**Response** `201`
```json
{
  "status": "SUCCESS",
  "message": "과제 신청이 완료되었습니다.",
  "data": {
    "progressId": 1,
    "githubIssueNumber": 42,
    "status": "APPLIED"
  }
}
```

- `githubIssueNumber`: 생성된 GitHub Issue 번호 (조직 레포에서 확인 가능)
- Issue 제목 형식: `[과제 {orderInStudy}] {title}`

---

### GET `/studies/{studyId}/progress/me`

스터디 내 내 과제별 상세 진행현황을 조회합니다.

**Response** `200`
```json
{
  "status": "SUCCESS",
  "message": "내 진행현황을 조회했습니다.",
  "data": [
    {
      "progressId": 1,
      "assignmentId": 1,
      "title": "과제 1: 스프링 CRUD 구현",
      "orderInStudy": 1,
      "status": "COMPLETED",
      "githubIssueNumber": 42,
      "appliedAt": "2025-01-01T00:00:00",
      "completedAt": "2025-01-10T00:00:00"
    },
    {
      "progressId": 2,
      "assignmentId": 2,
      "title": "과제 2: JPA 연관관계 매핑",
      "orderInStudy": 2,
      "status": "APPLIED",
      "githubIssueNumber": 43,
      "appliedAt": "2025-01-11T00:00:00",
      "completedAt": null
    }
  ]
}
```

- 신청한 과제만 반환 (미신청 과제는 포함되지 않음)
- `completedAt`: 미완료 시 `null`

---

## 에러 코드

| 에러 코드 | HTTP | 메시지 | 발생 상황 |
|-----------|------|--------|-----------|
| `ASSIGNMENT_NOT_FOUND` | 404 | 존재하지 않는 과제입니다. | 존재하지 않는 `assignmentId` |
| `ASSIGNMENT_ALREADY_APPLIED` | 409 | 이미 신청한 과제입니다. | 중복 신청 |
| `ASSIGNMENT_PREREQUISITE_NOT_MET` | 403 | 이전 과제를 먼저 완료해야 합니다. | 이전 순번 과제 미완료 상태에서 신청 |
| `ASSIGNMENT_PROGRESS_NOT_FOUND` | 404 | 과제 진행 이력이 없습니다. | 진행 기록 없음 |
| `GITHUB_ACCOUNT_NOT_LINKED` | 400 | GitHub 계정이 연동되어 있지 않습니다. | 과제 신청 시 GitHub 미연동 |
| `GITHUB_REPO_NOT_REGISTERED` | 400 | GitHub 레포지토리가 등록되어 있지 않습니다. | 과제 신청 시 레포 미등록 |
| `GITHUB_ISSUE_CREATION_FAILED` | 502 | GitHub Issue 생성에 실패했습니다. | GitHub API 호출 실패 |
| `FORBIDDEN` | 403 | 권한이 없습니다. | LEADER/ADMIN 전용 API를 일반 멤버가 호출 |
| `STUDY_MEMBER_NOT_FOUND` | 404 | 스터디 멤버가 아닙니다. | 스터디 비가입 상태에서 접근 |

---

## 과제 진행 플로우

```
과제 생성 (LEADER/ADMIN)
    ↓
과제 신청 (멤버)
  - GitHub Issue 자동 생성
  - status: APPLIED
    ↓
PR 작성 및 Merge (멤버 → GitHub)
  - PR body에 issue 번호 참조
    ↓
Webhook 수신 (자동)
  - status: COMPLETED
  - completedAt 기록
```
