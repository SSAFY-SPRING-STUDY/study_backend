CREATE TABLE image
(
    id      BIGINT AUTO_INCREMENT NOT NULL,
    post_id BIGINT       NOT NULL,
    `key`   VARCHAR(255) NOT NULL,
    status  SMALLINT     NOT NULL,
    CONSTRAINT pk_image PRIMARY KEY (id)
);

CREATE TABLE notice
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    title     VARCHAR(255) NOT NULL,
    content   VARCHAR(255) NOT NULL,
    author_id BIGINT       NOT NULL,
    CONSTRAINT pk_notice PRIMARY KEY (id)
);

ALTER TABLE image
    ADD CONSTRAINT FK_IMAGE_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE notice
    ADD CONSTRAINT FK_NOTICE_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES member (id);
