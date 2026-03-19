# Admin 회원 관리 API

관리자(ROLE_ADMIN)가 전체 회원을 조회하고 정보를 수정하는 API입니다.

---

## 공통

- **Base URL**: `/api/v1/admin/members`
- **권한**: 모든 엔드포인트에 `ROLE_ADMIN` 필요
- **응답 형식**: `ApiResponse<T>` 래퍼

```json
{ "message": "...", "data": { ... } }
```

---

## API 목록

### 1. 회원 목록 조회

```
GET /api/v1/admin/members
```

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| keyword | String | N | 이메일 또는 닉네임 부분 일치 검색 |
| page | int | N | 페이지 번호 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20) |
| sort | String | N | 정렬 기준 (기본값: id,desc) |

**응답 예시** `200 OK`

```json
{
  "message": "회원 목록 조회가 완료되었습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user@example.com",
        "name": "홍길동",
        "nickName": "gildong",
        "level": "BASIC",
        "role": "ROLE_USER"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  }
}
```

---

### 2. 회원 상세 조회

```
GET /api/v1/admin/members/{memberId}
```

**Path Variables**

| 이름 | 타입 | 설명 |
|------|------|------|
| memberId | Long | 조회할 회원 ID |

**응답 예시** `200 OK`

```json
{
  "message": "회원 상세 조회가 완료되었습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "nickName": "gildong",
    "level": "BASIC",
    "role": "ROLE_USER"
  }
}
```

**에러**

| 상황 | HTTP | 에러코드 |
|------|------|----------|
| 존재하지 않는 회원 | 404 | `MEMBER_NOT_FOUND` |

---

### 3. 회원 정보 수정

```
PATCH /api/v1/admin/members/{memberId}
```

**Path Variables**

| 이름 | 타입 | 설명 |
|------|------|------|
| memberId | Long | 수정할 회원 ID |

**Request Body**

null인 필드는 수정하지 않습니다 (부분 수정).

```json
{
  "name": "새이름",
  "nickname": "새닉네임",
  "role": "ROLE_ADMIN",
  "level": "INTERMEDIATE"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| name | String | N | 최대 50자 |
| nickname | String | N | 1~50자, 중복 불가 |
| role | MemberRole | N | `ROLE_USER` / `ROLE_ADMIN` |
| level | MemberLevel | N | `BASIC` / `INTERMEDIATE` / `ADVANCED` |

**응답 예시** `200 OK`

```json
{
  "message": "회원 정보 수정이 완료되었습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "새이름",
    "nickName": "새닉네임",
    "level": "INTERMEDIATE",
    "role": "ROLE_ADMIN"
  }
}
```

**에러**

| 상황 | HTTP | 에러코드 |
|------|------|----------|
| 존재하지 않는 회원 | 404 | `MEMBER_NOT_FOUND` |
| 닉네임 중복 | 409 | `USERNAME_DUPLICATE` |
