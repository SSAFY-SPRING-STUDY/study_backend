CREATE TABLE comment
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    content          TEXT                  NOT NULL,
    re_comment_count BIGINT                NOT NULL DEFAULT 0,
    created_at       DATETIME              NOT NULL,
    updated_at       DATETIME              NOT NULL,
    post_id          BIGINT                NOT NULL,
    author_id        BIGINT                NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id)
);

CREATE TABLE re_comment
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    content    TEXT                  NOT NULL,
    created_at DATETIME              NOT NULL,
    updated_at DATETIME              NOT NULL,
    comment_id BIGINT                NOT NULL,
    author_id  BIGINT                NOT NULL,
    CONSTRAINT pk_re_comment PRIMARY KEY (id)
);

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES member (id);

ALTER TABLE re_comment
    ADD CONSTRAINT FK_RECOMMENT_ON_COMMENT FOREIGN KEY (comment_id) REFERENCES comment (id);

ALTER TABLE re_comment
    ADD CONSTRAINT FK_RECOMMENT_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES member (id);
