# API 명세서

이 문서는 백엔드 API의 명세서입니다. 프론트엔드 개발 시 참고용으로 사용됩니다.

## 공통 응답 포맷

모든 API는 아래와 같은 형식의 JSON을 반환합니다.

```json
{
  "message": "string (API 호출 결과 메시지)",
  "data": "object (API 결과 데이터)"
}
```

-   성공 시 `data` 필드에 요청한 데이터가 포함됩니다.
-   실패 시 `data` 필드는 `null`이거나 오류에 대한 추가 정보가 포함될 수 있습니다.

---

## 1. 인증 (Auth)

인증 관련 API입니다.

### 1.1. 로그인

-   **Description**: 이메일과 비밀번호로 로그인합니다. 성공 시 `access-token`과 `refresh-token`이 쿠키에 설정됩니다.
-   **URL**: `POST /api/v1/auth/login`
-   **Auth**: `permitAll`
-   **Request Body**: `LoginRequest`
    ```json
    {
      "email": "string (email format)",
      "password": "string"
    }
    ```
-   **Response Body**: `ApiResponse<MemberInfo>`
    ```json
    {
      "message": "로그인이 성공적으로 완료되었습니다.",
      "data": {
        "id": "long",
        "email": "string",
        "name": "string",
        "nickName": "string",
        "level": "string (e.g., BASIC, INTERMEDIATE, ADVANCED)",
        "role": "string (e.g., ROLE_USER, ROLE_ADMIN)"
      }
    }
    ```

### 1.2. 토큰 재발급

-   **Description**: 쿠키에 담긴 `refresh-token`을 이용해 `access-token`과 `refresh-token`을 재발급(갱신)합니다.
-   **URL**: `POST /api/v1/auth/refresh`
-   **Auth**: `permitAll`
-   **Request Body**: (없음)
-   **Response Body**: `ApiResponse<Void>`
    ```json
    {
      "message": "토큰이 재발급되었습니다.",
      "data": null
    }
    ```

### 1.3. 로그아웃

-   **Description**: 서버에 저장된 `refresh-token`을 삭제하고 클라이언트의 토큰 쿠키를 만료시킵니다.
-   **URL**: `POST /api/v1/auth/logout`
-   **Auth**: `permitAll`
-   **Request Body**: (없음)
-   **Response Body**: `ApiResponse<Void>`
    ```json
    {
      "message": "로그아웃이 완료되었습니다.",
      "data": null
    }
    ```

---

## 2. 회원 (Member)

회원 관련 API입니다.

### 2.1. 회원가입

-   **Description**: 새로운 회원을 등록합니다.
-   **URL**: `POST /api/v1/members/signup`
-   **Auth**: `permitAll`
-   **Request Body**: `SignupRequest`
    ```json
    {
      "email": "string (email format, max 100)",
      "password": "string (min 8, max 30)",
      "name": "string (max 50)",
      "nickname": "string (min 3, max 50)"
    }
    ```
-   **Response Body**: `ApiResponse<Void>`
    ```json
    {
      "message": "회원가입이 성공적으로 완료되었습니다.",
      "data": null
    }
    ```

### 2.2. 내 정보 조회

-   **Description**: 현재 로그인한 사용자의 정보를 조회합니다.
-   **URL**: `GET /api/v1/members/me`
-   **Auth**: `Authenticated`
-   **Request Body**: (없음)
-   **Response Body**: `ApiResponse<MemberInfo>` (1.1. 로그인 응답 data와 동일)

### 2.3. 특정 회원 정보 조회

-   **Description**: `memberId`에 해당하는 특정 회원의 정보를 조회합니다.
-   **URL**: `GET /api/v1/members/{memberId}`
-   **Auth**: `Authenticated`
-   **Request Body**: (없음)
-   **Response Body**: `ApiResponse<MemberInfo>` (1.1. 로그인 응답 data와 동일)

### 2.4. 내 정보 수정

-   **Description**: 현재 로그인한 사용자의 이름 또는 닉네임을 수정합니다.
-   **URL**: `PATCH /api/v1/members/me`
-   **Auth**: `Authenticated`
-   **Request Body**: `MemberUpdateRequest`
    ```json
    {
      "name": "string (optional, max 50)",
      "nickname": "string (optional, min 3, max 50)"
    }
    ```
