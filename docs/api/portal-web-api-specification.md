# 企采云对公商城 (NOVA B2B) 前端买方门户 API 接口规范与契约文档

> **面向对象**：前端工程团队 (`b2b-portal-web`)  
> **服务版本**：`v1.0.0-MVP`  
> **更新时间**：2026-09-20  
> **后端代码库**：`b2b-commerce-backend` (Branch: `develop`)

---

## 1. 全局设计规范与约定 (Standards & Conventions)

### 1.1 服务基础路径
- **本地开发环境**：`http://127.0.0.1:8088`
- 前端可通过 Next.js `rewrites` 或反向代理将 `/api/*` 转发至后端服务。

### 1.2 统一响应信封格式 (ApiResponse)
所有 HTTP 接口无论成功还是失败，均统一封装为如下 JSON 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1726758400000
}
```

- `code`：业务状态码。`200` 表示业务处理成功；非 `200` 表示发生业务异常或参数校验失败；
- `message`：提示消息。成功时一般为 `"success"`，异常时为可展示给用户的明确语义提示；
- `data`：实际业务数据负载。若无返回数据则为 `null`；
- `timestamp`：服务端 Unix 毫秒时间戳。

### 1.3 统一鉴权与多租户请求头 (Authentication & Headers)
除公开接口（如登录、注册、商品目录浏览）外，所有受保护的业务接口均需携带以下两个 Header：

| 请求头名称 | 格式 / 示例 | 说明 |
| :--- | :--- | :--- |
| `Authorization` | `Bearer eyJhbGciOi...` | JWT 访问令牌（登录或注册成功后返回的 `accessToken`） |
| `X-Organization-Id` | `company-lantu` | 当前代表的客户企业组织 ID（用于服务端行级多租户数据隔离） |

> **提示**：用户所属企业信息可在登录响应或 `GET /api/v1/auth/session` 中获取。

### 1.4 常见业务状态码与错误处理

| Code | 状态标识 | 业务含义与前端处理建议 |
| :--- | :--- | :--- |
| `200` | `SUCCESS` | 请求成功 |
| `400` | `BAD_REQUEST` | 参数格式校验失败或缺少必填字段（Toast 提示 `message`） |
| `401` | `UNAUTHORIZED` | 访问令牌过期或未登录（前端应清除本地 Token 并重定向至 `/login`） |
| `403` | `FORBIDDEN` | 当前用户在该企业下无权操作此资源 |
| `404` | `NOT_FOUND` | 请求的商品、订单或组织不存在 |
| `2001` | `MEMBERSHIP_INVALID`| 用户未加入目标客户企业 |
| `2002` | `TENANT_NOT_FOUND` | 客户企业已被禁用或不存在 |
| `5001` | `CHECKOUT_TOKEN_INVALID` | 结算防重 Token 已失效或已被核销（引导用户返回购物车重新核对结算） |
| `5002` | `CART_EMPTY` | 购物车未勾选任何商品 |
| `5003` | `CREDIT_LIMIT_EXCEEDED` | 企业授信额度不足，无法提交账期订单 |
| `5004` | `STOCK_INSUFFICIENT` | 商品库存不足 |

---

## 2. 接口详细清单 (API Endpoints)

---

### 2.0 认证与企业空间 (Authentication & IAM)

#### 2.0.1 买方用户登录
- **接口说明**：通过企业邮箱与密码登录，成功后颁发双 Token、买方用户信息及所属企业列表。
- **请求方法**：`POST`
- **请求路径**：`/api/v1/auth/login`
- **请求体**：
```json
{
  "email": "procurement@lantu-mfg.example",
  "password": "demo"
}
```
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsIn...",
    "expiresIn": 7200,
    "user": {
      "id": "user-lin-yue",
      "name": "林悦",
      "role": "buyer",
      "department": "生产采购部",
      "avatarText": "蓝图",
      "companyId": "company-lantu"
    },
    "currentCompany": {
      "id": "company-lantu",
      "name": "深圳市蓝图精密制造有限公司",
      "shortName": "蓝图精密",
      "taxId": "91440300MA5F8N7X2K",
      "customerTier": "gold",
      "isVerified": true,
      "creditLimit": 1280000.00,
      "creditUsed": 409600.00,
      "paymentTermDays": 30,
      "currency": "CNY"
    },
    "availableCompanies": [
      {
        "id": "company-lantu",
        "name": "深圳市蓝图精密制造有限公司",
        "shortName": "蓝图精密",
        "taxId": "91440300MA5F8N7X2K",
        "customerTier": "gold",
        "isVerified": true,
        "creditLimit": 1280000.00,
        "creditUsed": 409600.00,
        "paymentTermDays": 30,
        "currency": "CNY"
      }
    ]
  },
  "timestamp": 1726758400000
}
```

