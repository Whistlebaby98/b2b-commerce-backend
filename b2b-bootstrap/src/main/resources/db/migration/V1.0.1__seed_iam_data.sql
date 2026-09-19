-- =========================================================================
-- Flyway Migration Script: V1.0.1__seed_iam_data.sql
-- Description: 初始化演示客户企业与买方用户种子数据 (对齐前端 mock-data.ts)
-- Author: b2b-commerce-backend
-- =========================================================================

-- 1. 插入演示企业: 深圳市蓝图精密制造有限公司 (company-lantu)
INSERT INTO org_company (
    id, name, short_name, tax_id, customer_tier, is_verified,
    credit_limit, credit_used, payment_term_days, currency,
    created_at, updated_at, deleted
) VALUES (
    'company-lantu',
    '深圳市蓝图精密制造有限公司',
    '蓝图精密制造',
    '91440300MA5F8N7X2K',
    'gold',
    true,
    1280000.00,
    409600.00,
    30,
    'CNY',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    false
) ON CONFLICT (id) DO UPDATE SET
    credit_limit = EXCLUDED.credit_limit,
    credit_used = EXCLUDED.credit_used,
    updated_at = CURRENT_TIMESTAMP;

-- 2. 插入演示买方用户: 林悦 (user-lin-yue)
-- 初始密码为: 123456 (BCrypt 哈希: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi)
INSERT INTO org_user (
    id, name, email, password_hash, department, avatar_text,
    created_at, updated_at, deleted
) VALUES (
    'user-lin-yue',
    '林悦',
    'procurement@lantu-mfg.example',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    '采购部',
    '蓝图',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    false
) ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    updated_at = CURRENT_TIMESTAMP;

-- 3. 插入用户与企业隶属关系 (org_membership)
INSERT INTO org_membership (
    id, user_id, company_id, role, created_at, updated_at
) VALUES (
    10001,
    'user-lin-yue',
    'company-lantu',
    'buyer',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id, company_id) DO UPDATE SET
    role = EXCLUDED.role,
    updated_at = CURRENT_TIMESTAMP;
