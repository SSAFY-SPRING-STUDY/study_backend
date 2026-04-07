ALTER TABLE study
  ADD COLUMN github_org_name VARCHAR(100),
  ADD COLUMN github_repo_name VARCHAR(100),
  ADD COLUMN github_webhook_secret VARCHAR(255);

CREATE TABLE study_member (
  id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  study_id  BIGINT      NOT NULL,
  member_id BIGINT      NOT NULL,
  role      VARCHAR(20) NOT NULL,
  joined_at DATETIME    NOT NULL,
  FOREIGN KEY (study_id)  REFERENCES study(id),
  FOREIGN KEY (member_id) REFERENCES member(id),
  UNIQUE KEY uq_study_member (study_id, member_id)
);