#### 2.0.2 买方自主注册
- **接口说明**：新买方用户提交姓名、企业邮箱与密码，自动完成注册并直接签发登录凭证。
- **请求方法**：`POST`
- **请求路径**：`/api/v1/auth/register`
- **请求体**：
```json
{
  "name": "王强",
  "email": "wang.qiang@example.com",
  "password": "password123",
  "companyName": "深圳市蓝图精密制造有限公司"
}
```
- **响应体**：结构同 `2.0.1 买方用户登录`。

#### 2.0.3 获取当前会话快照 (SessionSnapshot)
- **接口说明**：页面初次加载或刷新时调用，恢复当前认证状态与企业组织上下文。
- **请求方法**：`GET`
- **请求路径**：`/api/v1/auth/session`
- **请求头**：`Authorization: Bearer <token>`, `X-Organization-Id: <companyId>`
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "authenticated": true,
    "email": "procurement@lantu-mfg.example",
    "user": {
      "id": "user-lin-yue",
      "name": "林悦",
      "role": "buyer",
      "department": "生产采购部",
      "avatarText": "蓝图",
      "companyId": "company-lantu"
    },
    "company": {
      "id": "company-lantu",
      "name": "深圳市蓝图精密制造有限公司",
      "shortName": "蓝图精密",
      "taxId": "91440300MA5F8N7X2K",
      "customerTier": "gold",
      "isVerified": true,
      "creditLimit": 1280000.00,
      "creditUsed": 409600.00,
      "paymentTermDays": 30,
      "currency": "CNY"
    }
  },
  "timestamp": 1726758400000
}
```

#### 2.0.4 切换企业组织上下文
- **接口说明**：用户在归属的多家企业间切换代表企业，生成新上下文的访问令牌。
- **请求方法**：`POST`
- **请求路径**：`/api/v1/auth/switch-context`
- **请求头**：`Authorization: Bearer <token>`
- **请求体**：
```json
{
  "targetCompanyId": "company-lantu"
}
```

#### 2.0.5 刷新访问令牌
- **接口说明**：当 Access Token 即将过期时，使用长期 Refresh Token 换取新令牌。
- **请求方法**：`POST`
- **请求路径**：`/api/v1/auth/refresh-token`
- **请求体**：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsIn...",
  "companyId": "company-lantu"
}
```

---

### 2.1 商品中心与选品 (Catalog & Bulk Buying)

