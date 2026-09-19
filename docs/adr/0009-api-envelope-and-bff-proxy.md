---
status: accepted
date: 2026-09-19
---

# 统一 API 响应信封、OpenAPI 规范与 Next.js 代理转发

为了保证前后端契约清晰、错误码语义化且联调无跨域阻碍，我们决定：
1. 后端所有 RESTful API 均采用统一响应信封 `ApiResponse<T>`（包含 code、message、data、timestamp）；
2. 基于 SpringDoc OpenAPI 3.0 自动生成可视化接口文档与 OpenAPI Spec，便于前端生成 TypeScript 类型；
3. 本地开发与联调期间，在前端 Next.js 中通过 `next.config.mjs` 的 `rewrites()` 将 `/api/:path*` 代理至后端服务，规避浏览器 CORS 跨域问题。

## Considered Options

- **统一 ApiResponse + Next.js rewrites 代理（已采纳）**：契约统一，前端开发无需配置复杂 CORS，错误处理集中一致。
- **裸数据返回 + 全局后端 CORS 放行**：错误信息缺乏统一结构，生产环境多域部署仍需额外配置。

## Consequences

- 所有的 Controller 必须返回统一的 `ApiResponse<T>` 或由全局 ResponseBodyAdvice 统一包装。
- 业务异常必须通过 `@RestControllerAdvice` 统一捕获并转换为规范的业务错误响应。
