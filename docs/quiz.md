# 퀴즈 기능 설계 문서

## 개요

Post 콘텐츠를 기반으로 Gemini AI가 자동으로 퀴즈를 생성하는 기능.

- 관리자가 `postId`로 생성 요청 → Gemini AI가 10문제 × 5지선다 생성 → DB 저장 (재생성 불가)
- 사용자는 정답이 숨겨진 문제를 풀고 제출 → 채점 결과 반환
- **합격 기준**: 10문제 중 7문제 이상 정답 (6문제 이하 → 재시험, 횟수 제한 없음)
- 퀴즈 페이지 진입 시: 시도 이력이 있으면 최근 결과 표시, 없으면 문제 표시

---

## 기술 스택 추가

| 항목 | 내용 |
|---|---|
| AI | Google Gemini (`gemini-2.0-flash`) |
| AI 라이브러리 | Spring AI 1.0.0 (`spring-ai-google-genai-spring-boot-starter`) |
| 환경 변수 | `GEMINI_API_KEY` 추가 필요 |

### build.gradle 변경

```groovy
implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0"))
implementation 'org.springframework.ai:spring-ai-google-genai-spring-boot-starter'
```

### application.yml 변경

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
        chat:
          options:
            model: gemini-2.0-flash
            temperature: 0.3
```

---

## 도메인 계층 구조

```
Post
  └── Quiz (1:1, 한 번 생성 후 고정)
        ├── QuizQuestion (1:N, 10개)
        │     └── QuizOption (1:N, 5개 고정)  ← isCorrect는 DB에만, 조회 응답에 미포함
        └── QuizAttempt (1:N, 시도 이력 — 횟수 제한 없음)
              └── QuizAttemptAnswer (1:N, 문제별 제출 답안)
```

---

## DB 스키마

### V10__create_quiz_tables.sql

```sql
CREATE TABLE quiz
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uq_quiz_post_id UNIQUE (post_id),
    CONSTRAINT FK_QUIZ_ON_POST FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE
);

CREATE TABLE quiz_question
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id        BIGINT NOT NULL,
    question       TEXT   NOT NULL,
    question_order INT    NOT NULL,
    CONSTRAINT FK_QUIZ_QUESTION_ON_QUIZ FOREIGN KEY (quiz_id) REFERENCES quiz (id) ON DELETE CASCADE
);

CREATE TABLE quiz_option
(
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    quiz_question_id BIGINT       NOT NULL,
    content          VARCHAR(500) NOT NULL,
    is_correct       TINYINT(1)   NOT NULL DEFAULT 0,
    option_order     INT          NOT NULL,
    CONSTRAINT FK_QUIZ_OPTION_ON_QUESTION FOREIGN KEY (quiz_question_id) REFERENCES quiz_question (id) ON DELETE CASCADE
);

CREATE TABLE quiz_attempt
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id    BIGINT     NOT NULL,
    member_id  BIGINT     NOT NULL,
    score      INT        NOT NULL,
    passed     TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME   NOT NULL,
    CONSTRAINT FK_QUIZ_ATTEMPT_ON_QUIZ   FOREIGN KEY (quiz_id)   REFERENCES quiz (id) ON DELETE CASCADE,
    CONSTRAINT FK_QUIZ_ATTEMPT_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE quiz_attempt_answer
(
    id               BIGINT     AUTO_INCREMENT PRIMARY KEY,
    quiz_attempt_id  BIGINT     NOT NULL,
    quiz_question_id BIGINT     NOT NULL,
    quiz_option_id   BIGINT     NOT NULL,
    is_correct       TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT FK_ATTEMPT_ANSWER_ON_ATTEMPT  FOREIGN KEY (quiz_attempt_id)  REFERENCES quiz_attempt (id) ON DELETE CASCADE,
    CONSTRAINT FK_ATTEMPT_ANSWER_ON_QUESTION FOREIGN KEY (quiz_question_id) REFERENCES quiz_question (id),
    CONSTRAINT FK_ATTEMPT_ANSWER_ON_OPTION   FOREIGN KEY (quiz_option_id)   REFERENCES quiz_option (id)
);
```

**설계 포인트**
- `quiz.post_id` UNIQUE → Post당 퀴즈 1개 강제, 재생성 불가
- `quiz_attempt`는 시도 이력 전부 보관 (재시험 횟수 제한 없음)
- `quiz_attempt_answer.is_correct`는 제출 시점에 채점해서 저장 (옵션 데이터 변경 방어)
- Post 삭제 시 Quiz → QuizQuestion → QuizOption → QuizAttempt → QuizAttemptAnswer 연쇄 삭제

---

## 패키지 구조

```
domain/edu/quiz/
├── entity/
│   ├── Quiz.java
│   ├── QuizQuestion.java
│   ├── QuizOption.java
│   ├── QuizAttempt.java
│   └── QuizAttemptAnswer.java
├── repository/
│   ├── QuizRepository.java
│   └── QuizAttemptRepository.java
├── service/
│   ├── dto/
│   │   └── QuizGenerationResult.java   ← BeanOutputConverter 타겟
│   └── QuizService.java
└── controller/
    ├── dto/
    │   ├── request/
    │   │   └── QuizSubmitRequest.java
    │   └── response/
    │       ├── QuizResponse.java
    │       ├── QuizQuestionResponse.java
    │       ├── QuizOptionResponse.java
    │       ├── QuizResultResponse.java
    │       └── QuizAttemptAnswerResponse.java
    └── QuizController.java

