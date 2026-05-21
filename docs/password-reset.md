# 비밀번호 재설정 (이메일 인증) 설계

## 1. 목적

자체 가입(이메일 + 비밀번호) 회원이 비밀번호를 잊었을 때, 가입 이메일로 발송된 인증 링크를 통해 비밀번호를 재설정할 수 있도록 한다.

## 2. 결정 사항 요약

| 항목 | 결정 | 비고 |
|------|------|------|
| 메일 발송 방식 | Gmail SMTP | `spring-boot-starter-mail` |
| 토큰 저장소 | Redis | 기존 RefreshToken과 동일 패턴 |
| 토큰 만료 시간 | 30분 | |
| 재설정 플로우 | 메일 링크 → 새 비밀번호 페이지 → 확정 | 2 step API |
| 응답 통일 | 회원 존재 여부와 무관하게 항상 200 | User Enumeration 방지 |
| 메일 발송 정책 | 실제 회원에게만 발송 | 존재하지 않는 이메일은 발송 안 함 |
| 메일 발송 방식 | 비동기 (`@Async`) | Timing attack 회피 |
| Rate limiting | 이메일 기준 5분에 3회 | Redis 카운터 |
| GitHub OAuth 전용 계정 | 응답 동일, "GitHub 로그인 사용" 안내 메일 발송 | |
| 재설정 성공 후처리 | 모든 RefreshToken 무효화 + 알림 메일 발송 | |
| 새 비밀번호 검증 | 기존 회원가입과 동일 (`@Size(min=8, max=30)`) | |

## 3. API 명세

### 3.1 비밀번호 재설정 요청

```
POST /api/v1/auth/password-reset/request
Content-Type: application/json

Request:
{
  "email": "user@example.com"
}

Response (항상 200, 회원 존재 여부와 무관):
{
  "success": true,
  "message": "입력하신 이메일이 가입된 계정이라면 재설정 메일이 발송됩니다.",
  "data": null
}
```

**동작:**
- 요청 즉시 200 반환 (비동기 처리)
- 내부적으로 다음 조건 만족 시 메일 발송:
  - 회원 존재 + `password != null` → 재설정 링크 메일 발송
  - 회원 존재 + `password == null` (GitHub OAuth 전용) → "GitHub 로그인을 사용하세요" 안내 메일 발송
  - 회원 미존재 → 발송하지 않음
- Rate limit 초과 시: 200 응답은 동일하게 반환하되 메일 발송 스킵 (로그만 기록)

### 3.2 비밀번호 재설정 확정

```
POST /api/v1/auth/password-reset/confirm
Content-Type: application/json

Request:
{
  "token": "a1b2c3d4-...",
  "newPassword": "newPassword123"
}

Response 200:
{
  "success": true,
  "message": "비밀번호가 성공적으로 재설정되었습니다.",
  "data": null
}

Response 400 - 토큰 만료/위조/이미 사용됨:
{
  "success": false,
  "message": "유효하지 않거나 만료된 재설정 토큰입니다.",
  "data": null
}

Response 400 - 비밀번호 검증 실패:
(Bean Validation 기본 메시지)
```

**동작:**
1. Redis에서 토큰 조회 → memberId 추출 (없으면 `INVALID_PASSWORD_RESET_TOKEN`)
2. 토큰을 Redis에서 즉시 삭제 (재사용 방지)
3. 비밀번호 인코딩 후 `Member.password` 업데이트
4. 해당 memberId의 RefreshToken 삭제 (모든 세션 강제 종료)
5. "비밀번호 재설정 알림" 메일 발송 (비동기)

## 4. 데이터 모델

### 4.1 Redis 키 설계

| Key | Value | TTL | 용도 |
|-----|-------|-----|------|
| `pwreset:token:{token}` | `{memberId}` | 30분 | 재설정 토큰 → 사용자 매핑 |
| `pwreset:ratelimit:{email}` | 카운터 | 5분 | Rate limiting |

**토큰 형식**: `UUID v4` (예: `550e8400-e29b-41d4-a716-446655440000`)
- 추측 불가능한 안전한 랜덤
- URL-safe
- 충돌 확률 무시 가능

**왜 email 역인덱스를 별도로 두지 않는가?**
- 토큰 → memberId 단방향 조회만 필요
- 같은 사용자가 여러 번 요청해도 각 토큰은 독립적으로 유효 (마지막에 클릭한 토큰이 사용됨)
- 단순화 우선

## 5. 플로우 다이어그램

