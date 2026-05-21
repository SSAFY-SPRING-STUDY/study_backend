# GitHub App 기반 기능 제거 계획

## 1. 목적과 범위

서비스가 GitHub에 과도하게 의존하지 않도록, **GitHub OAuth 로그인을 제외한 모든
GitHub 연동 기능을 제거**한다.

### 유지 (touch 금지)

- `GET /api/v1/auth/github` — GitHub OAuth 로그인 진입점
- `GET /api/v1/auth/github/callback` — 로그인 콜백 + JWT 발급
- `Member.githubId` / `Member.githubUsername` — OAuth 로그인 시 사용자 식별 키
- `MemberRepository.findByGithubId` / `existsByGithubId`
- `GitHubOAuthService` 중 **로그인 부분만**
- `GitHubOAuthClient.exchangeLoginCode` / `getUser`
- `GitHubProperties` 중 `clientId`, `clientSecret`, `redirectUri`, `frontRedirectUri`
- `GitHubTokenResponse`, `GitHubUserResponse` DTO
- `ErrorCode.GITHUB_OAUTH_FAILED`, `GITHUB_ONLY_ACCOUNT`

### 삭제

| 분류 | 대상 | 비고 |
|---|---|---|
| Webhook | `webhook/GitHubWebhookController.java` | 패키지 통째로 비움 |
| Webhook 테스트 | `test/.../webhook/GitHubWebhookControllerTest.java` | |
| GitHub REST API 클라이언트 | `global/github/GitHubClient.java` | Issue·Repo 생성 |
| GitHub DTO | `dto/GitHubCreateRepoRequest.java`, `GitHubIssueRequest.java`, `GitHubIssueResponse.java` | |
| GitHub Connect 서비스 | `GitHubOAuthService.getConnectRedirectUrl`, `handleConnectCallback` | |
| Connect OAuth Client | `GitHubOAuthClient.exchangeConnectCode` | |
| Connect Properties | `GitHubProperties` 중 `connectClientId`, `connectClientSecret`, `connectRedirectUri` | record 필드 제거 |
| Connect 컨트롤러 | `MemberController.startGithubConnect`, `githubConnectCallback` | 의존 `GitHubOAuthService` 주입도 제거 |
| State JWT | `JwtProvider.generateStateToken`, `validateStateToken`, `STATE_EXPIRATION_MS` | Connect 전용 |
| Assignment 도메인 | `domain/edu/assignment/**` 전체 (controller, service, entity, repository, dto) | 사용자 결정에 따라 통째로 삭제 |
| Assignment 테스트 | `test/.../assignment/service/AssignmentServiceTest.java` 등 | |
| Study GitHub 필드 | `Study.githubOrgName`, `Study.githubWebhookSecret` (필드/Builder/update) | |
| StudyRequest/Response | `githubOrgName`, `githubWebhookSecret` 필드 | |
| StudyMember GitHub 필드 | `StudyMember.githubRepoName`, `updateGithubRepo` | |
| StudyMemberController | `updateGithubRepo` 엔드포인트 (`PATCH /api/v1/studies/{studyId}/members/{targetMemberId}/repo`), `join`의 `githubRepoName` 파라미터 | |
| StudyMemberService | `join`의 `githubRepoName` 파라미터·`gitHubClient.createOrgRepo` 호출·`GITHUB_ORG_NOT_CONFIGURED` 검증, `updateGithubRepo`, `GitHubClient` 주입 | |
| DTO | `JoinStudyRequest`, `UpdateMemberRepoRequest` | join은 body 없는 POST로 단순화 |
| StudyMemberInfo | `githubRepoName` 필드 | |
| Member.isGithubLinked | 사용처가 모두 사라짐 | 메서드 삭제 |
| Member.connectGithub | 로그인 흐름에서만 사용 → **유지** (기존 회원이 GitHub OAuth로 추가 로그인했을 때 githubId/Username 채워주는 역할) | 메서드명 그대로 둠 |
| Security 설정 | `/api/v1/members/me/github/connect/callback`, `/api/v1/webhook/github/**` permitAll 라인 | |
| ErrorCode | `GITHUB_ISSUE_CREATION_FAILED`, `GITHUB_WEBHOOK_INVALID_SIGNATURE`, `GITHUB_ACCOUNT_NOT_LINKED`, `GITHUB_ACCOUNT_ALREADY_LINKED`, `GITHUB_REPO_NOT_REGISTERED`, `GITHUB_REPO_CREATION_FAILED`, `GITHUB_ORG_NOT_CONFIGURED`, `ASSIGNMENT_*` 전부 | |
| DB | `study.github_org_name`, `study.github_webhook_secret`, `study_member.github_repo_name`, `assignment`, `assignment_progress` 테이블 | V21 신규 마이그레이션 |