-   **Response Body**: `ApiResponse<MemberInfo>` (수정된 정보 반환)

### 2.5. 내 비밀번호 수정

-   **Description**: 현재 로그인한 사용자의 비밀번호를 수정합니다.
-   **URL**: `PATCH /api/v1/members/me/password`
-   **Auth**: `Authenticated`
-   **Request Body**: `PasswordUpdateRequest`
    ```json
    {
      "currentPassword": "string (min 8, max 30)",
      "newPassword": "string (min 8, max 30)"
    }
    ```
-   **Response Body**: `ApiResponse<Void>`

---

## 3. 스터디 (Study)

스터디 그룹(강의 대분류) 관련 API입니다.

### 3.1. 스터디 생성

-   **Description**: 새로운 스터디를 생성합니다.
-   **URL**: `POST /api/v1/studies`
-   **Auth**: `ADMIN`
-   **Request Body**: `StudyRequest`
    ```json
    {
      "name": "string (max 100)",
      "description": "string (max 500)",
      "level": "string (BASIC, INTERMEDIATE, ADVANCED)",
      "type": "string (BACKEND, ALGORITHM, COMPUTER_SCIENCE)"
    }
    ```
-   **Response Body**: `ApiResponse<StudyResponse>`
    ```json
    {
      "message": "스터디가 성공적으로 생성되었습니다.",
      "data": {
        "id": "long",
        "name": "string",
        "description": "string",
        "level": "string",
        "type": "string"
      }
    }
    ```

### 3.2. 타입 기반 스터디 목록 조회

-   **Description**: `studyType`에 따라 스터디 목록을 페이지네이션으로 조회합니다.
-   **URL**: `GET /api/v1/studies?studyType={studyType}&page={page}&size={size}`
-   **Auth**: `Authenticated`
-   **Request Params**:
    -   `studyType`: `string` (BACKEND, ALGORITHM, COMPUTER_SCIENCE)
    -   `page`: `int` (default: 0)
    -   `size`: `int` (default: 10)
-   **Response Body**: `ApiResponse<Page<StudyResponse>>` (Spring의 Page 객체 형식)

### 3.3. 특정 스터디 조회

-   **Description**: `studyId`로 특정 스터디 정보를 조회합니다.
-   **URL**: `GET /api/v1/studies/{studyId}`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<StudyResponse>` (3.1. 응답 data와 동일)

### 3.4. 스터디 수정

-   **Description**: `studyId`에 해당하는 스터디 정보를 수정합니다.
-   **URL**: `PUT /api/v1/studies/{studyId}`
-   **Auth**: `ADMIN`
-   **Request Body**: `StudyRequest` (3.1. 요청 Body와 동일)
-   **Response Body**: `ApiResponse<StudyResponse>` (수정된 정보 반환)

### 3.5. 스터디 삭제

-   **Description**: `studyId`에 해당하는 스터디를 삭제합니다.
-   **URL**: `DELETE /api/v1/studies/{studyId}`
-   **Auth**: `ADMIN`
-   **Response Body**: `ApiResponse<Void>`

---

## 4. 커리큘럼 (Curriculum)

커리큘럼(강의 중분류) 관련 API입니다.

### 4.1. 커리큘럼 생성

-   **Description**: 특정 스터디(`studyId`)에 새로운 커리큘럼을 생성합니다.
-   **URL**: `POST /api/v1/studies/{studyId}/curriculums`
-   **Auth**: `ADMIN`
-   **Request Body**: `CurriculumRequest`
    ```json
    {
      "name": "string",
      "description": "string",
      "order": "integer"
    }
    ```
-   **Response Body**: `ApiResponse<CurriculumResponse>`
    ```json
    {
      "message": "커리큘럼이 성공적으로 생성되었습니다.",
      "data": {
        "id": "long",
        "title": "string",
        "description": "string",
        "order": "integer",
        "postsCount": "integer"
      }
    }
    ```

### 4.2. 스터디별 커리큘럼 목록 조회

-   **Description**: 특정 스터디(`studyId`)의 커리큘럼 목록을 페이지네이션으로 조회합니다.
-   **URL**: `GET /api/v1/studies/{studyId}/curriculums?page={page}&size={size}&sort={sort}`
-   **Auth**: `Authenticated`
-   **Request Params**:
    -   `page`: `int` (default: 0)
    -   `size`: `int` (default: 10)
    -   `sort`: `string` (default: `orderInStudy,asc`)
-   **Response Body**: `ApiResponse<Page<CurriculumResponse>>`
    ```json
    {
      "message": "커리큘럼 목록이 성공적으로 조회되었습니다.",
      "data": {
        "content": [
          {
            "id": "long",
            "title": "string",
            "description": "string",
            "order": "integer",
            "postsCount": "integer"
          }
        ],
        "totalElements": "long",
        "totalPages": "integer",
        "size": "integer",
        "number": "integer"
      }
    }
    ```

### 4.3. 특정 커리큘럼 조회

-   **Description**: `curriculumId`로 특정 커리큘럼 정보를 조회합니다.
-   **URL**: `GET /api/v1/curriculums/{curriculumId}`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<CurriculumResponse>` (4.1. 응답 data와 동일)