global/ai/
└── GeminiConfig.java
```

---

## 엔티티 설계

### Quiz

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `Long` | PK |
| `post` | `Post` | `@OneToOne(LAZY)`, unique FK |
| `questions` | `List<QuizQuestion>` | `@OneToMany(CascadeType.ALL, orphanRemoval=true)` |
| `createdAt` | `LocalDateTime` | `@CreatedDate` |
| `updatedAt` | `LocalDateTime` | `@LastModifiedDate` |

### QuizQuestion

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `Long` | PK |
| `quiz` | `Quiz` | `@ManyToOne(LAZY)` |
| `question` | `String` | TEXT |
| `questionOrder` | `int` | 문제 순서 (1~10) |
| `options` | `List<QuizOption>` | `@OneToMany(CascadeType.ALL, orphanRemoval=true)` |

### QuizOption

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `Long` | PK |
| `quizQuestion` | `QuizQuestion` | `@ManyToOne(LAZY)` |
| `content` | `String` | VARCHAR(500) |
| `isCorrect` | `boolean` | 정답 여부 (API 응답에 미포함) |
| `optionOrder` | `int` | 보기 순서 (1~5) |

### QuizAttempt

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `Long` | PK |
| `quiz` | `Quiz` | `@ManyToOne(LAZY)` |
| `member` | `Member` | `@ManyToOne(LAZY)` |
| `score` | `int` | 정답 수 (0~10) |
| `passed` | `boolean` | 합격 여부 (score >= 7) |
| `createdAt` | `LocalDateTime` | `@CreatedDate` |
| `answers` | `List<QuizAttemptAnswer>` | `@OneToMany(CascadeType.ALL)` |

### QuizAttemptAnswer

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `Long` | PK |
| `quizAttempt` | `QuizAttempt` | `@ManyToOne(LAZY)` |
| `quizQuestion` | `QuizQuestion` | `@ManyToOne(LAZY)` |
| `quizOption` | `QuizOption` | `@ManyToOne(LAZY)` — 제출한 보기 |
| `isCorrect` | `boolean` | 채점 결과 |

모든 엔티티 공통: `@NoArgsConstructor(access = PROTECTED)`, `@Builder(id 제외)`, `@Getter`

---

## API 명세

### 프론트엔드 진입 흐름

```
퀴즈 페이지 진입
  → GET /api/v1/posts/{postId}/quiz/attempts/me
      ├── 200 OK  : 최근 시도 결과 표시 (재시험 버튼 포함)
      └── 404     : GET /api/v1/posts/{postId}/quiz 호출 → 문제 표시