## 2. 결정 사항 요약 (사용자 확정)

- [x] **Assignment 도메인은 통째로 삭제** — Issue 생성 / Webhook 완료가 본질이라 GitHub 연동을 빼면 도메인 자체가 사실상 빈 껍질.
- [x] **GitHub 계정 Connect 기능 삭제** — 로그인 외 GitHub 의존을 모두 제거한다는 방향과 일치.
- [x] **DB 컬럼/테이블도 V21 마이그레이션으로 DROP** — 스키마까지 정리.

## 3. DB 마이그레이션 (V21) 초안

> 외래키 → 자식 테이블 → 부모 컬럼 순으로 정리한다. MySQL/Flyway 기준.

```sql
-- V21__remove_github_app_features.sql

-- 1) Assignment 도메인 테이블 제거 (자식 → 부모 순)
DROP TABLE IF EXISTS assignment_progress;
DROP TABLE IF EXISTS assignment;

-- 2) StudyMember의 GitHub 레포 컬럼 제거
ALTER TABLE study_member DROP COLUMN github_repo_name;

-- 3) Study의 GitHub 조직/Webhook 컬럼 제거
ALTER TABLE study
    DROP COLUMN github_org_name,
    DROP COLUMN github_webhook_secret;
```

> `assignment_progress`가 `study_member`를 FK로 잡고 있으므로 `study_member` 컬럼
> 변경보다 먼저 DROP 해야 안전하다. (현재 FK가 `ON DELETE CASCADE`이므로 ALTER 자체는
> 가능하지만, 테이블을 먼저 비우는 편이 흐름상 명확.)

## 4. 작업 순서 (PR 분할은 하지 않고 한 번에 처리)

1. **DB 마이그레이션 작성** — `V21__remove_github_app_features.sql`
2. **Assignment 도메인 디렉터리 삭제**
   - `src/main/java/.../domain/edu/assignment/**`
   - `src/test/java/.../domain/edu/assignment/**`
3. **Webhook 패키지 삭제**
   - `src/main/java/.../webhook/**`
   - `src/test/java/.../webhook/**`
4. **GitHub API/Webhook 코어 삭제**
   - `global/github/GitHubClient.java`
   - `global/github/dto/GitHubCreateRepoRequest.java`
   - `global/github/dto/GitHubIssueRequest.java`
   - `global/github/dto/GitHubIssueResponse.java`
5. **GitHub Connect 흐름 삭제**
   - `MemberController` 두 메서드 + `GitHubOAuthService` 주입 제거
   - `GitHubOAuthService` connect 메서드 / connect 의존 필드 정리
   - `GitHubOAuthClient.exchangeConnectCode` 삭제
   - `GitHubProperties` record에서 `connectClientId`, `connectClientSecret`, `connectRedirectUri` 제거
   - `JwtProvider`에서 state 토큰 메서드 / 상수 / 관련 import 제거
6. **Study / StudyMember 정리**
   - `Study` 엔티티에서 GitHub 필드 / Builder 인자 / `update` 시그니처
   - `StudyService.createStudy` / `updateStudy`에서 GitHub 인자 제거
   - `StudyRequest`, `StudyResponse` 필드 제거
   - `StudyMember` 엔티티에서 `githubRepoName` 필드와 `updateGithubRepo` 제거
   - `StudyMemberController`에서 `updateGithubRepo` 엔드포인트 제거, `join`의 request body 단순화
   - `StudyMemberService.join`에서 GitHub 분기/`createOrgRepo` 호출 전부 제거
   - `StudyMemberInfo`에서 `githubRepoName` 제거
   - `JoinStudyRequest`, `UpdateMemberRepoRequest` 파일 삭제 (또는 JoinStudyRequest는 사용처 없으므로 삭제)