```
[사용자]                [Backend]                  [Redis]            [Gmail SMTP]
   │                       │                         │                     │
   │ POST /password-reset/request                    │                     │
   ├──────────────────────►│                         │                     │
   │                       │ 1. Rate limit 체크      │                     │
   │                       ├────────────────────────►│                     │
   │                       │                         │                     │
   │                       │ 2. 회원 조회 (MySQL)    │                     │
   │                       │                         │                     │
   │                       │ 3. 즉시 200 응답        │                     │
   │◄──────────────────────┤                         │                     │
   │                       │ 4. @Async 메일 발송     │                     │
   │                       │    ─ 토큰 생성/저장 ────►│                     │
   │                       │    ─ 메일 발송 ─────────┼────────────────────►│
   │                                                                       │
   │              ┌────────────────────────────────────────────────────────┘
   │              │ 메일 도착 (재설정 링크: {FRONT}/reset?token=xxx)
   │◄─────────────┘
   │
   │ 링크 클릭 → 프론트에서 새 비밀번호 입력
   │
   │ POST /password-reset/confirm {token, newPassword}
   ├──────────────────────►│                         │                     │
   │                       │ 5. 토큰 조회/삭제        │                     │
   │                       ├────────────────────────►│                     │
   │                       │ 6. password 업데이트     │                     │
   │                       │    (MySQL)               │                     │
   │                       │ 7. RefreshToken 삭제     │                     │
   │                       ├────────────────────────►│                     │
   │                       │ 8. 200 응답              │                     │
   │◄──────────────────────┤                         │                     │
   │                       │ 9. @Async 알림 메일      │                     │
   │                       │    발송 ─────────────────┼────────────────────►│
```

## 6. 구현 컴포넌트

### 6.1 신규 파일

| 경로 | 역할 |
|------|------|
| `domain/auth/controller/dto/PasswordResetRequestRequest.java` | 재설정 요청 DTO |
| `domain/auth/controller/dto/PasswordResetConfirmRequest.java` | 재설정 확정 DTO |
| `domain/auth/service/PasswordResetService.java` | 재설정 비즈니스 로직 |
| `domain/auth/repository/PasswordResetTokenRepository.java` | Redis 토큰 저장/조회/삭제 |
| `domain/auth/repository/PasswordResetRateLimiter.java` | Rate limit 카운터 (Redis) |
| `global/mail/MailSender.java` | 메일 발송 인터페이스 |
| `global/mail/GmailMailSender.java` | Gmail SMTP 구현체 (`@Async` 적용) |
| `global/mail/MailProperties.java` | 메일 설정 바인딩 |
| `global/config/AsyncConfig.java` | `@EnableAsync` + ThreadPool 설정 |

### 6.2 수정 파일

| 경로 | 변경 내용 |
|------|----------|
| `domain/auth/controller/AuthController.java` | `/password-reset/request`, `/password-reset/confirm` 엔드포인트 추가 |
| `global/exception/error/ErrorCode.java` | `INVALID_PASSWORD_RESET_TOKEN` 추가 |
| `build.gradle` | `spring-boot-starter-mail` 의존성 추가 |
| `application.yml` | `spring.mail.*` 설정 추가 |

### 6.3 변경 없음

- `Member` 엔티티: 기존 `updatePassword(...)` 메서드 재사용 (있다면) / 없으면 추가
- `MemberRepository`: 기존 `findByEmail` 재사용
- `RefreshTokenRepository`: 기존 `delete(memberId)` 재사용

## 7. 환경 변수 / 설정

`application.yml` 추가:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}    # Gmail 앱 비밀번호 (2FA 필수)
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

custom:
  password-reset:
    token-ttl: 1800000          # 30분 (ms)
    rate-limit-window: 300000   # 5분 (ms)
    rate-limit-max: 3
    front-reset-url: ${FRONT_RESET_URL}   # 예: https://app.example.com/reset
```

새 환경변수:
- `MAIL_USERNAME` — Gmail 계정
- `MAIL_PASSWORD` — Gmail 앱 비밀번호 (일반 비밀번호 아님, 2단계 인증 활성화 후 발급)
- `FRONT_RESET_URL` — 프론트엔드 재설정 페이지 base URL

## 8. 메일 템플릿

### 8.1 비밀번호 재설정 메일 (일반 회원)

```
제목: [SSAFY Study] 비밀번호 재설정 안내

안녕하세요.

비밀번호 재설정 요청을 받았습니다.
아래 링크를 클릭하여 새 비밀번호를 설정해주세요.

{FRONT_RESET_URL}?token={token}

본 링크는 30분간 유효합니다.

본인이 요청하지 않은 경우 이 메일을 무시해주세요.
계정에는 아무런 변경도 일어나지 않습니다.
```

### 8.2 GitHub 전용 계정 안내 메일

```
제목: [SSAFY Study] 비밀번호 재설정 안내

안녕하세요.

비밀번호 재설정 요청을 받았으나, 해당 계정은 GitHub 로그인으로 가입되어 있어
비밀번호 재설정이 불가능합니다.

GitHub 로그인을 이용해주세요.
```

### 8.3 재설정 성공 알림 메일

```
제목: [SSAFY Study] 비밀번호가 변경되었습니다

