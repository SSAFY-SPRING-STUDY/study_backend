CREATE TABLE curriculum
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    name           VARCHAR(255)          NOT NULL,
    `description`  VARCHAR(255)          NOT NULL,
    order_in_study INT                   NOT NULL,
    posts_count    INT                   NOT NULL,
    version        BIGINT                NULL,
    study_id       BIGINT                NULL,
    CONSTRAINT pk_curriculum PRIMARY KEY (id)
);

CREATE TABLE post
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    title               VARCHAR(255)          NOT NULL,
    content             LONGTEXT              NOT NULL,
    author_id           BIGINT                NULL,
    order_in_curriculum INT                   NOT NULL,
    curriculum_id       BIGINT                NULL,
    created_at          datetime              NOT NULL,
    updated_at          datetime              NOT NULL,
    CONSTRAINT pk_post PRIMARY KEY (id)
);

CREATE TABLE study
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    `description` VARCHAR(255)          NOT NULL,
    level         VARCHAR(255)          NOT NULL,
    type          VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_study PRIMARY KEY (id)
);

ALTER TABLE curriculum
    ADD CONSTRAINT uc_curriculum_orderinstudy UNIQUE (order_in_study);

ALTER TABLE curriculum
    ADD CONSTRAINT FK_CURRICULUM_ON_STUDY FOREIGN KEY (study_id) REFERENCES study (id);

ALTER TABLE post
    ADD CONSTRAINT FK_POST_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES member (id);

ALTER TABLE post
    ADD CONSTRAINT FK_POST_ON_CURRICULUM FOREIGN KEY (curriculum_id) REFERENCES curriculum (id);

ALTER TABLE member
    MODIFY email VARCHAR(255);

ALTER TABLE member
    MODIFY level VARCHAR(255);

ALTER TABLE member
    MODIFY name VARCHAR(255);

ALTER TABLE member
    MODIFY nickname VARCHAR(255);

ALTER TABLE member
    MODIFY `role` VARCHAR(255);