### 4.4. 커리큘럼 수정

-   **Description**: `curriculumId`에 해당하는 커리큘럼 정보를 수정합니다.
-   **URL**: `PUT /api/v1/curriculums/{curriculumId}`
-   **Auth**: `ADMIN`
-   **Request Body**: `CurriculumRequest` (4.1. 요청 Body와 동일)
-   **Response Body**: `ApiResponse<CurriculumResponse>` (수정된 정보 반환)

### 4.5. 커리큘럼 삭제

-   **Description**: `curriculumId`에 해당하는 커리큘럼을 삭제합니다.
-   **URL**: `DELETE /api/v1/curriculums/{curriculumId}`
-   **Auth**: `ADMIN`
-   **Response Body**: `ApiResponse<Void>`

---

## 5. 게시글 (Post)

게시글(강의 소분류) 관련 API입니다.

### 5.1. 커리큘럼별 게시글 목록 조회

-   **Description**: 특정 커리큘럼(`curriculumId`)의 게시글 목록을 `orderInCurriculum` 오름차순으로 전체 조회합니다. (사이드 네비게이션 용도)
-   **URL**: `GET /api/v1/curriculums/{curriculumId}/posts`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<List<PostSimpleResponse>>`
    ```json
    {
      "message": "게시글 목록이 성공적으로 조회되었습니다.",
      "data": [
        {
          "postId": "long",
          "title": "string",
          "authorId": "long",
          "authorName": "string",
          "curriculumId": "long",
          "orderInCurriculum": "integer"
        }
      ]
    }
    ```

### 5.2. 게시글 생성

-   **Description**: 특정 커리큘럼(`curriculumId`)에 새로운 게시글을 생성합니다.
-   **URL**: `POST /api/v1/curriculums/{curriculumId}/posts`
-   **Auth**: `ADMIN`
-   **Request Body**: `PostRequest`
    ```json
    {
      "title": "string (max 100)",
      "content": "string (markdown)"
    }
    ```
-   **Response Body**: `ApiResponse<PostSimpleResponse>`
    ```json
    {
      "message": "게시글이 성공적으로 생성되었습니다.",
      "data": {
        "postId": "long",
        "title": "string",
        "authorId": "long",
        "authorName": "string",
        "curriculumId": "long",
        "orderInCurriculum": "integer"
      }
    }
    ```

### 5.3. 특정 게시글 조회

-   **Description**: `postId`로 특정 게시글의 상세 정보를 조회합니다.
-   **URL**: `GET /api/v1/posts/{postId}`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<PostDetailResponse>`
    ```json
    {
      "message": "게시글이 성공적으로 조회되었습니다.",
      "data": {
        "postId": "long",
        "title": "string",
        "content": "string (markdown)",
        "authorId": "long",
        "authorName": "string",
        "curriculumId": "long",
        "orderInCurriculum": "integer"
      }
    }
    ```

### 5.4. 게시글 수정

-   **Description**: `postId`에 해당하는 게시글을 수정합니다.
-   **URL**: `PUT /api/v1/posts/{postId}`
-   **Auth**: `ADMIN`
-   **Request Body**: `PostRequest` (5.2. 요청 Body와 동일)
-   **Response Body**: `ApiResponse<PostSimpleResponse>` (수정된 정보 반환, 5.2. 응답 data와 동일)

### 5.5. 게시글 삭제

