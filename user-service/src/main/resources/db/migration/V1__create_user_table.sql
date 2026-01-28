CREATE TABLE users
(
    user_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE COMMENT '아이디(이메일)',
    password     VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    name         VARCHAR(100) NOT NULL COMMENT '이름',
    phone_number VARCHAR(20)  NOT NULL COMMENT '휴대폰 번호',
    gender       VARCHAR(10)  NOT NULL COMMENT '성별(MALE, FEMALE)',
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '권한(USER, ADMIN)',
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '활성화 상태',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP NULL COMMENT '탈퇴 일시',
    INDEX        idx_email (email),
    INDEX        idx_is_active (is_active)
);