-- GitHub App 기반 기능 제거: Webhook/Issue/Repo/Assignment 관련 컬럼·테이블 정리
-- GitHub OAuth 로그인용 컬럼(member.github_id, member.github_username)은 유지

-- 1) Assignment 도메인 테이블 제거 (자식 → 부모 순)
DROP TABLE IF EXISTS assignment_progress;
DROP TABLE IF EXISTS assignment;

-- 2) StudyMember의 GitHub 레포 컬럼 제거
ALTER TABLE study_member DROP COLUMN github_repo_name;

-- 3) Study의 GitHub 조직/Webhook 컬럼 제거
ALTER TABLE study
    DROP COLUMN github_org_name,
    DROP COLUMN github_webhook_secret;