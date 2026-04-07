CREATE TABLE assignment (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  study_id       BIGINT       NOT NULL,
  title          VARCHAR(255) NOT NULL,
  content        LONGTEXT     NOT NULL,
  pr_template    LONGTEXT     NOT NULL,
  order_in_study INT          NOT NULL,
  created_at     DATETIME     NOT NULL,
  updated_at     DATETIME     NOT NULL,
  FOREIGN KEY (study_id) REFERENCES study(id),
  UNIQUE KEY uq_assignment_order (study_id, order_in_study)
);
