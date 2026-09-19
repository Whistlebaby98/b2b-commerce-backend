# AGENTS.md - 企采云对公商城后端工程约束与行动指南

本文件是 AI Agent 在本代码库（`b2b-commerce-backend`）中进行架构设计、编码实现与重构时的**最高行为准则与硬性约束**。

---

## 1. 核心上下文与权威参考 (Context Pointers)

在编写任何业务逻辑前，必须严格对齐以下权威来源：
- **领域词汇与通用语言**：[`CONTEXT.md`](CONTEXT.md) —— 所有实体命名、概念必须与此保持一致，杜绝随意造词。
- **架构决策记录**：[`docs/adr/`](docs/adr/) —— 包含技术底座、多租户、计价、审批与数据库选型（ADR 0001 ~ 0010）。
- **前端契约与原型**：`D:\b2bCoding\b2b-portal-web` —— 前端已有领域模型（`domain.ts`）与 API 期望，后端数据结构必须与之兼容。

---

## 2. 架构红线与模块依赖约束 (Architectural Invariants)

本项目为 **Maven 多模块 DDD 模块化单体**，严格禁止破坏模块边界：

1. **单向依赖原则**：
   - 依赖流向：`b2b-bootstrap` -> `b2b-trade` / `b2b-approval` / `b2b-pricing` / `b2b-catalog` / `b2b-finance` / `b2b-iam` -> `b2b-common`。
   - **严禁跨模块循环依赖**。
2. **模块间数据访问防腐**：
   - **严禁跨模块直接注入 Mapper / DAO**（例如 `b2b-trade` 严禁直接注入 `CatalogMapper` 或直接查询商品库表）。
   - 跨模块数据读取必须调用目标模块 `api` 包下暴露的 `Facade` 接口。
   - 跨模块状态驱动与通知必须通过 **RocketMQ 领域事件** 异步解耦。
3. **分层整洁性**：
   - `domain` 包为纯 Java 领域模型与规则，严禁依赖 Spring MVC、MyBatis 等外部框架注解。
   - 业务校验下沉到领域实体或领域服务，禁止在 Controller 中堆砌业务计算。

---

## 3. 多租户与安全约束 (Multi-Tenancy Guardrails)

1. **组织上下文穿透**：
   - 所有业务请求必须从 `X-Organization-Id` Header 提取当前企业上下文，并由 `ScopedValue` / `TransmittableThreadLocal` 管理。
   - 异步任务与 MQ 消费必须恢复发送时的租户上下文。
2. **强制行级隔离**：
   - 所有涉及企业数据的表（订单、购物车、协议价、授信、地址）必须包含 `company_id` 字段。
   - 依赖 MyBatis-Plus `TenantLineInnerInterceptor` 自动追加过滤，严禁在业务代码中手动拼接有漏洞的裸 SQL。
   - 涉及 PostgreSQL 16 数据库操作时，优先激活内核级 RLS（Row-Level Security）。

---

## 4. 交易与计价业务不变量 (Trade & Pricing Invariants)

1. **不可变快照原则**：
   - 订单一经生成，其 `OrderLine` 中的成交单价、阶梯命中明细、优惠分摊、收货地址快照与开票资质快照**绝对不可变**。
   - 严禁后续因商品调价或企业协议变更而动态回算历史订单金额。
2. **纯内存无副作用算价**：
   - 计价流水线必须是无 I/O 的纯函数计算：前置批量查询价格与活动 -> 内存计算 -> 输出 `PricingSnapshot`。
3. **两阶段审批状态机**（严格遵循 ADR 0001 & ADR 0003）：
   - 买方提交订单后，初始状态必须且只能为 `pending_approval`（待审批）。
   - 买方端 API 严禁提供“通过”或“驳回”接口，审批推进由 `b2b-approval` 或外部系统回调触发。

---

## 5. 并发、幂等与事务一致性 (Concurrency & Transactions)

1. **结算防重复提交**：
   - 提交订单接口必须强校验 `checkout_token`，使用 Redis Lua 脚本原子核销。