#### 2.1.1 查询商品分类列表
- **接口说明**：获取全部分类及各分类下商品统计。
- **请求方法**：`GET`
- **请求路径**：`/api/v1/categories`
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "precision-parts",
      "name": "精密零件",
      "description": "轴承、联轴器及机械传动件",
      "productCount": 128
    },
    {
      "id": "industrial-automation",
      "name": "工业自动化",
      "description": "传感器、控制器与伺服系统",
      "productCount": 86
    }
  ],
  "timestamp": 1726758400000
}
```

#### 2.1.2 多条件分页搜索商品目录
- **接口说明**：商品列表浏览与高级搜索。
- **请求方法**：`GET`
- **请求路径**：`/api/v1/products`
- **请求参数 (Query)**：
  - `query`：关键词模糊搜索（商品标题、品牌、副标题）；
  - `categoryId`：分类 ID（如 `precision-parts`）；
  - `brand`：品牌（如 `NSK`）；
  - `availability`：库存状态（如 `in_stock`）；
  - `sortBy`：排序规则（`recommended` 推荐, `newest` 最新, `price_asc` 价格升序, `price_desc` 价格降序）；
  - `page`：当前页码（从 1 开始，默认 1）；
  - `pageSize`：每页记录数（默认 20）。
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": "prod-nsk-6205zz",
        "slug": "nsk-6205zz",
        "title": "NSK 深沟球轴承 6205ZZ",
        "subtitle": "日本精工 · 进口原装",
        "brand": "NSK",
        "categoryId": "precision-parts",
        "category": "精密轴承",
        "description": "适用于电机、减速机及一般工业传动设备的双面防尘深沟球轴承。",
        "unit": "个",
        "defaultSkuId": "sku-nsk-6205zz",
        "status": "active",
        "tone": "#e4f4f3",
        "accent": "#199d97",
        "mark": "NSK",
        "badges": ["agreement", "bestseller", "spot"],
        "tags": ["轴承", "进口", "深沟球"],
        "isFeatured": true,
        "createdAt": "2026-07-02T08:00:00Z",
        "updatedAt": "2026-09-14T08:00:00Z",
        "skus": [
          {
            "id": "sku-nsk-6205zz",
            "productId": "prod-nsk-6205zz",
            "code": "SKU-NSK-6205ZZ",
            "name": "6205ZZ / 双面铁盖",
            "attributes": { "内径": "25 mm", "外径": "52 mm", "宽度": "15 mm" },
            "unit": "个",
            "price": 18.60,
            "listPrice": 22.40,
            "currency": "CNY",
            "stock": 2480,
            "availability": "in_stock",
            "leadTimeLabel": "现货 · 48 小时内发出",
            "status": "available",
            "weightKg": 0.13,
            "priceTiers": [
              { "minQuantity": 1, "unitPrice": 18.60, "label": "协议价" },
              { "minQuantity": 50, "unitPrice": 17.48, "label": "50+ 阶梯价" },
              { "minQuantity": 100, "unitPrice": 16.74, "label": "100+ 阶梯价" }
            ]
          }
        ]
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1
  },
  "timestamp": 1726758400000
}
```

#### 2.1.3 查询指定商品详情
- **接口说明**：查询商品完整档案、下属所有规格 SKU 及阶梯价格。
- **请求方法**：`GET`
- **请求路径**：`/api/v1/products/{id}`
- **路径参数**：`id`（商品 ID 或 slug，如 `prod-nsk-6205zz`）
- **响应体**：同 `2.1.2` 中的单件商品对象。

