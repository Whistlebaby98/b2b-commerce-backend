-- =========================================================================
-- Flyway Migration Script: V1.0.3__trade_and_finance_schema.sql
-- Description: 交易采购车、企业地址与开票资质建表及初始化数据脚本
-- Author: b2b-commerce-backend
-- =========================================================================

-- 1. 采购车行项表 (oms_cart_item)
CREATE TABLE IF NOT EXISTS oms_cart_item (
    id VARCHAR(64) PRIMARY KEY,
    company_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    sku_id VARCHAR(64) NOT NULL REFERENCES pms_sku(id),
    quantity INT NOT NULL DEFAULT 1,
    selected BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_company_user_sku UNIQUE (company_id, user_id, sku_id)
);
CREATE INDEX IF NOT EXISTS idx_cart_company_user ON oms_cart_item(company_id, user_id);

-- 2. 企业地址档案表 (fms_company_address)
CREATE TABLE IF NOT EXISTS fms_company_address (
    id VARCHAR(64) PRIMARY KEY,
    company_id VARCHAR(64) NOT NULL REFERENCES org_company(id),
    label VARCHAR(128) NOT NULL,
    kind VARCHAR(32) NOT NULL DEFAULT 'shipping',
    recipient VARCHAR(128) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    province VARCHAR(64) NOT NULL,
    city VARCHAR(64) NOT NULL,
    district VARCHAR(64) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    postal_code VARCHAR(32),
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX IF NOT EXISTS idx_addr_company_kind ON fms_company_address(company_id, kind);

-- 3. 企业开票资质档案表 (fms_invoice_profile)
CREATE TABLE IF NOT EXISTS fms_invoice_profile (
    id VARCHAR(64) PRIMARY KEY,
    company_id VARCHAR(64) NOT NULL REFERENCES org_company(id),
    type VARCHAR(32) NOT NULL DEFAULT 'vat_special',
    title VARCHAR(255) NOT NULL,
    tax_id VARCHAR(64) NOT NULL,
    bank_name VARCHAR(128),
    bank_account VARCHAR(64),
    registered_address VARCHAR(255),
    registered_phone VARCHAR(32),
    receive_email VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'active',
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX IF NOT EXISTS idx_invoice_company_status ON fms_invoice_profile(company_id, status);

-- =========================================================================
-- 种子数据初始化 (Seed Data)
-- =========================================================================

-- 1. 初始化企业地址档案
INSERT INTO fms_company_address (id, company_id, label, kind, recipient, phone, province, city, district, detail, postal_code, is_default)
VALUES 
    ('addr-shenzhen-factory', 'company-lantu', '深圳工厂（默认）', 'shipping', '陈志远', '138****6721', '广东省', '深圳市', '宝安区', '福海街道和平社区桥和路 18 号蓝图智造园 2 栋 1 楼', '518103', true),
    ('addr-dongguan-warehouse', 'company-lantu', '东莞仓库', 'shipping', '周敏', '139****2458', '广东省', '东莞市', '长安镇', '乌沙社区振安东路 129 号 A 区仓储中心', '523850', false),
    ('addr-hq-billing', 'company-lantu', '总部开票地址', 'billing', '林悦', '0755-8234-1902', '广东省', '深圳市', '南山区', '粤海街道科苑南路 2666 号中国华润大厦 20 层', '518054', true)
ON CONFLICT (id) DO NOTHING;

-- 2. 初始化企业开票资质档案
INSERT INTO fms_invoice_profile (id, company_id, type, title, tax_id, bank_name, bank_account, registered_address, registered_phone, receive_email, status, is_default)
VALUES
    ('invoice-special-default', 'company-lantu', 'vat_special', '深圳市蓝图精密制造有限公司', '91440300MA5XXXXXX', '招商银行深圳科技园支行', '7559 **** **** 8812', '广东省深圳市南山区科技园', '0755-8234-1902', 'finance@lantu-mfg.example', 'active', true),
    ('invoice-normal-default', 'company-lantu', 'vat_normal', '深圳市蓝图精密制造有限公司', '91440300MA5XXXXXX', NULL, NULL, NULL, NULL, 'finance@lantu-mfg.example', 'active', false)
ON CONFLICT (id) DO NOTHING;

-- 3. 初始化采购车示例商品
INSERT INTO oms_cart_item (id, company_id, user_id, sku_id, quantity, selected)
VALUES
    ('cart-item-1', 'company-lantu', 'user-lin-yue', 'sku-nsk-6205zz', 12, true),
    ('cart-item-2', 'company-lantu', 'user-lin-yue', 'sku-sick-wl12g3', 2, true),
    ('cart-item-3', 'company-lantu', 'user-lin-yue', 'sku-yaskawa-sgm7j-04', 1, true)
ON CONFLICT (id) DO NOTHING;

-- 4. 初始化示例订单 (对齐前端 MOCK_ORDERS)
INSERT INTO oms_order (
    id, order_no, company_id, created_by, status, approval_status, payment_method,
    subtotal, list_subtotal, promotion_discount, shipping_fee, tax, total, currency,
    shipping_address_snapshot, invoice_snapshot, note, requested_delivery_date, submitted_at
) VALUES (
    'ord-202609140012',
    'PO202609140012',
    'company-lantu',
    'user-lin-yue',
    'pending_approval',
    'pending',
    'credit_account',
    2808.00,
    3222.00,
    414.00,
    0.00,
    0.00,
    2808.00,
    'CNY',
    '{
        "id": "addr-shenzhen-factory",
        "label": "深圳工厂（默认）",
        "kind": "shipping",
        "recipient": "陈志远",
        "phone": "138****6721",
        "province": "广东省",
        "city": "深圳市",
        "district": "宝安区",
        "detail": "福海街道和平社区桥和路 18 号蓝图智造园 2 栋 1 楼",
        "postalCode": "518103"
    }'::jsonb,
    '{
        "id": "invoice-special-default",
        "type": "vat_special",
        "title": "深圳市蓝图精密制造有限公司",
        "taxId": "91440300MA5XXXXXX",
        "bankName": "招商银行深圳科技园支行",
        "bankAccount": "7559 **** **** 8812",
        "registeredAddress": "广东省深圳市南山区科技园",
        "registeredPhone": "0755-8234-1902",
        "receiveEmail": "finance@lantu-mfg.example"
    }'::jsonb,
    '生产线急需传感器替换件，请优先排单',
    '2026-09-22',
    '2026-09-14 10:24:00+08'
) ON CONFLICT (id) DO NOTHING;

