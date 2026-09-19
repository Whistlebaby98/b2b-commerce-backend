---
status: accepted
date: 2026-09-19
---

# 基于双 Token 与 X-Organization-Id Header 的无状态组织鉴权方案

为了支持买方用户无缝代表不同企业组织（`ActiveOrganizationContext`）进行采购，并适应未来移动端（企微/小程序）与第三方 ERP 对接，我们决定采用双 Token 无状态认证机制：买方用户通过账号密码认证后，获取短期 JWT Access Token（2小时，无状态验签）与长期 Redis Refresh Token（7天，可主动撤销）；所有业务请求在 Header 中携带 `Authorization: Bearer <token>` 与 `X-Organization-Id: <companyId>`，后端拦截器核验该用户对目标企业的 `OrganizationMembership` 权限后动态绑定当前企业上下文。

## Considered Options

- **双 Token + Header 传递组织上下文（已采纳）**：无状态易于横向扩展，组织切换无感知，天然支持多端与 OpenAPI 对接。
- **服务端有状态 Session + Cookie**：强依赖浏览器 Cookie 机制，在多端、跨域反向代理以及微服务拆分时难以维护。

## Consequences

- 前端在切换企业时仅需更新请求头中的 `X-Organization-Id`，无需重新登录。
- 用户登出或权限回收时，需在 Redis 中记录 Token 黑名单或废止 Refresh Token。
