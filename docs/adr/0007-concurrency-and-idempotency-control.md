---
status: accepted
date: 2026-09-19
---

# 基于 Redisson 分布式锁、Token 幂等机制与 CAS 乐观扣减的并发控制

对公商城包含多用户并发选品加购、订单防重复提交以及授信额度（CreditLimit）并发预占等敏感交易环节。我们决定采用三层并发防护体系：
1. 结算阶段生成单次有效的 `checkout_token`，提交时通过 Redis Lua 脚本原子核销防重复提交；
2. 授信额度预占和库存扣减使用数据库原子 CAS 乐观更新（`UPDATE ... WHERE credit_limit - credit_used >= ?`）；
3. 对于大单批量结算等强互斥场景，使用 Redisson 分布式锁进行企业级粗粒度串行保护。

## Considered Options

- **三层并发防护（已采纳）**：在各层选用最合适的并发原语，既保障高吞吐又保障金融级强一致。
- **全局数据库悲观锁（FOR UPDATE）**：并发吞吐极低，易引发死锁并拖垮数据库连接池。

## Consequences

- 结算页必须在提交前调用 `preview` 接口预领 `checkout_token`。
- 业务异常必须明确区分“请勿重复提交”与“授信额度不足”等错误码。