#### 2.1.4 批量采购清单解析 (Bulk Resolve)
- **接口说明**：买方在批量采购页粘贴多行 SKU 文本，后端逐行匹配并返回可采购行、阶梯单价与关联商品对象。
- **请求方法**：`POST`
- **请求路径**：`/api/v1/products/bulk-resolve`
- **请求体**：
```json
{
  "content": "SKU-NSK-6205ZZ    20\nSKU-SICK-WL12G3   5\nSKU-UNKNOWN-001   2"
}
```
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "successCount": 2,
    "errorCount": 1,
    "rows": [
      {
        "lineNumber": 1,
        "raw": "SKU-NSK-6205ZZ    20",
        "skuCode": "SKU-NSK-6205ZZ",
        "quantity": 20,
        "sku": {
          "id": "sku-nsk-6205zz",
          "code": "SKU-NSK-6205ZZ",
          "name": "6205ZZ / 双面铁盖",
          "unit": "个",
          "price": 18.60,
          "stock": 2480,
          "availability": "in_stock"
        },
        "product": {
          "id": "prod-nsk-6205zz",
          "title": "NSK 深沟球轴承 6205ZZ",
          "tone": "#e4f4f3",
          "accent": "#199d97",
          "mark": "NSK"
        },
        "unitPrice": 18.60,
        "error": null
      },
      {
        "lineNumber": 3,
        "raw": "SKU-UNKNOWN-001   2",
        "skuCode": "SKU-UNKNOWN-001",
        "quantity": 2,
        "sku": null,
        "product": null,
        "unitPrice": null,
        "error": "SKU 编码不存在或已下架停售"
      }
    ]
  },
  "timestamp": 1726758400000
}
```

---

### 2.2 营销与促销中心 (Promotions)

#### 2.2.1 查询进行中的促销活动
- **接口说明**：获取当前生效的促销规则列表（用于 `/promotions` 页面与首页 Banner）。
- **请求方法**：`GET`
- **请求路径**：`/api/v1/promotions`
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "promo-free-freight",
      "title": "华南区域免运费",
      "description": "订单满 ¥2,000，深圳仓配送免运费。",
      "type": "free_shipping",
      "scope": "cart",
      "startsAt": "2026-09-01T00:00:00Z",
      "endsAt": "2026-09-30T23:59:59Z",
      "isActive": true,
      "minSubtotal": 2000.00,
      "stackable": true,
      "priority": 5
    },
    {
      "id": "promo-september-coupon",
      "code": "NOVA50",
      "title": "九月采购补贴",
      "description": "订单满 ¥5,000 减 ¥50。",
      "type": "coupon",
      "scope": "cart",
      "startsAt": "2026-09-01T00:00:00Z",
      "endsAt": "2026-09-30T23:59:59Z",
      "isActive": true,
      "minSubtotal": 5000.00,
      "discountAmount": 50.00,
      "maxDiscount": 50.00,
      "stackable": true,
      "priority": 3
    },
    {
      "id": "promo-sensor-volume",
      "title": "传感器批量采购优惠",
      "description": "工业传感器满 10 件再享 4% 折扣。",
      "type": "tier_price",
      "scope": "category",
      "categoryIds": ["industrial-automation"],
      "startsAt": "2026-09-01T00:00:00Z",
      "endsAt": "2026-09-30T23:59:59Z",
      "isActive": true,
      "minQuantity": 10,
      "tiers": [
        { "minQuantity": 10, "discountRate": 0.04 },
        { "minQuantity": 30, "discountRate": 0.08 }
      ],
      "stackable": false,
      "priority": 2
    },
    {
      "id": "promo-september-agreement",
      "title": "金牌客户专享协议价",
      "description": "已根据企业等级自动匹配，无需输入优惠码。",
      "type": "percentage",
      "scope": "catalogue",
      "startsAt": "2026-01-01T00:00:00Z",
      "endsAt": "2026-12-31T23:59:59Z",
      "isActive": true,
      "discountRate": 0.06,
      "stackable": false,
      "priority": 1
    }
  ],
  "timestamp": 1726758400000
}
```

#### 2.2.2 查询指定促销活动详情
- **请求方法**：`GET`
- **请求路径**：`/api/v1/promotions/{id}`
- **响应体**：单件活动详情对象。

---

### 2.3 采购车管理 (Cart)

