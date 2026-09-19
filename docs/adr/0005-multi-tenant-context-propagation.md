---
status: accepted
date: 2026-09-19
---

# 基于 Java 21 ScopedValue/TTL 与 MQ Header 染色的多租户上下文传递机制

在多租户对公商城中，所有的选品、阶梯价、购物车和订单均隶属于 `ActiveOrganizationContext`。为了确保上下文在 Java 21 虚拟线程、异步任务及后台 MQ 消费中安全穿透且不发生跨企业数据泄露，我们决定：在 Web 拦截层通过 `ScopedValue` / `TransmittableThreadLocal` 绑定当前企业与用户上下文；在发送 MQ 消息时将租户信息作为 Header 染色传递，消费端统一拦截恢复上下文，使后台异步逻辑同样受到 MyBatis-Plus 租户插件的行级过滤保护。

## Considered Options

- **Java 21 ScopedValue/TTL + MQ 染色（已采纳）**：内存安全、支持虚拟线程跨栈传递，MQ 异步消费全链路受租户隔离保护。
- **普通 ThreadLocal**：在虚拟线程、线程池复用或 MQ 异步场景下极易丢失上下文或污染线程，带来严重的数据越权漏洞。

## Consequences

- 所有的对外 MQ 消息发送器必须自动装配上下文拦截器，将 `company_id` 和 `user_id` 注入消息元数据。
- 所有的 MQ 监听器入口必须包含租户上下文恢复切面。