```

### 1. 퀴즈 생성 (관리자 전용)

```
POST /api/v1/posts/{postId}/quiz/generate
권한: ADMIN
```

- 이미 퀴즈가 존재하면 `QUIZ_ALREADY_EXISTS` 에러 (재생성 불가)
- Gemini AI 호출 → DB 저장

**Response (201 Created)**
```json
{
  "message": "퀴즈가 성공적으로 생성되었습니다.",
  "data": {
    "quizId": 1,
    "postId": 42,
    "createdAt": "2026-03-15T10:00:00",
    "questions": [
      {
        "questionId": 1,
        "questionOrder": 1,
        "question": "문제 내용",
        "options": [
          { "optionId": 1, "optionOrder": 1, "content": "보기 1" },
          { "optionId": 2, "optionOrder": 2, "content": "보기 2" },
          { "optionId": 3, "optionOrder": 3, "content": "보기 3" },
          { "optionId": 4, "optionOrder": 4, "content": "보기 4" },
          { "optionId": 5, "optionOrder": 5, "content": "보기 5" }
        ]
      }
    ]
  }
}
```

### 2. 퀴즈 조회

```
GET /api/v1/posts/{postId}/quiz
권한: 인증된 사용자
```

`isCorrect` 미포함. 생성 응답과 동일 구조.

### 3. 내 최근 시도 결과 조회

```
GET /api/v1/posts/{postId}/quiz/attempts/me
권한: 인증된 사용자
```

- 시도 이력 없으면 `404 QUIZ_ATTEMPT_NOT_FOUND` → 프론트가 문제 페이지로 전환
- 시도 이력 있으면 가장 최근 시도 결과 반환

**Response (200 OK)**
```json
{
  "message": "퀴즈 시도 이력을 조회했습니다.",
  "data": {
    "attemptId": 5,
    "score": 6,
    "totalQuestions": 10,
    "passed": false,
    "createdAt": "2026-03-15T10:30:00",
    "results": [
      {
        "questionId": 1,
        "question": "문제 내용",
        "selectedOptionId": 3,
        "correctOptionId": 2,
        "correct": false
      }
    ]
  }
}
```

### 4. 퀴즈 제출 및 채점

```
POST /api/v1/posts/{postId}/quiz/submit
권한: 인증된 사용자
```

**Request Body**
```json
{
  "answers": [
    { "questionId": 1, "selectedOptionId": 3 },
    { "questionId": 2, "selectedOptionId": 7 }
  ]
}
```

**백엔드 검증 규칙**
- 제출 답안 수 != 퀴즈 문제 수 → `QUIZ_INVALID_SUBMISSION`
- 존재하지 않는 questionId 또는 해당 문제에 속하지 않는 optionId → `QUIZ_INVALID_SUBMISSION`

**Response (200 OK)** — 내 최근 시도 결과 조회와 동일 구조

---

## Spring AI 통합 설계

### QuizGenerationResult (AI 응답 파싱용 record)

```java
public record QuizGenerationResult(List<QuestionDto> questions) {
    public record QuestionDto(String question, List<OptionDto> options) {}
    public record OptionDto(String content, boolean isCorrect) {}
}
```

### 서비스 핵심 흐름

**생성 (`generateQuiz`)**
```java
// 이미 퀴즈 존재하면 에러
if (quizRepository.existsByPost(post)) {
    throw new CustomException(ErrorCode.QUIZ_ALREADY_EXISTS);
}

BeanOutputConverter<QuizGenerationResult> converter = new BeanOutputConverter<>(QuizGenerationResult.class);
String rawJson = chatClient.prompt()
    .user(buildPrompt(post, converter.getFormat()))
    .call()
    .content();
QuizGenerationResult result = converter.convert(rawJson);
// 엔티티 조립 → quizRepository.save(quiz)
```

**제출 (`submitQuiz`)**
```java
// 1. 답안 수 검증: answers.size() != quiz.getQuestions().size() → QUIZ_INVALID_SUBMISSION
// 2. 각 questionId가 해당 퀴즈 소속인지, selectedOptionId가 해당 문제 소속인지 검증
// 3. quizOption.isCorrect()로 채점, score 집계
// 4. passed = (score >= 7)
// 5. QuizAttempt + QuizAttemptAnswer 저장
// 6. QuizResultResponse 반환
```

**최근 시도 조회 (`getMyLatestAttempt`)**
```java
// QuizAttemptRepository.findTopByQuizAndMemberOrderByCreatedAtDesc(quiz, member)
//   → Optional.empty() 시 QUIZ_ATTEMPT_NOT_FOUND
```

### AI 프롬프트 요구사항

```
- 총 10개 문제
- 각 문제 정확히 5개 보기
- 문제당 isCorrect: true인 보기 반드시 1개
- 게시글 내용 기반, 게시글 언어(한국어/영어) 따름
```

---

## 에러 코드 추가

| 코드 | HTTP Status | 메시지 |
|---|---|---|
| `QUIZ_NOT_FOUND` | 404 | 퀴즈가 존재하지 않습니다. |
| `QUIZ_ALREADY_EXISTS` | 409 | 이미 생성된 퀴즈가 있습니다. |
| `QUIZ_GENERATION_FAILED` | 500 | 퀴즈 생성에 실패했습니다. 잠시 후 다시 시도해주세요. |
| `QUIZ_INVALID_SUBMISSION` | 400 | 제출한 답안이 유효하지 않습니다. |
| `QUIZ_ATTEMPT_NOT_FOUND` | 404 | 퀴즈 시도 이력이 없습니다. |

---

## 구현 순서

1. `build.gradle` + `application.yml` — Spring AI 의존성 및 Gemini 설정
2. `V10__create_quiz_tables.sql` — 5개 테이블 Flyway 마이그레이션
3. `ErrorCode.java` — 에러 코드 5개 추가
4. 엔티티 5개 (`Quiz`, `QuizQuestion`, `QuizOption`, `QuizAttempt`, `QuizAttemptAnswer`)
5. `QuizRepository`, `QuizAttemptRepository`
6. `GeminiConfig` + `QuizGenerationResult`
7. `QuizService` (생성 / 조회 / 제출·채점 / 최근 시도 조회)
8. Request/Response DTOs + `QuizController`