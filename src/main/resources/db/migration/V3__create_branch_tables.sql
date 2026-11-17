
-- ==========================================
-- V3: Branch Tables
-- MySQL 8.4
-- ==========================================

-- 1. BRANCHES
CREATE TABLE branches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_branches
        PRIMARY KEY (id),

    CONSTRAINT uq_branches_tenant_code
        UNIQUE (tenant_id, code),

    CONSTRAINT uq_branches_tenant_id
        UNIQUE (tenant_id, id),

    CONSTRAINT fk_branches_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_branches_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;


-- 2. BRANCH MEMBERSHIPS
CREATE TABLE branch_memberships (
    id BIGINT NOT NULL AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    tenant_membership_id BIGINT NOT NULL,

    assigned_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    unassigned_at DATETIME(6) NULL,

    CONSTRAINT pk_branch_memberships
        PRIMARY KEY (id),

    CONSTRAINT uq_branch_memberships_branch_member
        UNIQUE (branch_id, tenant_membership_id),

    INDEX idx_branch_memberships_tenant_branch
        (tenant_id, branch_id),

    INDEX idx_branch_memberships_tenant_member
        (tenant_id, tenant_membership_id),

    CONSTRAINT fk_branch_memberships_branch
        FOREIGN KEY (tenant_id, branch_id)
        REFERENCES branches(tenant_id, id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_branch_memberships_tenant_member
        FOREIGN KEY (tenant_id, tenant_membership_id)
        REFERENCES tenant_memberships(tenant_id, id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_branch_membership_dates
        CHECK (
            unassigned_at IS NULL
            OR unassigned_at >= assigned_at
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_as_ci;