-   **Description**: `postId`에 해당하는 게시글을 삭제합니다.
-   **URL**: `DELETE /api/v1/posts/{postId}`
-   **Auth**: `ADMIN`
-   **Response Body**: `ApiResponse<Void>`

---

## 6. 댓글 (Comment)

게시글 댓글 및 대댓글 관련 API입니다. 댓글의 depth는 최대 2단계(댓글 → 대댓글)로 제한됩니다.

### 6.1. 댓글 작성

-   **Description**: 특정 게시글(`postId`)에 댓글을 작성합니다.
-   **URL**: `POST /api/v1/posts/{postId}/comments`
-   **Auth**: `Authenticated`
-   **Request Body**: `CommentRequest`
    ```json
    {
      "content": "string (not blank)"
    }
    ```
-   **Response Body**: `ApiResponse<Void>`
    ```json
    {
      "message": "댓글이 성공적으로 생성되었습니다.",
      "data": null
    }
    ```

### 6.2. 게시글 댓글 목록 조회

-   **Description**: 특정 게시글(`postId`)의 최상위 댓글 목록을 페이지네이션으로 조회합니다.
-   **URL**: `GET /api/v1/posts/{postId}/comments?page={page}&size={size}`
-   **Auth**: `Authenticated`
-   **Request Params**:
    -   `page`: `int` (default: 0)
    -   `size`: `int` (default: 10)
-   **Response Body**: `ApiResponse<Page<CommentResponse>>`
    ```json
    {
      "message": "댓글 목록이 성공적으로 조회되었습니다.",
      "data": {
        "content": [
          {
            "commentId": "long",
            "content": "string",
            "authorId": "long",
            "authorName": "string",
            "postId": "long",
            "reCommentCount": "long",
            "createdAt": "datetime",
            "updatedAt": "datetime"
          }
        ],
        "totalElements": "long",
        "totalPages": "integer",
        "size": "integer",
        "number": "integer"
      }
    }
    ```

### 6.3. 댓글 수정

-   **Description**: 특정 댓글(`commentId`)을 수정합니다. 작성자 본인만 수정 가능합니다.
-   **URL**: `PATCH /api/v1/comments/{commentId}`
-   **Auth**: `Authenticated`
-   **Request Body**: `CommentRequest` (6.1. 요청 Body와 동일)
-   **Response Body**: `ApiResponse<CommentResponse>`
    ```json
    {
      "message": "댓글이 성공적으로 수정되었습니다.",
      "data": {
        "commentId": "long",
        "content": "string",
        "authorId": "long",
        "authorName": "string",
        "postId": "long",
        "reCommentCount": "long",
        "createdAt": "datetime",
        "updatedAt": "datetime"
      }
    }
    ```

### 6.4. 댓글 삭제