2. **资产与库存扣减**：
   - 企业授信额度（`credit_limit` / `credit_used`）与可售库存扣减必须使用数据库 CAS 乐观更新：
     `UPDATE ... SET used = used + ? WHERE id = ? AND limit - used >= ?`
3. **可靠消息投递**（本地消息表 Outbox）：
   - 订单提交与 MQ 消息发布必须使用本地消息表（Outbox Pattern），在同一本地事务中落库，杜绝分布式一致性漏洞。

---

## 6. 代码风格与 API 规范 (Code & API Standards)

1. **技术栈基线**：
   - Java 21 LTS（全面支持虚拟线程，避免使用会导致 pin 住载体线程的 `synchronized`，优先使用 `ReentrantLock`）。
   - Spring Boot 3.3.x，PostgreSQL 16，MyBatis-Plus 3.5.7，RocketMQ 5.3.x。
2. **REST API 统一信封**：
   - 所有 Controller 响应必须统一为 `ApiResponse<T>`：
     ```json
     {
       "code": 200,
       "message": "success",
       "data": { ... },
       "timestamp": 1726758400000
     }
     ```
   - 异常必须通过 `@RestControllerAdvice` 转化为语义明确的业务错误码，禁止返回原始堆栈信息。
3. **数据类型一致性**：
   - 涉及金额必须使用 `BigDecimal`（保留 2 位小数，采用 `RoundingMode.HALF_UP`），严禁使用 `float` / `double`。
   - 动态规格与快照使用 PostgreSQL `jsonb`，Java 侧映射为类型安全的 DTO / Map。

---

## 7. 注释规范与功能设计留档 (Documentation, Comments & Design Archiving)

1. **代码注释与 JavaDoc 规范**：
   - **类与接口级注释**：所有新建类、接口必须包含完整的 JavaDoc，阐述其业务职责、所属限界上下文、关联的 ADR 或领域实体。
   - **方法级注释**：所有 `Facade`、`ApplicationService`、`DomainService` 的公开方法必须详尽说明入参业务含义、返回值、可能抛出的业务异常（`BizException`）与边界条件。
   - **复杂业务行内注释**：涉及阶梯算价流水线、金额均摊算法、CAS 并发控制、状态机转换等核心逻辑，必须书写解释性注释（解释“为什么这样设计”，而非单纯重复代码字面逻辑），禁止无解释的魔数（Magic Numbers）。

2. **功能设计与 SQL 设计留档机制**：
   - **前置设计留档**：开发任何新功能或模块前，必须在 `docs/features/` 目录下建立对应的设计文档（例如 `docs/features/01-order-checkout-design.md`），明确记录：
     - 业务背景与用例（与前端 `b2b-portal-web` 契约对齐）；
     - 领域模型对象与交互时序图（Mermaid 序列图）；
     - 接口契约定义（HTTP Method、URL、请求与响应体结构）；
     - 并发防重、分布式事务与异常降级方案。
   - **SQL 架构设计留档与版本化**：
     - 设计文档中必须单独设立“数据库与 SQL 设计”章节，详细阐述建表 DDL、字段设计考量、PostgreSQL `jsonb` 快照结构、索引设计策略（含 GIN 索引）与 RLS 安全策略；
     - 所有数据库变更必须同时沉淀为 Flyway 版本化迁移脚本（存放在 `b2b-bootstrap/src/main/resources/db/migration/V{version}__{description}.sql`），确保数据库模式可追溯、可重复构建。

---

## 8. 变更完成检查清单 (Definition of Done)

每次编写或重构代码后，必须逐项核对：
- [ ] 是否新增了跨模块直接调用 DAO 的反模式？
- [ ] 涉及租户数据的查询是否包含 `company_id` 过滤？
- [ ] 算价逻辑是否与前端 `domain.ts` 保持一致，并保留了算价快照？
- [ ] 涉及订单与资金的流程是否实现了幂等与并发安全？
- [ ] 是否编写了详尽的类级/方法级 JavaDoc 以及关键逻辑业务注释？
- [ ] 是否在 `docs/features/` 下完成了该功能的设计文档与 SQL 留档？
- [ ] 数据库变更是否同步提供了 Flyway 迁移脚本（`V*__*.sql`）？
- [ ] 代码是否通过编译且无循环依赖？