안녕하세요.

회원님의 비밀번호가 방금 변경되었습니다.
모든 기기에서 자동으로 로그아웃되었습니다.

본인이 변경한 것이 맞다면 이 메일을 무시해주세요.
변경하지 않으셨다면, 즉시 고객센터로 문의해주세요.
```

> 1차 구현은 plain text. HTML 템플릿(Thymeleaf)은 후속 작업으로 분리.

## 9. 보안 고려사항

| 위협 | 대응 |
|------|------|
| **User Enumeration** | 모든 응답을 200으로 통일, 메일 발송도 비동기로 timing 통일 |
| **토큰 추측** | UUID v4 사용 (122 bits 엔트로피) |
| **토큰 재사용** | 사용 즉시 Redis에서 삭제 |
| **토큰 가로채기 (메일 노출)** | 30분 TTL로 노출 윈도우 최소화 |
| **무차별 요청** | Rate limit (이메일 기준 5분 3회) |
| **세션 하이재킹 잔존** | 재설정 성공 시 모든 RefreshToken 삭제 |
| **본인 부지 변경** | 성공 시 알림 메일 발송 |
| **Gmail 자격증명 유출** | 앱 비밀번호 사용 (2FA 강제), 환경변수 분리 |

**의도적으로 적용하지 않은 보호 장치:**
- 토큰 횟수 제한(예: 1회만 사용 가능) → 이미 사용 즉시 삭제로 보장됨
- IP 기준 rate limit → 1차 범위 외 (NAT 환경에서 정상 사용자 차단 우려)

## 10. 에러 처리

### 10.1 신규 ErrorCode

```java
INVALID_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 재설정 토큰입니다."),
```

### 10.2 명시적으로 노출하지 않는 에러

다음 상황은 모두 `200 OK` 응답으로 처리 (User Enumeration 방지):
- 존재하지 않는 이메일
- GitHub OAuth 전용 계정
- Rate limit 초과 (메일 발송만 스킵, 응답은 200)
- 잘못된 이메일 형식? → **이건 400** (단순 입력 검증 실패는 enumeration과 무관)

## 11. 테스트 계획

### 11.1 단위 테스트 (PasswordResetServiceTest)

- ✅ 정상 회원이 요청 시 → 토큰 생성 + 메일 전송 호출
- ✅ 존재하지 않는 이메일 요청 시 → 토큰 생성 안 함 + 메일 전송 호출 안 함
- ✅ GitHub OAuth 전용 계정 요청 시 → 안내 메일 전송 호출
- ✅ Rate limit 초과 시 → 메일 전송 호출 안 함
- ✅ 유효한 토큰으로 confirm 시 → 비밀번호 변경 + RefreshToken 삭제 + 알림 메일
- ✅ 만료/존재하지 않는 토큰으로 confirm 시 → `INVALID_PASSWORD_RESET_TOKEN`
- ✅ 토큰 사용 후 재사용 시도 시 → `INVALID_PASSWORD_RESET_TOKEN`
- ✅ 비밀번호 길이 부족 시 → Validation 실패

### 11.2 컨트롤러 테스트 (AuthControllerTest)

- ✅ `/password-reset/request` 정상/실패 모두 200 응답
- ✅ `/password-reset/confirm` 정상 시 비밀번호 변경 동작 확인
- ✅ `/password-reset/confirm` 위조 토큰 시 400 응답

### 11.3 통합/수동 테스트

- ✅ 실제 Gmail 발송 1회 (수동, dev 환경)
- ✅ 비밀번호 변경 후 기존 access/refresh 토큰으로 API 호출 → 401

## 12. 구현 단계 (제안 순서)

1. `build.gradle`에 `spring-boot-starter-mail` 추가
2. `MailSender` 인터페이스 + `GmailMailSender` 구현 + `AsyncConfig`
3. `PasswordResetTokenRepository`, `PasswordResetRateLimiter` (Redis)
4. `ErrorCode.INVALID_PASSWORD_RESET_TOKEN` 추가
5. `PasswordResetRequestRequest`, `PasswordResetConfirmRequest` DTO
6. `PasswordResetService` 구현
7. `AuthController`에 두 엔드포인트 추가
8. 단위 + 컨트롤러 테스트 작성
9. `application.yml` 설정 추가
10. README/`.env.example` 업데이트 (있다면)

## 13. 후속 작업 (이번 PR 범위 밖)

- HTML 메일 템플릿 (Thymeleaf)
- 다국어 메일 (i18n)
- AWS SES 마이그레이션 (운영 트래픽 증가 시)
- 이메일 인증 (가입 시 본인 확인) — 별도 설계 필요
- 비밀번호 만료/주기적 변경 강제 — 별도 정책 필요

---

**확인 사항**: 이 설계가 합의되면 위 12장 순서대로 구현을 시작합니다.
