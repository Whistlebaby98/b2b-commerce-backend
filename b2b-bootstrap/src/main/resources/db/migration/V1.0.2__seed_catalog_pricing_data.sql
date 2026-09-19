-- =========================================================================
-- Flyway Migration Script: V1.0.2__seed_catalog_pricing_data.sql
-- Description: 初始化商品分类、核心工业品 SPU/SKU 与阶梯价格种子数据 (对齐前端 mock-data.ts)
-- Author: b2b-commerce-backend
-- =========================================================================

-- 1. 插入 5 大标准品类
INSERT INTO pms_category (id, name, description, sort_order, created_at, updated_at) VALUES
('precision-parts', '精密零件', '轴承、联轴器及机械传动件', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('industrial-automation', '工业自动化', '传感器、控制器与伺服系统', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('electrical-components', '电气元件', '低压电器、继电器与配电产品', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('tools-and-consumables', '工具与耗材', '生产现场常用工具和耗材', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('office-and-safety', '办公与安全', '劳保、办公及仓储用品', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;

-- 2. 插入商品 1: NSK 深沟球轴承 6205ZZ
INSERT INTO pms_product (
    id, slug, title, subtitle, brand, category_id, description, unit,
    default_sku_id, tone, accent, mark, badges, tags, status, is_featured, created_at, updated_at
) VALUES (
    'prod-nsk-6205zz',
    'nsk-6205zz',
    'NSK 深沟球轴承 6205ZZ',
    '日本精工 · 进口原装',
    'NSK',
    'precision-parts',
    '适用于电机、减速机及一般工业传动设备的双面防尘深沟球轴承。',
    '个',
    'sku-nsk-6205zz',
    '#e4f4f3',
    '#199d97',
    'NSK',
    '["agreement", "bestseller", "spot"]'::jsonb,
    '["轴承", "进口", "深沟球"]'::jsonb,
    'active',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, updated_at = CURRENT_TIMESTAMP;

INSERT INTO pms_sku (
    id, product_id, code, name, attributes, unit, price, list_price,
    currency, stock, availability, lead_time_label, status, weight_kg, created_at, updated_at
) VALUES (
    'sku-nsk-6205zz',
    'prod-nsk-6205zz',
    'SKU-NSK-6205ZZ',
    '6205ZZ / 双面铁盖',
    '{"内径": "25 mm", "外径": "52 mm", "宽度": "15 mm"}'::jsonb,
    '个',
    18.60,
    22.40,
    'CNY',
    2480,
    'in_stock',
    '现货 · 48 小时内发出',
    'available',
    0.13,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, stock = EXCLUDED.stock, updated_at = CURRENT_TIMESTAMP;

INSERT INTO price_tier (id, sku_id, min_quantity, unit_price, label, created_at, updated_at) VALUES
(20001, 'sku-nsk-6205zz', 1, 18.60, '协议价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20002, 'sku-nsk-6205zz', 50, 17.48, '50+ 阶梯价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20003, 'sku-nsk-6205zz', 100, 16.74, '100+ 阶梯价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET unit_price = EXCLUDED.unit_price, updated_at = CURRENT_TIMESTAMP;

-- 3. 插入商品 2: SICK 光电传感器 WL12G-3
INSERT INTO pms_product (
    id, slug, title, subtitle, brand, category_id, description, unit,
    default_sku_id, tone, accent, mark, badges, tags, status, is_featured, created_at, updated_at
) VALUES (
    'prod-sick-wl12g3',
    'sick-wl12g3',
    'SICK 光电传感器 WL12G-3',
    '德国西克 · M12 接插件',
    'SICK',
    'industrial-automation',
    '紧凑型漫反射光电传感器，支持稳定的高速物料检测。',
    '件',
    'sku-sick-wl12g3',
    '#eaf0fb',
    '#527bc6',
    'SICK',
    '["agreement", "bestseller"]'::jsonb,
    '["传感器", "光电", "M12"]'::jsonb,
    'active',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, updated_at = CURRENT_TIMESTAMP;

INSERT INTO pms_sku (
    id, product_id, code, name, attributes, unit, price, list_price,
    currency, stock, availability, lead_time_label, status, weight_kg, created_at, updated_at
) VALUES (
    'sku-sick-wl12g3',
    'prod-sick-wl12g3',
    'SKU-SICK-WL12G3',
    'WL12G-3 · PNP 常开',
    '{"检测距离": "100 mm", "输出": "PNP", "接插件": "M12"}'::jsonb,
    '件',
    156.00,
    179.00,
    'CNY',
    316,
    'low_stock',
    '预计 3–5 个工作日',
    'available',
    0.08,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, stock = EXCLUDED.stock, updated_at = CURRENT_TIMESTAMP;

INSERT INTO price_tier (id, sku_id, min_quantity, unit_price, label, created_at, updated_at) VALUES
(20004, 'sku-sick-wl12g3', 1, 156.00, '协议价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20005, 'sku-sick-wl12g3', 10, 149.80, '10+ 阶梯价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20006, 'sku-sick-wl12g3', 30, 143.52, '30+ 阶梯价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET unit_price = EXCLUDED.unit_price, updated_at = CURRENT_TIMESTAMP;

-- 4. 插入商品 3: 安川 Σ-7 伺服电机 SGM7J
INSERT INTO pms_product (
    id, slug, title, subtitle, brand, category_id, description, unit,
    default_sku_id, tone, accent, mark, badges, tags, status, is_featured, created_at, updated_at
) VALUES (
    'prod-yaskawa-sgm7j',
    'yaskawa-sgm7j-04',
    '安川 Σ-7 伺服电机 SGM7J',
    '400W · 带 20-bit 编码器',
    'YASKAWA',
    'industrial-automation',
    '高响应交流伺服电机，适合精密定位及高速往复运动场景。',
    '台',
    'sku-yaskawa-sgm7j-04',
    '#fff0e9',
    '#ef846c',
    'Σ7',
    '["agreement", "promotion"]'::jsonb,
    '["伺服", "电机", "400W"]'::jsonb,
    'active',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, updated_at = CURRENT_TIMESTAMP;

INSERT INTO pms_sku (
    id, product_id, code, name, attributes, unit, price, list_price,
    currency, stock, availability, lead_time_label, status, weight_kg, created_at, updated_at
) VALUES (
    'sku-yaskawa-sgm7j-04',
    'prod-yaskawa-sgm7j',
    'SKU-YASK-SGM7J-04',
    'SGM7J-04A7A21 · 400 W',
    '{"功率": "400 W", "编码器": "20-bit", "法兰": "80 mm"}'::jsonb,
    '台',
    1280.00,
    1450.00,
    'CNY',
    58,
    'preorder',
    '预计 7–10 个工作日',
    'available',
    2.40,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, stock = EXCLUDED.stock, updated_at = CURRENT_TIMESTAMP;

INSERT INTO price_tier (id, sku_id, min_quantity, unit_price, label, created_at, updated_at) VALUES
(20007, 'sku-yaskawa-sgm7j-04', 1, 1280.00, '协议价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20008, 'sku-yaskawa-sgm7j-04', 5, 1228.80, '5+ 阶梯价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20009, 'sku-yaskawa-sgm7j-04', 10, 1177.60, '10+ 阶梯价', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET unit_price = EXCLUDED.unit_price, updated_at = CURRENT_TIMESTAMP;
