CREATE TABLE notification
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    member_id  BIGINT NULL,
    content    VARCHAR(255) NULL,
    is_read    BIT(1) NULL,
    created_at datetime NULL,
    CONSTRAINT pk_notification PRIMARY KEY (id)
);
