-- V1: Authentication Tables
-- MySQL 8.4

-- 1. USERS
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,

    email VARCHAR(254) NOT NULL,
    name VARCHAR(100) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    email_verified_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_users
        PRIMARY KEY (id),

    CONSTRAINT uq_users_email
        UNIQUE (email),

    CONSTRAINT chk_users_status
        CHECK (
            status IN ('PENDING', 'ACTIVE', 'SUSPENDED')
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;


-- 2. LOCAL CREDENTIALS
CREATE TABLE local_credentials (
    user_id BIGINT NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    password_changed_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_local_credentials
        PRIMARY KEY (user_id),

    CONSTRAINT fk_local_credentials_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;


-- 3. OAUTH ACCOUNTS
CREATE TABLE oauth_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    provider VARCHAR(20)
        CHARACTER SET ascii
        COLLATE ascii_bin
        NOT NULL,

    provider_user_id VARCHAR(255)
        CHARACTER SET ascii
        COLLATE ascii_bin
        NOT NULL,

    provider_email VARCHAR(254) NULL,

    linked_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_oauth_accounts
        PRIMARY KEY (id),

    CONSTRAINT uq_oauth_provider_subject
        UNIQUE (provider, provider_user_id),

    CONSTRAINT uq_oauth_user_provider
        UNIQUE (user_id, provider),

    CONSTRAINT fk_oauth_accounts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_oauth_provider
        CHECK (
            provider IN ('GOOGLE')
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;
