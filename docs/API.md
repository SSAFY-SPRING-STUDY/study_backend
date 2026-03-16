# API 문서

> Base URL: `http://localhost:8080`
> Content-Type: `application/json`

---

## 목차

- [공통 사항](#공통-사항)
- [인증 (Auth)](#인증-auth)
- [회원 (Member)](#회원-member)
- [스터디 (Study)](#스터디-study)
- [커리큘럼 (Curriculum)](#커리큘럼-curriculum)
- [게시글 (Post)](#게시글-post)
- [이미지 (Image)](#이미지-image)
- [공지사항 (Notice)](#공지사항-notice)
- [댓글 (Comment)](#댓글-comment)
- [퀴즈 (Quiz)](#퀴즈-quiz)
- [알림 (Notification)](#알림-notification)
- [에러 코드](#에러-코드)

---

## 공통 사항

### 응답 형식

모든 API는 동일한 래퍼 형식으로 응답합니다.

```json
{
  "message": "처리 결과 메시지",
  "data": { }
}
```

에러 응답:
```json
{
  "message": "에러 메시지",
  "data": null
}
```

### 인증 방식

JWT 토큰은 **HttpOnly 쿠키**로 전달됩니다. 브라우저는 자동으로 쿠키를 포함하므로 별도로 Authorization 헤더를 설정할 필요가 없습니다.

| 쿠키명 | 설명 |
|---|---|
| `access-token` | 액세스 토큰 (유효시간 30분) |
| `refresh-token` | 리프레시 토큰 (유효시간 7일) |

### 권한 레벨

| 표기 | 설명 |
|---|---|
| 🔓 공개 | 인증 불필요 |
| 🔐 인증 필요 | 로그인한 사용자 |
| 🔴 관리자 | `ROLE_ADMIN` 권한 필요 |

---

## 인증 (Auth)

### 로그인

**POST** `/api/v1/auth/login` — 🔓 공개

Request Body:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response `200`:
```json
{
  "message": "로그인에 성공했습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "nickName": "길동이",
    "level": "BRONZE",
    "role": "ROLE_USER"
  }
}
```

> 응답과 함께 `access-token`, `refresh-token` 쿠키가 설정됩니다.

---

### 토큰 재발급

**POST** `/api/v1/auth/refresh` — 🔓 공개 (refresh-token 쿠키 필요)

Request Body: 없음

Response `200`:
```json
{
  "message": "토큰 재발급에 성공했습니다.",
  "data": null
}
```

> `access-token`, `refresh-token` 쿠키가 새로운 토큰으로 교체됩니다 (토큰 로테이션).

---

### 로그아웃

**POST** `/api/v1/auth/logout` — 🔐 인증 필요

Request Body: 없음

Response `200`:
```json
{
  "message": "로그아웃에 성공했습니다.",
  "data": null
}
```

> 서버의 리프레시 토큰이 삭제되고, 쿠키가 만료됩니다.

---

## 회원 (Member)

### 회원가입

**POST** `/api/v1/members/signup` — 🔓 공개

Request Body:
```json
{
  "email": "user@example.com",    // 이메일 형식, 최대 100자
  "password": "password123",      // 8~30자
  "name": "홍길동",                // 최대 50자
  "nickname": "길동이"             // 3~50자
}
```

Response `200`:
```json
{
  "message": "회원가입에 성공했습니다.",
  "data": null
}
```

---

### 내 프로필 조회

**GET** `/api/v1/members/me` — 🔐 인증 필요

Response `200`:
```json
{
  "message": "회원 정보 조회에 성공했습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "nickName": "길동이",
    "level": "BRONZE",
    "role": "ROLE_USER"
  }
}
```

---

### 특정 회원 프로필 조회

**GET** `/api/v1/members/{memberId}` — 🔓 공개

Response `200`: [내 프로필 조회](#내-프로필-조회)와 동일한 형식

---

### 내 프로필 수정

**PATCH** `/api/v1/members/me` — 🔐 인증 필요

Request Body (변경할 필드만 포함):
```json
{
  "name": "홍길동",     // 최대 50자 (선택)
  "nickname": "길동이" // 3~50자 (선택)
}
```

Response `200`:
```json
{
  "message": "회원 정보 수정에 성공했습니다.",
  "data": { /* MemberInfo */ }
}
```

---

### 비밀번호 변경

**PATCH** `/api/v1/members/me/password` — 🔐 인증 필요

Request Body:
```json
{
  "currentPassword": "oldPassword123",  // 8~30자
  "newPassword": "newPassword456"       // 8~30자
}
```

Response `200`:
```json
{
  "message": "비밀번호 변경에 성공했습니다.",
  "data": null
}
```

---

## 스터디 (Study)

### 스터디 목록 조회 (타입별 페이징)

**GET** `/api/v1/studies` — 🔓 공개

Query Parameters:
| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `studyType` | String | - | `BACKEND` / `FRONTEND` / `ALGORITHM` |
| `page` | int | 0 | 페이지 번호 (0부터 시작) |
| `size` | int | 10 | 페이지 크기 |

Response `200`:
```json
{
  "message": "스터디 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Spring Boot 기초",
        "description": "스프링 부트를 처음 배우는 사람들을 위한 스터디",
        "level": "BASIC",
        "type": "BACKEND"
      }
    ],
    "totalPages": 3,
    "totalElements": 25,
    "number": 0,
    "size": 10
  }
}
```

> `level` 값: `BASIC` / `INTERMEDIATE` / `ADVANCED`

---

### 스터디 상세 조회

**GET** `/api/v1/studies/{studyId}` — 🔓 공개

Response `200`:
```json
{
  "message": "스터디 조회에 성공했습니다.",
  "data": {
    "id": 1,
    "name": "Spring Boot 기초",
    "description": "스프링 부트를 처음 배우는 사람들을 위한 스터디",
    "level": "BASIC",
    "type": "BACKEND"
  }
}
```

---

### 스터디 생성

**POST** `/api/v1/studies` — 🔴 관리자

Request Body:
```json
{
  "name": "Spring Boot 기초",                          // 최대 100자
  "description": "스프링 부트를 처음 배우는 사람들을 위한 스터디",  // 최대 500자 (선택)
  "level": "BASIC",                                   // BASIC / INTERMEDIATE / ADVANCED
  "type": "BACKEND"                                   // BACKEND / FRONTEND / ALGORITHM
}
```

Response `201`:
```json
{
  "message": "스터디가 생성되었습니다.",
  "data": { /* StudyResponse */ }
}
```

---

### 스터디 수정

**PUT** `/api/v1/studies/{studyId}` — 🔴 관리자

Request Body: [스터디 생성](#스터디-생성)과 동일

Response `200`:
```json
{
  "message": "스터디가 수정되었습니다.",
  "data": { /* StudyResponse */ }
}
```

---

### 스터디 삭제

**DELETE** `/api/v1/studies/{studyId}` — 🔴 관리자

Response `200`:
```json
{
  "message": "스터디가 삭제되었습니다.",
  "data": null
}
```

---

## 커리큘럼 (Curriculum)

### 커리큘럼 목록 조회

**GET** `/api/v1/studies/{studyId}/curriculums` — 🔓 공개

Query Parameters:
| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 10 | 페이지 크기 |

> `orderInStudy` 기준으로 오름차순 정렬됩니다.

Response `200`:
```json
{
  "message": "커리큘럼 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "1장. 스프링 부트 소개",
        "description": "스프링 부트의 기본 개념을 소개합니다.",
        "order": 1,
        "postsCount": 5
      }
    ],
    "totalPages": 2,
    "totalElements": 12,
    "number": 0,
    "size": 10
  }
}
```

---

### 커리큘럼 상세 조회

**GET** `/api/v1/curriculums/{curriculumId}` — 🔓 공개

Response `200`:
```json
{
  "message": "커리큘럼 조회에 성공했습니다.",
  "data": {
    "id": 1,
    "title": "1장. 스프링 부트 소개",
    "description": "스프링 부트의 기본 개념을 소개합니다.",
    "order": 1,
    "postsCount": 5
  }
}
```

---

### 커리큘럼 생성

**POST** `/api/v1/studies/{studyId}/curriculums` — 🔴 관리자

Request Body:
```json
{
  "name": "1장. 스프링 부트 소개",
  "description": "스프링 부트의 기본 개념을 소개합니다.",
  "order": 1
}
```

Response `201`:
```json
{
  "message": "커리큘럼이 생성되었습니다.",
  "data": { /* CurriculumResponse */ }
}
```

---

### 커리큘럼 수정

**PUT** `/api/v1/curriculums/{curriculumId}` — 🔴 관리자

Request Body: [커리큘럼 생성](#커리큘럼-생성)과 동일

Response `200`:
```json
{
  "message": "커리큘럼이 수정되었습니다.",
  "data": { /* CurriculumResponse */ }
}
```

---

### 커리큘럼 삭제

**DELETE** `/api/v1/curriculums/{curriculumId}` — 🔴 관리자

Response `200`:
```json
{
  "message": "커리큘럼이 삭제되었습니다.",
  "data": null
}
```

---

## 게시글 (Post)

### 게시글 목록 조회

**GET** `/api/v1/curriculums/{curriculumId}/posts` — 🔓 공개

> `orderInCurriculum` 기준으로 오름차순 정렬됩니다.

Response `200`:
```json
{
  "message": "게시글 목록 조회에 성공했습니다.",
  "data": [
    {
      "postId": 1,
      "title": "스프링 부트란?",
      "authorId": 1,
      "authorName": "관리자",
      "curriculumId": 1,
      "orderInCurriculum": 1
    }
  ]
}
```

---

### 게시글 상세 조회

**GET** `/api/v1/posts/{postId}` — 🔓 공개

Response `200`:
```json
{
  "message": "게시글 조회에 성공했습니다.",
  "data": {
    "postId": 1,
    "title": "스프링 부트란?",
    "content": "# 스프링 부트\n스프링 부트는 ...",
    "authorId": 1,
    "authorName": "관리자",
    "curriculumId": 1,
    "orderInCurriculum": 1
  }
}
```

---

### 게시글 생성

**POST** `/api/v1/curriculums/{curriculumId}/posts` — 🔴 관리자

Request Body:
```json
{
  "title": "스프링 부트란?",              // 최대 100자
  "content": "# 스프링 부트\n마크다운 내용"
}
```

Response `201`:
```json
{
  "message": "게시글이 생성되었습니다.",
  "data": { /* PostSimpleResponse */ }
}
```

---

### 게시글 수정

**PUT** `/api/v1/posts/{postId}` — 🔴 관리자

Request Body: [게시글 생성](#게시글-생성)과 동일

Response `200`:
```json
{
  "message": "게시글이 수정되었습니다.",
  "data": { /* PostSimpleResponse */ }
}
```

---

### 게시글 삭제

**DELETE** `/api/v1/posts/{postId}` — 🔴 관리자

Response `200`:
```json
{
  "message": "게시글이 삭제되었습니다.",
  "data": null
}
```

---

## 이미지 (Image)

마크다운 게시글의 이미지는 S3 Presigned URL 방식으로 업로드합니다. 단, **다운로드는 백엔드 프록시 엔드포인트를 통해 직접 스트리밍**하므로 URL이 만료되지 않습니다.
 
### 업로드 흐름

```
1. POST /images/presigned-url           →  presigned upload URL + imageId 발급 (postId 불필요)
2. PUT {presignedUploadUrl}             →  S3 직접 업로드 (Content-Type 헤더 필수)
3. PATCH /images/{imageId}/complete     →  업로드 완료 처리
4. 에디터에 /api/v1/images/{imageId} 를 이미지 경로로 삽입
5. POST /curriculums/{id}/posts         →  게시글 저장 시 imageIds 포함 → 이미지-게시글 연결
```

> 글쓰기를 취소하거나 브라우저를 닫으면 이미지는 고아(orphan) 상태로 남습니다.
> 매일 새벽 3시 스케줄러가 24시간 이상 경과한 고아 이미지를 S3·DB에서 자동 삭제합니다.

---

### Presigned 업로드 URL 발급

**POST** `/api/v1/posts/{postId}/images/presigned-url` — 🔓 공개

Request Body:
```json
{
  "contentType": "image/png",   // MIME 타입
  "contentLength": 204800,      // 파일 크기 (bytes, 양수)
  "fileName": "screenshot.png"
}
```

Response `201`:
```json
{
  "message": "이미지 업로드 URL이 발급되었습니다.",
  "data": {
    "imageId": 1,
    "imageUrl": "https://s3.amazonaws.com/bucket/...?X-Amz-Signature=...",  // presigned upload URL (5분 유효)
    "imageKey": "posts/1/uuid-filename.png"                                  // S3 오브젝트 키
  }
}
```

> `imageUrl`에 `PUT` 요청으로 파일을 직접 업로드합니다.
> 업로드 시 `Content-Type` 헤더를 발급 요청과 동일하게 설정해야 합니다.
> 업로드 완료 후 마크다운에는 `/api/v1/images/{imageId}` 경로를 삽입하세요.

---

### 업로드 완료 처리

**PATCH** `/api/v1/images/{imageId}/complete` — 🔓 공개

> `imageId`는 presigned URL 발급 시 DB에 저장된 이미지 ID입니다. (별도로 얻는 방법 필요 시 문의)

Response `200`:
```json
{
  "message": "이미지 업로드가 완료되었습니다.",
  "data": null
}
```

---

### 이미지 다운로드 URL 조회

**GET** `/api/v1/images/{imageId}` — 🔓 공개

Response `200`:
```json
{
  "message": "이미지가 성공적으로 조회되었습니다.",
  "data": {
    "imageUrl": "https://s3.amazonaws.com/bucket/...?X-Amz-Signature=...",  // 10분 유효 presigned URL
    "imageKey": "images/uuid-filename.png"
  }
}
```

---

### 게시글 이미지 목록 조회

**GET** `/api/v1/posts/{postId}/images` — 🔓 공개

Response `200`:
```json
{
  "message": "게시글의 이미지들이 성공적으로 조회되었습니다.",
  "data": [
    {
      "imageUrl": "https://s3.amazonaws.com/...",
      "imageKey": "images/uuid-filename.png"
    }
  ]
}
```

---

## 공지사항 (Notice)

### 공지사항 목록 조회

**GET** `/api/v1/notices` — 🔓 공개

Query Parameters:
| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 10 | 페이지 크기 |

Response `200`:
```json
{
  "message": "공지사항 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "서비스 점검 안내",
        "content": "2026년 3월 20일 오전 2시~4시 점검 예정입니다.",
        "createdAt": "2026-03-15T10:00:00",
        "updatedAt": "2026-03-15T10:00:00"
      }
    ],
    "totalPages": 1,
    "totalElements": 3,
    "number": 0,
    "size": 10
  }
}
```

---

### 공지사항 상세 조회

**GET** `/api/v1/notices/{noticeId}` — 🔓 공개

Response `200`:
```json
{
  "message": "공지사항 조회에 성공했습니다.",
  "data": {
    "id": 1,
    "title": "서비스 점검 안내",
    "content": "2026년 3월 20일 오전 2시~4시 점검 예정입니다.",
    "createdAt": "2026-03-15T10:00:00",
    "updatedAt": "2026-03-15T10:00:00"
  }
}
```

---

### 공지사항 생성

**POST** `/api/v1/notices` — 🔴 관리자

Request Body:
```json
{
  "title": "서비스 점검 안내",
  "content": "2026년 3월 20일 오전 2시~4시 점검 예정입니다."
}
```

Response `201`:
```json
{
  "message": "공지사항이 생성되었습니다.",
  "data": { /* NoticeResponse */ }
}
```

---

### 공지사항 수정

**PUT** `/api/v1/notices/{noticeId}` — 🔴 관리자

Request Body: [공지사항 생성](#공지사항-생성)과 동일

Response `200`:
```json
{
  "message": "공지사항이 수정되었습니다.",
  "data": { /* NoticeResponse */ }
}
```

---

### 공지사항 삭제

**DELETE** `/api/v1/notices/{noticeId}` — 🔴 관리자

Response `200`:
```json
{
  "message": "공지사항이 삭제되었습니다.",
  "data": null
}
```

---

## 댓글 (Comment)

### 댓글 목록 조회 (최상위 댓글)

**GET** `/api/v1/posts/{postId}/comments` — 🔓 공개

Query Parameters:
| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 10 | 페이지 크기 |

Response `200`:
```json
{
  "message": "댓글 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "commentId": 1,
        "content": "좋은 글이네요!",
        "authorId": 2,
        "authorName": "길동이",
        "postId": 1,
        "reCommentCount": 3,
        "createdAt": "2026-03-15T10:00:00",
        "updatedAt": "2026-03-15T10:00:00"
      }
    ],
    "totalPages": 1,
    "totalElements": 5,
    "number": 0,
    "size": 10
  }
}
```

---

### 댓글 작성

**POST** `/api/v1/posts/{postId}/comments` — 🔐 인증 필요

Request Body:
```json
{
  "content": "좋은 글이네요!"
}
```

Response `201`:
```json
{
  "message": "댓글이 작성되었습니다.",
  "data": null
}
```

---

### 댓글 수정

**PATCH** `/api/v1/comments/{commentId}` — 🔐 인증 필요 (작성자 본인)

Request Body:
```json
{
  "content": "수정된 댓글 내용입니다."
}
```

Response `200`:
```json
{
  "message": "댓글이 수정되었습니다.",
  "data": {
    "commentId": 1,
    "content": "수정된 댓글 내용입니다.",
    "authorId": 2,
    "authorName": "길동이",
    "postId": 1,
    "reCommentCount": 3,
    "createdAt": "2026-03-15T10:00:00",
    "updatedAt": "2026-03-15T11:00:00"
  }
}
```

---

### 댓글 삭제

**DELETE** `/api/v1/comments/{commentId}` — 🔐 인증 필요 (작성자 본인)

Response `200`:
```json
{
  "message": "댓글이 삭제되었습니다.",
  "data": null
}
```

---

### 대댓글 목록 조회

**GET** `/api/v1/comments/{commentId}/recomments` — 🔓 공개

Query Parameters:
| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 10 | 페이지 크기 |

Response `200`:
```json
{
  "message": "대댓글 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "reCommentId": 1,
        "content": "동의합니다!",
        "authorId": 3,
        "authorName": "철수",
        "parentCommentId": 1,
        "createdAt": "2026-03-15T10:30:00",
        "updatedAt": "2026-03-15T10:30:00"
      }
    ],
    "totalPages": 1,
    "totalElements": 3,
    "number": 0,
    "size": 10
  }
}
```

---

### 대댓글 작성

**POST** `/api/v1/comments/{commentId}/recomments` — 🔐 인증 필요

Request Body:
```json
{
  "content": "동의합니다!"
}
```

Response `201`:
```json
{
  "message": "대댓글이 작성되었습니다.",
  "data": null
}
```

---

### 대댓글 수정

**PATCH** `/api/v1/recomments/{reCommentId}` — 🔐 인증 필요 (작성자 본인)

Request Body:
```json
{
  "content": "수정된 대댓글입니다."
}
```

Response `200`:
```json
{
  "message": "대댓글이 수정되었습니다.",
  "data": {
    "reCommentId": 1,
    "content": "수정된 대댓글입니다.",
    "authorId": 3,
    "authorName": "철수",
    "parentCommentId": 1,
    "createdAt": "2026-03-15T10:30:00",
    "updatedAt": "2026-03-15T11:30:00"
  }
}
```

---

### 대댓글 삭제

**DELETE** `/api/v1/recomments/{reCommentId}` — 🔐 인증 필요 (작성자 본인)

Response `200`:
```json
{
  "message": "대댓글이 삭제되었습니다.",
  "data": null
}
```

---

## 퀴즈 (Quiz)

각 게시글에 1개의 퀴즈가 존재합니다. 퀴즈는 10문제 × 5지선다로 구성되며, 7점 이상이면 합격입니다.

### 프론트엔드 흐름

```
게시글 학습 완료 후 퀴즈 페이지 진입
    ↓
GET /posts/{postId}/quiz/attempts/me
    ├── 200 → 이전 시도 결과가 있음 → 결과 화면 표시
    └── 404 → 시도 이력 없음 → 퀴즈 문제 화면으로
                    ↓
            GET /posts/{postId}/quiz
                    ↓
            퀴즈 풀기 후 제출
                    ↓
            POST /posts/{postId}/quiz/submit → 결과 화면 표시
```

> 재시험 횟수 제한 없음. 결과 화면에서 "다시 도전" 선택 시 퀴즈 문제 화면으로 이동.

---

### 퀴즈 조회

**GET** `/api/v1/posts/{postId}/quiz` — 🔐 인증 필요

> **정답 정보 미포함.** `isCorrect` 필드는 응답에 포함되지 않습니다.

Response `200`:
```json
{
  "message": "퀴즈 조회에 성공했습니다.",
  "data": {
    "quizId": 1,
    "postId": 1,
    "createdAt": "2026-03-15T09:00:00",
    "questions": [
      {
        "questionId": 1,
        "questionOrder": 1,
        "question": "스프링 부트의 주요 특징은?",
        "options": [
          { "optionId": 1, "optionOrder": 1, "content": "자동 설정" },
          { "optionId": 2, "optionOrder": 2, "content": "수동 XML 설정" },
          { "optionId": 3, "optionOrder": 3, "content": "EJB 기반" },
          { "optionId": 4, "optionOrder": 4, "content": "WAR 배포 필수" },
          { "optionId": 5, "optionOrder": 5, "content": "서블릿 직접 구현" }
        ]
      }
      // ... 총 10문제
    ]
  }
}
```

---

### 퀴즈 제출

**POST** `/api/v1/posts/{postId}/quiz/submit` — 🔐 인증 필요

> 모든 문제에 대한 답안을 반드시 제출해야 합니다 (일부 제출 불가).
> `questionId`와 `selectedOptionId`는 해당 퀴즈에 유효한 값이어야 합니다.

Request Body:
```json
{
  "answers": [
    { "questionId": 1, "selectedOptionId": 1 },
    { "questionId": 2, "selectedOptionId": 7 },
    // ... 총 10문제 모두 포함
  ]
}
```

Response `200`:
```json
{
  "message": "퀴즈 제출이 완료되었습니다.",
  "data": {
    "attemptId": 1,
    "score": 8,
    "totalQuestions": 10,
    "passed": true,
    "createdAt": "2026-03-15T10:00:00",
    "results": [
      {
        "questionId": 1,
        "question": "스프링 부트의 주요 특징은?",
        "selectedOptionId": 1,
        "correctOptionId": 1,
        "correct": true
      }
      // ... 10문제 결과
    ]
  }
}
```

> `passed`: `score >= 7`이면 `true` (합격)

---

### 내 최근 시도 결과 조회

**GET** `/api/v1/posts/{postId}/quiz/attempts/me` — 🔐 인증 필요

Response `200`: [퀴즈 제출](#퀴즈-제출) 응답의 `data`와 동일한 형식

Response `404`: 시도 이력이 없는 경우 → 퀴즈 문제 화면으로 이동

---

### 퀴즈 응시 현황 조회 (관리자)

**GET** `/api/v1/posts/{postId}/quiz/attempts` — 🔴 관리자

> 해당 퀴즈에 응시한 회원별 **가장 최근 제출 결과**를 조회합니다. 동일 회원의 여러 시도 중 최신 1건만 포함됩니다.

Query Parameters:
| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 20 | 페이지 크기 |

Response `200`:
```json
{
  "message": "퀴즈 응시 현황을 조회했습니다.",
  "data": {
    "content": [
      {
        "attemptId": 5,
        "memberId": 2,
        "memberName": "홍길동",
        "memberNickname": "길동이",
        "score": 8,
        "passed": true,
        "attemptedAt": "2026-03-15T10:00:00"
      }
    ],
    "totalPages": 1,
    "totalElements": 42,
    "number": 0,
    "size": 20
  }
}
```

---

### 퀴즈 생성 (관리자)

**POST** `/api/v1/posts/{postId}/quiz/generate` — 🔴 관리자

> Gemini AI를 사용하여 게시글 내용 기반으로 퀴즈를 자동 생성합니다.
> 이미 퀴즈가 존재하면 `409 CONFLICT`를 반환합니다.

Request Body: 없음

Response `201`:
```json
{
  "message": "퀴즈가 생성되었습니다.",
  "data": { /* QuizResponse (정답 정보 포함) */ }
}
```

---

## 알림 (Notification)

### 실시간 알림 구독 (SSE)

**GET** `/api/v1/notifications/subscribe` — 🔐 인증 필요

> Server-Sent Events (SSE) 연결. `text/event-stream` 형식으로 실시간 알림을 수신합니다.

```javascript
const eventSource = new EventSource('/api/v1/notifications/subscribe', {
  withCredentials: true  // 쿠키 포함 필수
});

eventSource.onmessage = (event) => {
  const notification = JSON.parse(event.data);
};
```

---

### 알림 목록 조회

**GET** `/api/v1/notifications` — 🔐 인증 필요

Query Parameters:
| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | 0 | 페이지 번호 |
| `size` | int | 10 | 페이지 크기 |

Response `200`:
```json
{
  "message": "알림 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "content": "새로운 댓글이 달렸습니다.",
        "isRead": false,
        "createdAt": "2026-03-15T10:00:00"
      }
    ],
    "totalPages": 1,
    "totalElements": 5,
    "number": 0,
    "size": 10
  }
}
```

---

### 알림 읽음 처리

**PATCH** `/api/v1/notifications/{notificationId}/read` — 🔐 인증 필요

Response `200`:
```json
{
  "message": "알림이 읽음 처리되었습니다.",
  "data": null
}
```

---

## 에러 코드

| HTTP 상태 | 에러 코드 | 메시지 |
|---|---|---|
| 400 | `INVALID_INPUT_VALUE` | 유효하지 않은 입력 값입니다. |
| 400 | `INVALID_ENUM_VALUE` | 유효하지 않은 enum값입니다. |
| 400 | `INVALID_PASSWORD` | 기존 비밀번호의 정보가 올바르지 않습니다. |
| 400 | `SAME_AS_OLD_PASSWORD` | 새로운 비밀번호는 이전 비밀번호와 달라야 합니다. |
| 400 | `QUIZ_INVALID_SUBMISSION` | 제출한 답안이 유효하지 않습니다. |
| 401 | `UNAUTHORIZED` | 인증이 필요합니다. |
| 401 | `BAD_CREDENTIAL` | 아이디 또는 비밀번호가 올바르지 않습니다. |
| 401 | `TOKEN_EXPIRED` | 토큰이 만료되었습니다. 다시 로그인해주세요. |
| 401 | `INVALID_TOKEN` | 유효하지 않은 토큰입니다. 다시 로그인해주세요. |
| 401 | `REFRESH_TOKEN_NOT_FOUND` | RefreshToken이 존재하지 않습니다. |
| 403 | `FORBIDDEN` | 권한이 없습니다. |
| 403 | `POST_ACCESS_DENIED` | 게시물 작성자가 아닙니다. |
| 403 | `COMMENT_ACCESS_DENIED` | 접근할 수 있는 권한이 없습니다. |
| 404 | `MEMBER_NOT_FOUND` | 존재하지 않는 사용자입니다. |
| 404 | `STUDY_NOT_FOUND` | 존재하지 않는 스터디입니다. |
| 404 | `CURRICULUM_NOT_FOUND` | 존재하지 않는 커리큘럼입니다. |
| 404 | `POST_NOT_FOUND` | 존재하지 않는 게시물입니다. |
| 404 | `IMAGE_NOT_FOUND` | 존재하지 않는 이미지입니다. |
| 404 | `NOTICE_NOT_FOUND` | 존재하지 않는 공지사항입니다. |
| 404 | `COMMENT_NOT_FOUND` | 존재하지 않는 댓글입니다. |
| 404 | `RECOMMENT_NOT_FOUND` | 존재하지 않는 대댓글입니다. |
| 404 | `NOTIFICATION_NOT_FOUND` | 존재하지 않는 알림입니다. |
| 404 | `QUIZ_NOT_FOUND` | 퀴즈가 존재하지 않습니다. |
| 404 | `QUIZ_ATTEMPT_NOT_FOUND` | 퀴즈 시도 이력이 없습니다. |
| 409 | `EMAIL_DUPLICATE` | 이미 사용 중인 이메일입니다. |
| 409 | `USERNAME_DUPLICATE` | 이미 사용 중인 사용자 이름입니다. |
| 409 | `CURRICULUM_ORDER_CONSTRAINT_VIOLATION` | 커리큘럼의 orderInStudy는 같은 스터디 내에서 중복될 수 없습니다. |
| 409 | `QUIZ_ALREADY_EXISTS` | 이미 생성된 퀴즈가 있습니다. |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요. |
| 500 | `QUIZ_GENERATION_FAILED` | 퀴즈 생성에 실패했습니다. 잠시 후 다시 시도해주세요. |
