-- =========================================================================
-- Flyway Migration Script: V1.0.4__add_promotions_and_features.sql
-- Description: 营销促销活动中心表及预置活动数据（对齐前端 MVP）
-- Author: b2b-commerce-backend
-- =========================================================================

-- 1. 营销促销活动表 (mkt_promotion)
CREATE TABLE IF NOT EXISTS mkt_promotion (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(64),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(32) NOT NULL,
    scope VARCHAR(32) NOT NULL,
    category_ids JSONB,
    product_ids JSONB,
    sku_ids JSONB,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    discount_rate NUMERIC(10, 4),
    discount_amount NUMERIC(12, 2),
    min_subtotal NUMERIC(12, 2),
    min_quantity INT,
    max_discount NUMERIC(12, 2),
    tiers JSONB,
    stackable BOOLEAN NOT NULL DEFAULT false,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mkt_promo_active_priority ON mkt_promotion(is_active, priority DESC);
CREATE INDEX IF NOT EXISTS idx_mkt_promo_dates ON mkt_promotion(starts_at, ends_at);

-- 2. 预置 4 条对齐前端 MOCK_PROMOTIONS 的初始活动数据
INSERT INTO mkt_promotion (
    id, code, title, description, type, scope,
    category_ids, product_ids, sku_ids,
    starts_at, ends_at, is_active,
    discount_rate, discount_amount, min_subtotal, min_quantity, max_discount,
    tiers, stackable, priority, created_at, updated_at
) VALUES
(
    'promo-september-agreement',
    NULL,
    '金牌客户专享协议价',
    '已根据企业等级自动匹配，无需输入优惠码。',
    'percentage',
    'catalogue',
    NULL, NULL, NULL,
    '2026-01-01 00:00:00+08', '2026-12-31 23:59:59+08', true,
    0.06, NULL, NULL, NULL, NULL,
    NULL, false, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    'promo-sensor-volume',
    NULL,
    '传感器批量采购优惠',
    '工业传感器满 10 件再享 4% 折扣。',
    'tier_price',
    'category',
    '["industrial-automation"]'::jsonb, NULL, NULL,
    '2026-09-01 00:00:00+08', '2026-09-30 23:59:59+08', true,
    NULL, NULL, NULL, 10, NULL,
    '[{"minQuantity": 10, "discountRate": 0.04}, {"minQuantity": 30, "discountRate": 0.08}]'::jsonb,
    false, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    'promo-september-coupon',
    'NOVA50',
    '九月采购补贴',
    '订单满 ¥5,000 减 ¥50。',
    'coupon',
    'cart',
    NULL, NULL, NULL,
    '2026-09-01 00:00:00+08', '2026-09-30 23:59:59+08', true,
    NULL, 50.00, 5000.00, NULL, 50.00,
    NULL, true, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
(
    'promo-free-freight',
    NULL,
    '华南区域免运费',
    '订单满 ¥2,000，深圳仓配送免运费。',
    'free_shipping',
    'cart',
    NULL, NULL, NULL,
    '2026-09-01 00:00:00+08', '2026-09-30 23:59:59+08', true,
    NULL, NULL, 2000.00, NULL, NULL,
    NULL, true, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;
