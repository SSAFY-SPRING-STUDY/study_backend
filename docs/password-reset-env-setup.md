# 비밀번호 재설정 — 환경변수 설정 가이드

비밀번호 재설정 기능 구현이 완료되었습니다. 실제 동작을 위해 아래 환경변수 3개를
`.env` 파일에 추가해야 합니다.

## 1. 추가해야 할 환경변수

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `MAIL_USERNAME` | 메일 발송에 사용할 Gmail 계정 | `your-app@gmail.com` |
| `MAIL_PASSWORD` | Gmail 앱 비밀번호 (16자리, 일반 비밀번호 아님) | `abcd efgh ijkl mnop` |
| `FRONT_RESET_URL` | 프론트엔드 비밀번호 재설정 페이지 base URL | `https://app.example.com/reset` 또는 로컬은 `http://localhost:3000/reset` |

`.env` 추가 예시:
```
MAIL_USERNAME=your-app@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
FRONT_RESET_URL=http://localhost:3000/reset
```

## 2. Gmail 앱 비밀번호 발급 방법

> ⚠️ 일반 Gmail 비밀번호로는 SMTP 인증이 안 됩니다. 반드시 **앱 비밀번호**가 필요합니다.

1. 사용할 Gmail 계정으로 로그인 → https://myaccount.google.com/
2. 좌측 메뉴 **보안** 클릭
3. **2단계 인증** 활성화 (안 되어 있으면 먼저 활성화 필요)
4. 검색창에 "앱 비밀번호" 검색 → https://myaccount.google.com/apppasswords
5. 앱 이름 입력 (예: "SSAFY Study Backend") → **만들기** 클릭
6. 16자리 비밀번호 복사 (공백 없이 그대로 사용 가능)
7. `.env`의 `MAIL_PASSWORD`에 붙여넣기

## 3. 동작 확인

설정 후 백엔드 재시작 → 아래 두 엔드포인트 호출:

```bash
# 1. 재설정 요청 (실제 가입된 이메일로 메일 발송됨)
curl -X POST http://localhost:8080/api/v1/auth/password-reset/request \
  -H "Content-Type: application/json" \
  -d '{"email": "your-test-account@example.com"}'

# 응답: 회원 존재 여부와 무관하게 항상 200
# {"message":"입력하신 이메일이 가입된 계정이라면 재설정 메일이 발송됩니다.","data":null}

# 2. 메일에 도착한 토큰으로 확정
curl -X POST http://localhost:8080/api/v1/auth/password-reset/confirm \
  -H "Content-Type: application/json" \
  -d '{"token": "<메일에서 받은 토큰>", "newPassword": "newPassword123"}'

# 응답: {"message":"비밀번호가 성공적으로 재설정되었습니다.","data":null}
```

## 4. 주의 사항

- Gmail 무료 계정은 **일일 500통 발송 제한**이 있습니다. 운영 트래픽이 증가하면 AWS SES로 마이그레이션 필요.
- 앱 비밀번호는 한 번만 표시됩니다. 분실 시 새로 발급해야 합니다.
- 메일 발송 실패는 로그에만 남고 사용자에게 노출되지 않습니다 (User Enumeration 방지 차원).
- `FRONT_RESET_URL`은 프론트엔드가 비밀번호 재설정 페이지를 어디에 두는지에 따라 달라집니다. 프론트엔드 라우팅이 준비되지 않았다면 일단 임시 URL을 넣어두고 추후 변경하세요.
