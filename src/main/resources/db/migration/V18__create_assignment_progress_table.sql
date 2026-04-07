CREATE TABLE assignment_progress (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  study_member_id     BIGINT      NOT NULL,
  assignment_id       BIGINT      NOT NULL,
  status              VARCHAR(20) NOT NULL,
  github_issue_number BIGINT,
  applied_at          DATETIME    NOT NULL,
  completed_at        DATETIME,
  FOREIGN KEY (study_member_id) REFERENCES study_member(id) ON DELETE CASCADE,
  FOREIGN KEY (assignment_id)   REFERENCES assignment(id)   ON DELETE CASCADE,
  UNIQUE KEY uq_progress (study_member_id, assignment_id)
);