7. **Member 정리**
   - `Member.isGithubLinked` 메서드 삭제 (호출처 모두 사라짐)
   - `Member.githubId`, `githubUsername`, `connectGithub`는 **유지**
8. **ErrorCode 정리**
   - 표(섹션 1)에 나열된 GitHub/Assignment 관련 코드 enum 항목 제거
9. **SecurityConfig 정리**
   - `/api/v1/members/me/github/connect/callback`, `/api/v1/webhook/github/**` permitAll 라인 제거
10. **`application.yml` / `application-*.yml` 확인**
    - `github.connect-client-id`, `github.connect-client-secret`, `github.connect-redirect-uri`, `github.token` 키 삭제 (있다면)
    - 운영 환경 변수 정리 안내 필요
11. **테스트 정리**
    - `GitHubOAuthServiceTest` 의 connect 관련 케이스 제거 (로그인 케이스만 유지)
    - `MemberFixture` 의 GitHub 필드 사용처 확인 (githubId/githubUsername은 그대로 사용 가능)
12. **빌드/테스트**
    - `./gradlew compileJava test` 통과 확인

## 5. 위험 요소 / 체크리스트

- **외래키 dangling**: `assignment_progress.study_member_id` → `study_member.id`.
  - V21에서 `assignment_progress`를 먼저 DROP 하므로 FK 안전.
- **운영 DB 컬럼 NOT NULL 여부**: 현재 모두 nullable (V15/V16/V17/V18/V19 확인 완료). DROP 시 데이터 손실 외 제약 위반 없음.
- **컬럼 DROP 시 데이터 손실**: `study.github_org_name`, `study.github_webhook_secret`,
  `study_member.github_repo_name`, `assignment*` 테이블의 모든 데이터 영구 소실. 운영 데이터가 있다면 사전 백업 권고.
- **프론트엔드 영향**: 다음 URL/필드는 사라지므로 FE 측 호출/표시 코드 정리 필요.
  - `GET /api/v1/members/me/github/connect`
  - `GET /api/v1/members/me/github/connect/callback`
  - `POST /api/v1/webhook/github/{studyId}` (외부 GitHub Webhook 등록도 해제)
  - 모든 `/api/v1/.../assignments/...` 엔드포인트
  - `PATCH /api/v1/studies/{studyId}/members/{targetMemberId}/repo`
  - `StudyRequest`의 `githubOrgName`, `githubWebhookSecret` body 필드
  - `StudyMemberInfo`의 `githubRepoName`, `Study` 응답의 `githubOrgName`
  - `JoinStudyRequest` body — `POST /api/v1/studies/{studyId}/members`가 body 없이 호출되도록 변경
- **사이드 이펙트**: `Member.isGithubLinked()` 삭제 후 컴파일 오류로 잔존 호출처 자동 발견. assignment 도메인이 통째로 사라지므로 잔존 import만 정리하면 됨.
- **Connect OAuth App 콜백 URL**: GitHub 측 OAuth App 설정에서도 콜백 URL을 정리해야 함 (코드 작업 외 운영 작업).

## 6. 작업 산출물 (예상)

- 삭제 디렉터리: `domain/edu/assignment/`, `webhook/`
- 신규 파일: `src/main/resources/db/migration/V21__remove_github_app_features.sql`
- 수정 파일 (예상 12~15개):
  - `AuthController.java`(영향 없음 확인용), `MemberController.java`
  - `GitHubOAuthService.java`, `GitHubOAuthClient.java`, `GitHubProperties.java`
  - `JwtProvider.java`
  - `Study.java`, `StudyMember.java`
  - `StudyService.java`, `StudyMemberService.java`, `StudyMemberController.java`
  - `StudyRequest.java`, `StudyResponse.java`, `StudyMemberInfo.java`
  - `Member.java` (isGithubLinked 제거)
  - `ErrorCode.java`
  - `SecurityConfig.java`

## 7. 진행 승인 요청

위 계획대로 **한 번에 일괄 삭제 + V21 마이그레이션 추가**로 진행할지 결정 부탁드립니다.

- ✅ 그대로 진행 → "구현해줘"
- ✏️ 조정 필요 → 어느 단계를 빼거나 분할할지 알려주세요. (예: "V21만 먼저 빼고 코드만 정리하자")