-   **Description**: 특정 댓글(`commentId`)을 삭제합니다. 작성자 본인만 삭제 가능하며, 해당 댓글의 대댓글도 함께 삭제됩니다.
-   **URL**: `DELETE /api/v1/comments/{commentId}`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<Void>`
    ```json
    {
      "message": "댓글이 성공적으로 삭제되었습니다.",
      "data": null
    }
    ```

### 6.5. 대댓글 작성

-   **Description**: 특정 댓글(`commentId`)에 대댓글을 작성합니다. 대댓글에는 추가 대댓글을 작성할 수 없습니다.
-   **URL**: `POST /api/v1/comments/{commentId}/recomments`
-   **Auth**: `Authenticated`
-   **Request Body**: `CommentRequest` (6.1. 요청 Body와 동일)
-   **Response Body**: `ApiResponse<Void>`
    ```json
    {
      "message": "대댓글이 성공적으로 생성되었습니다.",
      "data": null
    }
    ```

### 6.6. 대댓글 목록 조회

-   **Description**: 특정 댓글(`commentId`)의 대댓글 목록을 페이지네이션으로 조회합니다.
-   **URL**: `GET /api/v1/comments/{commentId}/recomments?page={page}&size={size}`
-   **Auth**: `Authenticated`
-   **Request Params**:
    -   `page`: `int` (default: 0)
    -   `size`: `int` (default: 10)
-   **Response Body**: `ApiResponse<Page<ReCommentResponse>>`
    ```json
    {
      "message": "대댓글 목록이 성공적으로 조회되었습니다.",
      "data": {
        "content": [
          {
            "reCommentId": "long",
            "content": "string",
            "authorId": "long",
            "authorName": "string",
            "parentCommentId": "long",
            "createdAt": "datetime",
            "updatedAt": "datetime"
          }
        ],
        "totalElements": "long",
        "totalPages": "integer",
        "size": "integer",
        "number": "integer"
      }
    }
    ```

### 6.7. 대댓글 수정

-   **Description**: 특정 대댓글(`reCommentId`)을 수정합니다. 작성자 본인만 수정 가능합니다.
-   **URL**: `PATCH /api/v1/recomments/{reCommentId}`
-   **Auth**: `Authenticated`
-   **Request Body**: `CommentRequest` (6.1. 요청 Body와 동일)
-   **Response Body**: `ApiResponse<ReCommentResponse>`
    ```json
    {
      "message": "대댓글이 성공적으로 수정되었습니다.",
      "data": {
        "reCommentId": "long",
        "content": "string",
        "authorId": "long",
        "authorName": "string",
        "parentCommentId": "long",
        "createdAt": "datetime",
        "updatedAt": "datetime"
      }
    }
    ```

### 6.8. 대댓글 삭제

-   **Description**: 특정 대댓글(`reCommentId`)을 삭제합니다. 작성자 본인만 삭제 가능합니다.
-   **URL**: `DELETE /api/v1/recomments/{reCommentId}`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<Void>`
    ```json
    {
      "message": "대댓글이 성공적으로 삭제되었습니다.",
      "data": null
    }
    ```

---

## 7. 공지사항 (Notice)

공지사항 관련 API입니다. 공지사항이 생성되면 전체 회원에게 SSE 알림이 자동 발송됩니다.

### 7.1. 공지사항 목록 조회

-   **Description**: 공지사항 목록을 페이지네이션으로 조회합니다.
-   **URL**: `GET /api/v1/notices?page={page}&size={size}`
-   **Auth**: `Authenticated`
-   **Request Params**:
    -   `page`: `int`
    -   `size`: `int`
-   **Response Body**: `ApiResponse<Page<NoticeResponse>>`
    -   `NoticeResponse`
        ```json
        {
          "id": "long",
          "title": "string",
          "content": "string",
          "createdAt": "datetime",
          "updatedAt": "datetime"
        }
        ```

### 7.2. 특정 공지사항 조회

-   **Description**: `noticeId`로 특정 공지사항을 조회합니다.
-   **URL**: `GET /api/v1/notices/{noticeId}`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<NoticeResponse>` (7.1.의 `NoticeResponse`와 동일)

### 7.3. 공지사항 생성

-   **Description**: 새로운 공지사항을 생성합니다. 생성 시 전체 회원에게 `"공지를 확인해주세요!"` 알림이 자동 발송됩니다.
-   **URL**: `POST /api/v1/notices`
-   **Auth**: `ADMIN`
-   **Request Body**: `NoticeRequest`
    ```json
    {
      "title": "string",
      "content": "string"
    }
    ```
-   **Response Body**: `ApiResponse<Void>`

### 7.4. 공지사항 수정

-   **Description**: `noticeId`에 해당하는 공지사항을 수정합니다.
-   **URL**: `PUT /api/v1/notices/{noticeId}`
-   **Auth**: `ADMIN`
-   **Request Body**: `NoticeRequest` (7.3. 요청 Body와 동일)
-   **Response Body**: `ApiResponse<NoticeResponse>` (수정된 정보 반환)

### 7.5. 공지사항 삭제

-   **Description**: `noticeId`에 해당하는 공지사항을 삭제합니다.
-   **URL**: `DELETE /api/v1/notices/{noticeId}`
-   **Auth**: `ADMIN`
-   **Response Body**: `ApiResponse<Void>`

---

## 8. 이미지 (Image)

이미지 업로드/다운로드 관련 API입니다. (Presigned URL 방식)

### 8.1. 이미지 업로드 URL 생성

-   **Description**: 게시글(`postId`)에 이미지를 업로드하기 위한 Presigned URL을 발급받습니다.
-   **URL**: `POST /api/v1/posts/{postId}/images/presigned-url`
-   **Auth**: `Authenticated`
-   **Request Body**: `ImageRequest`
    ```json
    {
      "contentType": "string (e.g., image/png)",
      "contentLength": "long (byte)",
      "fileName": "string"
    }
    ```
-   **Response Body**: `ApiResponse<ImageResponse>`
    ```json
    {
      "message": "이미지 업로드 URL이 발급되었습니다.",
      "data": {
        "imageUrl": "string (Presigned URL for PUT)",
        "imageKey": "string (S3 object key)"
      }
    }
    ```

### 8.2. 이미지 업로드 완료 처리

-   **Description**: Presigned URL을 통해 S3에 이미지 업로드를 완료한 후, 서버에 업로드 완료를 알립니다.
-   **URL**: `PATCH /api/v1/images/{imageId}/complete`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<Void>`

