ALTER TABLE notification
    DROP COLUMN member_id,
    DROP COLUMN is_read;

CREATE TABLE notification_read
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    notification_id BIGINT                NOT NULL,
    member_id       BIGINT                NOT NULL,
    CONSTRAINT pk_notification_read PRIMARY KEY (id),
    CONSTRAINT uq_notification_read UNIQUE (notification_id, member_id)
);

ALTER TABLE notification_read
    ADD CONSTRAINT FK_NOTIFICATION_READ_ON_NOTIFICATION FOREIGN KEY (notification_id) REFERENCES notification (id);

ALTER TABLE notification_read
    ADD CONSTRAINT FK_NOTIFICATION_READ_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);
