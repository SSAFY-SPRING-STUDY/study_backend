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
    id         BIGINT     AUTO_INCREMENT PRIMARY KEY,
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