### 8.3. 특정 이미지 조회 (다운로드 URL)

-   **Description**: `imageId`에 해당하는 이미지의 Presigned URL(다운로드용)을 조회합니다.
-   **URL**: `GET /api/v1/images/{imageId}`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<ImageResponse>`
    ```json
    {
      "message": "이미지가 성공적으로 조회되었습니다.",
      "data": {
        "imageUrl": "string (Presigned URL for GET)",
        "imageKey": "string (S3 object key)"
      }
    }
    ```

### 8.4. 게시글의 모든 이미지 조회

-   **Description**: 특정 게시글(`postId`)에 포함된 모든 이미지의 다운로드 URL을 조회합니다.
-   **URL**: `GET /api/v1/posts/{postId}/images`
-   **Auth**: `Authenticated`
-   **Response Body**: `ApiResponse<List<ImageResponse>>`

---

## 9. 알림 (Notification)

SSE(Server-Sent Events) 기반 실시간 알림 관련 API입니다. 알림은 전체 발송 방식으로만 운영되며, 읽음 여부는 회원별로 개별 관리됩니다.

### 9.1. SSE 구독

-   **Description**: SSE 커넥션을 맺어 서버로부터 실시간 알림을 수신합니다. 연결 즉시 `connect` 이벤트가 전송되며, 60초마다 `ping` 이벤트로 연결을 유지합니다.
-   **URL**: `GET /api/v1/notifications/subscribe`
-   **Auth**: `Authenticated`
-   **Request Headers**: `Accept: text/event-stream`
-   **Response**: `text/event-stream`
    ```
    event: connect
    data: connected

    event: ping
    data: (empty)

    event: notification
    data: { "id": long, "content": "string", "isRead": false, "createdAt": "datetime" }
    ```
-   **비고**: 동일 사용자의 동시 구독은 최대 5개까지 허용되며, 초과 시 가장 오래된 연결이 제거됩니다.

### 9.2. 알림 목록 조회

-   **Description**: 전체 알림 목록을 페이지네이션으로 조회합니다. 각 알림의 `isRead` 필드는 현재 로그인한 회원의 읽음 여부를 반영합니다.
-   **URL**: `GET /api/v1/notifications?page={page}&size={size}&sort={sort}`
-   **Auth**: `Authenticated`
-   **Request Params**:
    -   `page`: `int` (default: 0)
    -   `size`: `int` (default: 20)
    -   `sort`: `string` (default: `createdAt,desc`)
-   **Response Body**: `ApiResponse<Page<NotificationResponse>>`
    ```json
    {
      "message": "알림 목록이 성공적으로 조회되었습니다.",
      "data": {
        "content": [
          {
            "id": "long",
            "content": "string",
            "isRead": "boolean",
            "createdAt": "datetime"
          }
        ],
        "totalElements": "long",
        "totalPages": "integer",
        "size": "integer",
        "number": "integer"
      }
    }
    ```

### 9.3. 알림 읽음 처리

-   **Description**: 특정 알림을 읽음 상태로 변경합니다. 이미 읽은 알림에 대해 재요청 시 무시됩니다(멱등).
-   **URL**: `PATCH /api/v1/notifications/{notificationId}/read`
-   **Auth**: `Authenticated`
-   **Path Variables**:
    -   `notificationId`: `long`
-   **Request Body**: (없음)
-   **Response Body**: `ApiResponse<Void>`
    ```json
    {
      "message": "알림이 성공적으로 읽음 처리되었습니다.",
      "data": null
    }
    ```
