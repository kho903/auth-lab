-- V2: Multi-Tenant Tables
-- MySQL 8.4

-- 1. TENANTS
CREATE TABLE tenants (
    id BIGINT NOT NULL AUTO_INCREMENT,

    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_by_user_id BIGINT NOT NULL,

    created_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_tenants
        PRIMARY KEY (id),

    CONSTRAINT uq_tenants_slug
        UNIQUE (slug),

    CONSTRAINT fk_tenants_creator
        FOREIGN KEY (created_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_tenants_status
        CHECK (
            status IN ('ACTIVE', 'SUSPENDED')
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;


-- 2. TENANT MEMBERSHIPS
CREATE TABLE tenant_memberships (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,

    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    joined_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    left_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_tenant_memberships
        PRIMARY KEY (id),

    CONSTRAINT uq_membership_user_tenant
        UNIQUE (user_id, tenant_id),

    CONSTRAINT uq_membership_tenant_id
        UNIQUE (tenant_id, id),

    INDEX idx_membership_tenant_status_role
        (tenant_id, status, role),

    CONSTRAINT fk_membership_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_membership_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_membership_role
        CHECK (
            role IN ('OWNER', 'ADMIN', 'MEMBER')
        ),

    CONSTRAINT chk_membership_status
        CHECK (
            status IN ('ACTIVE', 'SUSPENDED', 'LEFT')
        ),

    CONSTRAINT chk_membership_left_at
        CHECK (
            (status = 'LEFT' AND left_at IS NOT NULL)
            OR
            (status <> 'LEFT' AND left_at IS NULL)
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;


-- 3. TENANT INVITATIONS
CREATE TABLE tenant_invitations (
    id BIGINT NOT NULL AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,
    invited_by_membership_id BIGINT NOT NULL,

    invited_email VARCHAR(254) NOT NULL,
    token_hash BINARY(32) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at DATETIME(6) NOT NULL,

    accepted_by_user_id BIGINT NULL,
    accepted_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_tenant_invitations
        PRIMARY KEY (id),

    CONSTRAINT uq_invitation_token_hash
        UNIQUE (token_hash),

    CONSTRAINT uq_invitation_tenant_email
        UNIQUE (tenant_id, invited_email),

    INDEX idx_invitation_tenant_status
        (tenant_id, status, expires_at),

    INDEX idx_invitation_tenant_inviter
        (tenant_id, invited_by_membership_id),

    CONSTRAINT fk_invitation_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_invitation_inviter_membership
        FOREIGN KEY (tenant_id, invited_by_membership_id)
        REFERENCES tenant_memberships(tenant_id, id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_invitation_receiver
        FOREIGN KEY (accepted_by_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_invitation_status
        CHECK (
            status IN (
                'PENDING',
                'ACCEPTED',
                'CANCELED',
                'EXPIRED'
            )
        ),

    CONSTRAINT chk_invitation_expiration
        CHECK (
            expires_at > created_at
        ),

    CONSTRAINT chk_invitation_acceptance
        CHECK (
            (
                status = 'ACCEPTED'
                AND accepted_by_user_id IS NOT NULL
                AND accepted_at IS NOT NULL
            )
            OR
            (
                status <> 'ACCEPTED'
                AND accepted_by_user_id IS NULL
                AND accepted_at IS NULL
            )
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;


-- 4. NOTICES
CREATE TABLE notices (
    id BIGINT NOT NULL AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,
    author_membership_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,

    created_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_notices
        PRIMARY KEY (id),

    INDEX idx_notices_tenant_created
        (tenant_id, created_at, id),

    INDEX idx_notices_tenant_author
        (tenant_id, author_membership_id),

    CONSTRAINT fk_notices_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_notices_author_membership
        FOREIGN KEY (tenant_id, author_membership_id)
        REFERENCES tenant_memberships(tenant_id, id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;
