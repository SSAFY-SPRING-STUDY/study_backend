CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(50)  NOT NULL,
    email    VARCHAR(100) NOT NULL,
    nickname VARCHAR(50)  NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL,
    level    VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_member_email (email),
    UNIQUE KEY uq_member_nickname (nickname)
);