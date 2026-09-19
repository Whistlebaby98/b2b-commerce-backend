-- =========================================================================
-- Flyway Migration Script: V1.0.0__init_schema.sql
-- Description: 企采云对公商城 PostgreSQL 16 初始化建表脚本
-- Author: b2b-commerce-backend
-- =========================================================================

-- 启用 pg_trgm 扩展以支持高精度 SKU/OEM 模糊检索 (ADR 0010)
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ========================================================
-- 1. 系统与本地消息表 (Outbox Pattern, ADR 0004)
-- ========================================================
CREATE TABLE IF NOT EXISTS sys_outbox_message (
    id BIGINT PRIMARY KEY,
    company_id VARCHAR(64) NOT NULL,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    event_type VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_outbox_status_retry ON sys_outbox_message(status, next_retry_time);

-- ========================================================
-- 2. 组织与用户域 (IAM)
-- ========================================================
CREATE TABLE IF NOT EXISTS org_company (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(128) NOT NULL,
    tax_id VARCHAR(64) NOT NULL UNIQUE,
    customer_tier VARCHAR(32) NOT NULL DEFAULT 'standard',
    is_verified BOOLEAN NOT NULL DEFAULT true,
    credit_limit NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    credit_used NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    payment_term_days INT NOT NULL DEFAULT 30,
    currency VARCHAR(16) NOT NULL DEFAULT 'CNY',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS org_user (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    department VARCHAR(128),
    avatar_text VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS org_membership (
    id BIGINT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES org_user(id),
    company_id VARCHAR(64) NOT NULL REFERENCES org_company(id),
    role VARCHAR(32) NOT NULL DEFAULT 'buyer',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, company_id)
);

-- ========================================================
-- 3. 商品与目录域 (Catalog)
-- ========================================================
CREATE TABLE IF NOT EXISTS pms_category (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    parent_id VARCHAR(64),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pms_product (
    id VARCHAR(64) PRIMARY KEY,
    slug VARCHAR(128) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    brand VARCHAR(128) NOT NULL,
    category_id VARCHAR(64) NOT NULL REFERENCES pms_category(id),
    description TEXT,
    unit VARCHAR(32) NOT NULL DEFAULT '件',
    default_sku_id VARCHAR(64),
    tone VARCHAR(32),
    accent VARCHAR(32),
    mark VARCHAR(32),
    badges JSONB NOT NULL DEFAULT '[]'::jsonb,
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    status VARCHAR(32) NOT NULL DEFAULT 'active',
    is_featured BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pms_sku (
    id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL REFERENCES pms_product(id),
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    unit VARCHAR(32) NOT NULL DEFAULT '件',
    price NUMERIC(12, 2) NOT NULL,
    list_price NUMERIC(12, 2),
    currency VARCHAR(16) NOT NULL DEFAULT 'CNY',
    stock INT NOT NULL DEFAULT 0,
    availability VARCHAR(32) NOT NULL DEFAULT 'in_stock',
    lead_time_label VARCHAR(64) NOT NULL DEFAULT '现货，工作日当日发货',
    status VARCHAR(32) NOT NULL DEFAULT 'available',
    weight_kg NUMERIC(8, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sku_attributes_gin ON pms_sku USING gin (attributes);
CREATE INDEX IF NOT EXISTS idx_sku_code_trgm ON pms_sku USING gin (code gin_trgm_ops);

-- ========================================================
-- 4. 价格与阶梯价域 (Pricing)
-- ========================================================
CREATE TABLE IF NOT EXISTS price_tier (
    id BIGINT PRIMARY KEY,
    sku_id VARCHAR(64) NOT NULL REFERENCES pms_sku(id),
    min_quantity INT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    label VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_tier_sku_qty ON price_tier(sku_id, min_quantity);

-- ========================================================
-- 5. 交易与订单域 (Trade)
-- ========================================================
CREATE TABLE IF NOT EXISTS oms_order (
    id VARCHAR(64) PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    company_id VARCHAR(64) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending_approval',
    approval_status VARCHAR(32) NOT NULL DEFAULT 'pending',
    payment_method VARCHAR(32) NOT NULL,
    subtotal NUMERIC(14, 2) NOT NULL,
    list_subtotal NUMERIC(14, 2) NOT NULL,
    promotion_discount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    shipping_fee NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    tax NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total NUMERIC(14, 2) NOT NULL,
    currency VARCHAR(16) NOT NULL DEFAULT 'CNY',
    shipping_address_snapshot JSONB NOT NULL,
    invoice_snapshot JSONB,
    note TEXT,
    requested_delivery_date DATE,
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX IF NOT EXISTS idx_order_company_status ON oms_order(company_id, status);

CREATE TABLE IF NOT EXISTS oms_order_line (
    id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL REFERENCES oms_order(id),
    product_id VARCHAR(64) NOT NULL,
    sku_id VARCHAR(64) NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    product_title VARCHAR(255) NOT NULL,
    sku_name VARCHAR(255) NOT NULL,
    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    unit VARCHAR(32) NOT NULL,
    quantity INT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    list_unit_price NUMERIC(12, 2),
    promotion_discount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    subtotal NUMERIC(14, 2) NOT NULL,
    pricing_snapshot JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_order_line_order ON oms_order_line(order_id);

-- ========================================================
-- 6. 审批域 (Approval)
-- ========================================================
CREATE TABLE IF NOT EXISTS act_approval_request (
    id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL UNIQUE,
    order_no VARCHAR(64) NOT NULL,
    company_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    current_node VARCHAR(64) NOT NULL DEFAULT '待审批',
    submitted_by VARCHAR(64) NOT NULL,
    timeline JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_approval_company_status ON act_approval_request(company_id, status);