#### 2.3.1 获取当前企业采购车
- **接口说明**：获取当前企业的采购车行项明细与金额汇总。
- **请求方法**：`GET`
- **请求路径**：`/api/v1/cart`
- **请求头**：`Authorization`, `X-Organization-Id`
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "cart_company-lantu",
    "companyId": "company-lantu",
    "currency": "CNY",
    "items": [
      {
        "id": "cart-item-001",
        "productId": "prod-nsk-6205zz",
        "skuId": "sku-nsk-6205zz",
        "skuCode": "SKU-NSK-6205ZZ",
        "productTitle": "NSK 深沟球轴承 6205ZZ",
        "skuName": "6205ZZ / 双面铁盖",
        "unit": "个",
        "unitPrice": 18.60,
        "listUnitPrice": 22.40,
        "quantity": 12,
        "selected": true,
        "subtotal": 223.20
      }
    ],
    "totals": {
      "itemCount": 12,
      "subtotal": 223.20,
      "listSubtotal": 268.80,
      "promotionDiscount": 45.60,
      "shippingFee": 0.00,
      "tax": 0.00,
      "total": 223.20,
      "currency": "CNY"
    }
  },
  "timestamp": 1726758400000
}
```

#### 2.3.2 添加商品至采购车
- **请求方法**：`POST`
- **请求路径**：`/api/v1/cart/items`
- **请求体**：
```json
{
  "productId": "prod-nsk-6205zz",
  "skuId": "sku-nsk-6205zz",
  "quantity": 10
}
```

#### 2.3.3 修改采购车行数量
- **请求方法**：`PUT`
- **请求路径**：`/api/v1/cart/items/{id}/quantity`
- **路径参数**：`id`（采购车行 ID）
- **请求体**：
```json
{
  "quantity": 50
}
```

#### 2.3.4 切换单项商品勾选状态
- **请求方法**：`PUT`
- **请求路径**：`/api/v1/cart/items/{id}/select`

#### 2.3.5 批量勾选商品 (全选 / 全不选)
- **请求方法**：`PUT`
- **请求路径**：`/api/v1/cart/items/batch-select`
- **请求体**：
```json
{
  "allSelected": true
}
```

#### 2.3.6 删除采购车单项
- **请求方法**：`DELETE`
- **请求路径**：`/api/v1/cart/items/{id}`

#### 2.3.7 清空采购车
- **请求方法**：`DELETE`
- **请求路径**：`/api/v1/cart/clear`

---

### 2.4 结算确认与订单中心 (Checkout & Orders)

#### 2.4.1 结算金额试算与防重 Token 预领 (Preview)
- **接口说明**：进入 `/checkout` 页面时调用。后端试算当前选中行项总价、优惠分摊、运费，预检企业授信额度，并生成单次原子核销的 `checkoutToken`。
- **请求方法**：`POST`
- **请求路径**：`/api/v1/checkout/preview`
- **请求头**：`Authorization`, `X-Organization-Id`
- **请求体**（可选，直接下单场景可传特定商品）：
```json
{}
```
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "checkoutToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenExpiresInSeconds": 1800,
    "items": [ ... ],
    "totals": {
      "itemCount": 12,
      "subtotal": 223.20,
      "listSubtotal": 268.80,
      "promotionDiscount": 45.60,
      "shippingFee": 0.00,
      "tax": 0.00,
      "total": 223.20,
      "currency": "CNY"
    },
    "selectedAddress": {
      "id": "addr-shenzhen-factory",
      "label": "深圳工厂（默认）",
      "recipient": "陈志远",
      "phone": "138****6721",
      "province": "广东省",
      "city": "深圳市",
      "district": "宝安区",
      "detail": "福海街道和平社区桥和路 18 号蓝图智造园 2 栋 1 楼"
    },
    "selectedInvoice": {
      "id": "invoice-special-default",
      "type": "vat_special",
      "title": "深圳市蓝图精密制造有限公司",
      "taxId": "91440300MA5F8N7X2K",
      "receiveEmail": "finance@lantu-mfg.example"
    },
    "creditCheck": {
      "companyId": "company-lantu",
      "creditLimit": 1280000.00,
      "creditUsed": 409600.00,
      "availableCredit": 870400.00,
      "requestAmount": 223.20,
      "isSufficient": true
    }
  },
  "timestamp": 1726758400000
}
```

