---
status: accepted
date: 2026-09-19
---

# 采用 PostgreSQL 16 作为主业务关系型数据库

在关系型数据库选型中，我们决定采用 PostgreSQL 16 代替 MySQL 8.0 作为主业务数据库。PostgreSQL 16 在对公 B2B 商城场景下具备显著优势：其原生的 JSONB 与 GIN 倒排索引完美承载 SKU 动态属性与订单不可变快照（地址、发票、算价轨迹）；数据库内核级行级安全策略（Row-Level Security, RLS）为多租户隔离提供了底层防线；`FOR UPDATE SKIP LOCKED` 特性极大地提升了 Outbox 本地消息表并发拉取的吞吐量；结合 `pg_trgm` 扩展甚至能在前期免除 Elasticsearch 的运维成本即可实现高精度 SKU/OEM 码模糊检索。

## Considered Options

- **PostgreSQL 16（已采纳）**：JSONB 与复杂数据类型支持极佳，内核级 RLS 增强多租户安全，MVCC 并发性能优异且无 MySQL 间隙锁死锁困扰。
- **MySQL 8.0**：国内开发者认知度高，但在复杂 JSONB 深度检索、RLS 内核安全与高并发行锁争用（Gap Lock）上不及 PostgreSQL。

## Consequences

- 持久层配置需采用 PostgreSQL 方言与驱动（`org.postgresql:postgresql:42.7.4`）。
- 主键生成策略推荐使用 `BIGINT` 搭配雪花算法（Snowflake ID）或原生 `UUID` / `IDENTITY`。
- 对动态规格与快照字段（`attributes`、`pricing_snapshot`）采用 `jsonb` 类型建表并按需创建 GIN 索引。
