CREATE TABLE member_profile (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id         BIGINT NOT NULL UNIQUE,
    description       VARCHAR(500),
    profile_image_key VARCHAR(255),
    CONSTRAINT fk_member_profile_member FOREIGN KEY (member_id) REFERENCES member (id)
);