#### 2.4.2 提交创建采购订单
- **接口说明**：确认结算信息后点击提交。强校验并原子核销 `checkoutToken`，CAS 预占企业授信额度，生成不可变订单与审批工作单。订单初始状态必须为 `pending_approval`。
- **请求方法**：`POST`
- **请求路径**：`/api/v1/orders`
- **请求头**：`Authorization`, `X-Organization-Id`
- **请求体**：
```json
{
  "checkoutToken": "550e8400-e29b-41d4-a716-446655440000",
  "addressId": "addr-shenzhen-factory",
  "invoiceId": "invoice-special-default",
  "paymentMethod": "credit_account",
  "note": "请于工作日送货，入库前联系仓库。",
  "requestedDeliveryDate": "2026-09-25"
}
```
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "ord-202609140012",
    "orderNo": "PO202609140012",
    "companyId": "company-lantu",
    "createdBy": "user-lin-yue",
    "status": "pending_approval",
    "approvalStatus": "pending",
    "paymentMethod": "credit_account",
    "lines": [
      {
        "id": "line-001",
        "productId": "prod-sick-wl12g3",
        "skuId": "sku-sick-wl12g3",
        "skuCode": "SKU-SICK-WL12G3",
        "productTitle": "SICK 光电传感器 WL12G-3",
        "skuName": "WL12G-3 · PNP 常开",
        "attributes": { "检测距离": "100 mm", "输出": "PNP" },
        "unit": "件",
        "quantity": 18,
        "unitPrice": 156.00,
        "listUnitPrice": 179.00,
        "promotionDiscount": 0.00,
        "subtotal": 2808.00
      }
    ],
    "totals": {
      "subtotal": 2808.00,
      "listSubtotal": 3222.00,
      "promotionDiscount": 0.00,
      "shippingFee": 0.00,
      "tax": 0.00,
      "total": 2808.00,
      "currency": "CNY"
    },
    "shippingAddress": {
      "id": "addr-shenzhen-factory",
      "label": "深圳工厂（默认）",
      "recipient": "陈志远",
      "phone": "138****6721",
      "province": "广东省",
      "city": "深圳市",
      "district": "宝安区",
      "detail": "福海街道和平社区桥和路 18 号蓝图智造园 2 栋 1 楼"
    },
    "invoice": {
      "id": "invoice-special-default",
      "type": "vat_special",
      "title": "深圳市蓝图精密制造有限公司",
      "taxId": "91440300MA5F8N7X2K",
      "receiveEmail": "finance@lantu-mfg.example"
    },
    "note": "请于工作日送货，入库前联系仓库。",
    "requestedDeliveryDate": "2026-09-25",
    "createdAt": "2026-09-14T06:12:00Z",
    "updatedAt": "2026-09-14T08:20:00Z",
    "submittedAt": "2026-09-14T08:20:00Z"
  },
  "timestamp": 1726758400000
}
```

#### 2.4.3 分页查询订单中心列表
- **请求方法**：`GET`
- **请求路径**：`/api/v1/orders`
- **请求参数 (Query)**：
  - `status`：状态筛选（`all`, `pending_approval`, `processing`, `completed`）；
  - `query`：关键词搜索（订单号 `PO...` 或商品品名）；
  - `page`：页码（默认 1）；
  - `pageSize`：每页记录数（默认 20）。
- **响应体**：`PageResult<OrderDTO>`。

#### 2.4.4 查询订单详情与快照
- **请求方法**：`GET`
- **请求路径**：`/api/v1/orders/{id}`
- **响应体**：完整的 `OrderDTO` 对象。

---

### 2.5 企业结算档案与授信 (Finance)

#### 2.5.1 查询当前企业授信概况
- **请求方法**：`GET`
- **请求路径**：`/api/v1/finance/credit`
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "companyId": "company-lantu",
    "companyName": "深圳市蓝图精密制造有限公司",
    "creditLimit": 1280000.00,
    "creditUsed": 409600.00,
    "availableCredit": 870400.00,
    "currency": "CNY"
  },
  "timestamp": 1726758400000
}
```

#### 2.5.2 查询企业地址档案列表
- **请求方法**：`GET`
- **请求路径**：`/api/v1/finance/addresses`
- **响应体**：`List<AddressDTO>`。