-- 5. 初始化示例订单行项
INSERT INTO oms_order_line (
    id, order_id, product_id, sku_id, sku_code, product_title, sku_name,
    attributes, unit, quantity, unit_price, list_unit_price, promotion_discount, subtotal,
    pricing_snapshot
) VALUES (
    'line-202609140012-1',
    'ord-202609140012',
    'prod-sick-wl12g3',
    'sku-sick-wl12g3',
    'SKU-SICK-WL12G3',
    'SICK 光电传感器 WL12G-3',
    'WL12G-3 · PNP 常开',
    '{"检测距离": "100 mm", "输出": "PNP", "接插件": "M12"}'::jsonb,
    '件',
    18,
    156.00,
    179.00,
    0.00,
    2808.00,
    '{"skuId": "sku-sick-wl12g3", "originalUnitPrice": 179.00, "finalUnitPrice": 156.00, "quantity": 18}'::jsonb
) ON CONFLICT (id) DO NOTHING;

-- 6. 初始化审批请求
INSERT INTO act_approval_request (
    id, order_id, order_no, company_id, status, current_node, submitted_by, timeline
) VALUES (
    'appr-202609140012',
    'ord-202609140012',
    'PO202609140012',
    'company-lantu',
    'pending',
    '待部门主管审批',
    'user-lin-yue',
    '[
        {
            "action": "submitted",
            "operatorName": "林悦 (采购经理)",
            "comment": "订单已提交，等待主管审批",
            "timestamp": "2026-09-14T02:24:00Z"
        }
    ]'::jsonb
) ON CONFLICT (id) DO NOTHING;
