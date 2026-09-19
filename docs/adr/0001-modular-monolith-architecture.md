---
status: accepted
date: 2026-09-19
---

# 采用 Maven 多模块 DDD 模块化单体架构

为了在项目初期兼顾快速迭代与后期的平滑微服务演进，我们决定采用基于 Java 21 LTS + Spring Boot 3.3.x 的 Maven 多模块（Modular Monolith）工程结构，结合 MyBatis-Plus 3.5.x 实现数据持久化与多租户行级隔离。每个模块严格遵循 DDD 限界上下文划分，模块间禁止跨库/跨 DAO 依赖，仅通过明确的 API 门面与领域事件通信，从而在未来拆分微服务时无需重构业务核心代码。

## Considered Options

- **Maven 多模块 DDD 模块化单体**（已采纳）：边界清晰，单体期部署运维极简，后期拆分微服务成本最低。
- **单模块 Package 划分**：缺乏编译器级别的依赖约束，极易导致包间循环依赖与数据直连，后期拆分微服务极其痛苦。
- **起步即分布式微服务多代码库**：开发、部署、网络与分布式事务治理成本过高，严重拖慢初期 MVP 交付。

## Consequences

- 各业务模块（IAM、Catalog、Pricing、Trade、Approval、Finance）必须以独立 Maven Module 存在。
- 单体阶段通过 `b2b-bootstrap` 模块作为统一可执行 Jar 启动入口。
- 多租户通过 MyBatis-Plus `TenantLineInnerInterceptor` 在 SQL 执行期强制追加 `company_id` 过滤。