#### 2.5.3 新增企业地址
- **请求方法**：`POST`
- **请求路径**：`/api/v1/finance/addresses`
- **请求体**：
```json
{
  "label": "东莞第二仓库",
  "kind": "shipping",
  "recipient": "周敏",
  "phone": "13900139000",
  "province": "广东省",
  "city": "东莞市",
  "district": "长安镇",
  "detail": "乌沙社区振安东路 129 号 A 区仓储中心",
  "postalCode": "523850",
  "isDefault": false
}
```

#### 2.5.4 设为默认地址
- **请求方法**：`PUT`
- **请求路径**：`/api/v1/finance/addresses/{id}/default`

#### 2.5.5 删除地址
- **请求方法**：`DELETE`
- **请求路径**：`/api/v1/finance/addresses/{id}`

#### 2.5.6 查询企业开票资质列表
- **请求方法**：`GET`
- **请求路径**：`/api/v1/finance/invoices`
- **响应体**：`List<InvoiceDTO>`。

#### 2.5.7 新增开票资质
- **请求方法**：`POST`
- **请求路径**：`/api/v1/finance/invoices`
- **请求体**：
```json
{
  "type": "vat_special",
  "title": "深圳市蓝图精密制造有限公司",
  "taxId": "91440300MA5F8N7X2K",
  "bankName": "招商银行深圳科技园支行",
  "bankAccount": "755902188812",
  "registeredAddress": "广东省深圳市南山区科技园",
  "registeredPhone": "0755-8234-1902",
  "receiveEmail": "finance@lantu-mfg.example",
  "isDefault": true
}
```

#### 2.5.8 设为默认开票资质
- **请求方法**：`PUT`
- **请求路径**：`/api/v1/finance/invoices/{id}/default`

#### 2.5.9 删除开票资质
- **请求方法**：`DELETE`
- **请求路径**：`/api/v1/finance/invoices/{id}`

---

### 2.6 订单审批流追踪 (Approvals)

#### 2.6.1 查询企业审批单列表
- **请求方法**：`GET`
- **请求路径**：`/api/v1/approvals`
- **请求参数 (Query)**：`status`（可选，`pending`, `approved`, `rejected`）

#### 2.6.2 根据订单 ID 查询审批节点轨迹
- **请求方法**：`GET`
- **请求路径**：`/api/v1/approvals/{orderId}`
- **响应体**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "appr_ord-202609140012",
    "orderId": "ord-202609140012",
    "companyId": "company-lantu",
    "status": "pending",
    "currentStep": 1,
    "totalSteps": 2,
    "nodes": [
      {
        "nodeName": "部门主管审批",
        "approverRole": "approver",
        "status": "pending",
        "comment": null
      },
      {
        "nodeName": "财务总监终审",
        "approverRole": "finance",
        "status": "waiting",
        "comment": null
      }
    ]
  },
  "timestamp": 1726758400000
}
```

---

## 3. 前端集成指引 (Frontend Integration Guide)

1. **统一 API 客户端（建议使用 Axios 或原生 Fetch 封装）**：
   - 自动在请求拦截器中读取当前 Session 中的 `accessToken` 并设置 `Authorization: Bearer <token>`；
   - 自动在请求拦截器中读取当前选中的企业 ID 并设置 `X-Organization-Id: <companyId>`；
   - 在响应拦截器中判断 `res.data.code === 200`，若非 200 抛出业务错误并在 UI 抛出 Toast；
   - 当捕获到 `401` 时，若存在 `refreshToken`，尝试调用 `/api/v1/auth/refresh-token` 无感续期，若失败则清空会话并路由至 `/login`。
2. **渐进式替换 Mock 数据**：
   - 第一步：在 `store-provider.tsx` 中将 `MOCK_PRODUCTS` 替换为 `GET /api/v1/products`；
   - 第二步：将 `MOCK_PROMOTIONS` 替换为 `GET /api/v1/promotions`；
   - 第三步：将 `addresses` 与 `invoices` 替换为 `GET /api/v1/finance/addresses` 与 `GET /api/v1/finance/invoices`；
   - 第四步：将 `cart` 与 `orders` 替换为 `/api/v1/cart` 与 `/api/v1/orders` 的增删改查。
