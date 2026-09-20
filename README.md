# 企采云对公商城后端服务 (b2b-commerce-backend)

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-green.svg)](https://baomidou.com/)
[![RocketMQ](https://img.shields.io/badge/RocketMQ-5.3.x-red.svg)](https://rocketmq.apache.org/)

企业级对公采购商城后端核心服务，采用 **Maven 多模块 DDD 模块化单体 (Modular Monolith)** 架构。全面支持多租户行级数据隔离、纯内存动态阶梯价流水线、企业授信额度 CAS 预占与扣减、两阶段订单审批流、批量采购清单解析及全生命周期不可变订单快照。

---

## 1. 核心架构与技术栈

- **开发语言**：Java 21 LTS（全面启用 Virtual Threads 虚拟线程支持）
- **核心框架**：Spring Boot 3.3.x, Spring Framework 6.1.x
- **持久层中间件**：PostgreSQL 16（深度利用 JSONB、GIN 索引与内核级 RLS 行安全）
- **ORM 与数据访问**：MyBatis-Plus 3.5.7
- **数据库版本迁移**：Flyway 10.x（启动时自动执行增量迁移与种子数据初始化）
- **缓存与并发控制**：Redis 7.x（结算防重 Token 原子核销、分布式幂等锁）
- **异步解耦与事件驱动**：RocketMQ 5.3.x（本地消息表 Outbox 事务发布）
- **接口文档与契约**：SpringDoc OpenAPI 3.0 / Swagger UI
- **安全与身份认证**：JWT 双 Token 机制（Access Token + Refresh Token）、BCrypt 密码哈希

---

## 2. 运行环境与前置准备 (Prerequisites)

在本地启动或构建本工程前，请确保开发环境已安装并准备好以下组件：

| 依赖组件 | 推荐版本 | 说明 |
| :--- | :--- | :--- |
| **JDK** | `21 LTS` 及以上 | 推荐 [Eclipse Temurin 21](https://adoptium.net/) 或 Oracle OpenJDK 21 |
| **Maven** | `3.8.x` 或 `3.9.x` | 项目构建与依赖管理工具 |
| **PostgreSQL** | `16.x` | 关系型主数据库，需支持 JSONB 与并发控制 |
| **Redis** | `7.x` (或 6.2+) | 用于结算防重 Token、幂等性核销与热点缓存 |
| **RocketMQ** | `5.x` | 异步领域事件与本地消息表发布（可选，本地未安装时可在配置中指向远程或通过 Docker 快速启动） |
| **Docker & Docker Compose** | 最新稳定版 | **强烈推荐**，可一键拉起所有依赖中间件 |

---

## 3. 数据库与中间件配置指南

### 方式一：Docker Compose 一键启动中间件（推荐）

项目根目录下已提供完整的 `docker-compose.yml`，包含 PostgreSQL 16、Redis 7 与 RocketMQ 5.3。

1. 打开终端（PowerShell 或 CMD），进入项目根目录：
   ```bash
   cd d:\b2bCoding\b2b-commerce-backend
   ```
2. 执行一键启动命令：
   ```bash
   docker compose up -d
   ```
3. 查看各容器运行状态：
   ```bash
   docker compose ps
   ```
   * PostgreSQL 运行在 `localhost:5432`，默认库名 `b2b_commerce`，账号 `postgres`，密码 `postgres`；
   * Redis 运行在 `localhost:6379`，无密码；
   * RocketMQ NameServer 运行在 `localhost:9876`。

---

### 方式二：手动配置本地或远程数据库

如果您本地已有安装好的 PostgreSQL 实例，请按以下步骤准备：

1. **创建数据库**：
   连接至您的 PostgreSQL 16 服务（可通过 `psql` 或 Navicat / DBeaver / DataGrip），执行建库语句：
   ```sql
   CREATE DATABASE b2b_commerce WITH ENCODING = 'UTF8' LC_COLLATE = 'C' LC_CTYPE = 'C' TEMPLATE = template0;
   ```
2. **无需手动刷入 SQL 脚本（Flyway 自动化迁移）**：
   本项目集成了 Flyway 自动迁移机制。服务启动时会自动扫描 `b2b-bootstrap/src/main/resources/db/migration/` 目录下的所有增量脚本（`V1.0.0` ~ `V1.0.4`），自动创建表结构并预置演示种子数据：
   - `V1.0.0__init_schema.sql`：用户、企业、商品族、SKU 与阶梯价表；
   - `V1.0.1__seed_iam_data.sql`：演示企业（蓝图精密）与演示买方用户（林悦）；
   - `V1.0.2__seed_catalog_pricing_data.sql`：精密零件、自动化传感器等商品与多规格 SKU 阶梯价；
   - `V1.0.3__trade_and_finance_schema.sql`：采购车行项、收货地址档案、开票资质档案；
   - `V1.0.4__add_promotions_and_features.sql`：营销促销规则表及 4 条进行中的初始活动。

3. **检查配置文件与环境变量**：
   开发环境默认配置文件位于 `b2b-bootstrap/src/main/resources/application-dev.yml`。可通过系统环境变量或直接修改参数对齐本地配置：
   
   | 环境变量名称 | 默认值 | 说明 |
   | :--- | :--- | :--- |
   | `DB_HOST` | `localhost` | PostgreSQL 服务 IP 或主机名 |
   | `DB_PORT` | `5432` | PostgreSQL 端口 |
   | `DB_NAME` | `b2b_commerce` | 数据库名称 |
   | `DB_USER` | `postgres` | 数据库登录用户名 |
   | `DB_PASSWORD` | `postgres` | 数据库登录密码 |
   | `REDIS_HOST` | `localhost` | Redis 主机地址 |
   | `REDIS_PORT` | `6379` | Redis 端口 |
   | `REDIS_PASSWORD` | *(空)* | Redis 访问密码 |
   | `ROCKETMQ_NAMESRV` | `127.0.0.1:9876` | RocketMQ NameServer 地址 |

---

## 4. 项目编译、测试与启动

### 4.1 全量编译与单元测试
在项目根目录下执行 Maven 命令：

```powershell
# 执行全量单元测试与集成测试 (18/18 测试通过)
mvn clean test

# 快速跳过测试并打包构建整个工程
mvn clean install -DskipTests
```

### 4.2 启动后端服务

#### 方式 A：通过 Maven CLI 命令行启动
```powershell
mvn spring-boot:run -pl b2b-bootstrap
```

#### 方式 B：通过 IntelliJ IDEA / VS Code 启动
1. 打开 IDE 并将本项目作为 Maven 工程导入；
2. 确保 Project SDK 选择为 **JDK 21**；
3. 定位到启动类：
   `b2b-bootstrap/src/main/java/com/b2b/bootstrap/B2bCommerceApplication.java`；
4. 右键点击并选择 **Run 'B2bCommerceApplication'** 即可启动。

控制台打印如下信息即表示启动成功：
```text
2026-09-20T... [main] INFO  c.b.b.B2bCommerceApplication - Started B2bCommerceApplication in X.XXX seconds (process running for X.XXX)
```

---

## 5. 接口验证与默认测试账号

### 5.1 在线接口文档与 Swagger UI
服务启动后，在浏览器中打开以下链接即可查看完整 OpenAPI 3.0 接口文档并在线调试：
- **Swagger UI 交互式页面**：[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 规范 JSON 地址**：[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 5.2 预置测试账号（已完全对齐前端 b2b-portal-web）

| 属性 | 演示账号信息 |
| :--- | :--- |
| **企业邮箱** | `procurement@lantu-mfg.example` |
| **登录密码** | `123456`（或明文 `demo`） |
| **用户姓名** | 林悦 |
| **角色 / 部门** | 采购员 (`buyer`) / 生产采购部 |
| **所属企业** | 深圳市蓝图精密制造有限公司 (`company-lantu`) |
| **统一信用代码** | `91440300MA5F8N7X2K` |
| **客户等级** | 金牌客户 (`gold`)，已认证 |
| **授信额度** | 总额度：`¥1,280,000.00`，已用：`¥409,600.00`，可用：`¥870,400.00` |
| **结算账期** | 30 天 |

> **提示**：系统亦支持自主注册！可通过调用 `POST /api/v1/auth/register` 接口或前端登录页直接创建全新企业采购账户。

---

## 6. 前端项目 (`b2b-portal-web`) 联调指引

本后端服务已全面支持买方门户前端工程（位于 `D:\b2bCoding\b2b-portal-web`）。

### 6.1 前端反向代理配置
若前端使用 Next.js，可在 `next.config.mjs` 中配置 `rewrites`：
```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};

export default nextConfig;
```

### 6.2 请求头规范
除公开接口外，所有业务请求需携带：
```http
Authorization: Bearer <登录返回的 accessToken>
X-Organization-Id: company-lantu
Content-Type: application/json
```

### 6.3 详尽接口契约文档
详细入参、响应 JSON 示例及各页面字段映射，请直接查阅：
👉 **[`docs/api/portal-web-api-specification.md`](docs/api/portal-web-api-specification.md)**

---

## 7. 项目模块结构说明

本项目遵循严格的 DDD 单向依赖规范（`bootstrap` -> 业务模块 -> `common`，严禁跨模块循环依赖）：

```text
b2b-commerce-backend
├── b2b-bootstrap        # 系统引导根模块：Spring Boot 启动类、全局配置、Flyway 迁移脚本
├── b2b-iam              # 组织与认证中心：买方登录、注册、双 Token 颁发、企业组织上下文
├── b2b-catalog          # 商品选品中心：分类、SPU/SKU、动态规格、批量采购清单解析
├── b2b-pricing          # 计价与促销中心：纯内存阶梯算价流水线、营销促销活动规则
├── b2b-trade            # 交易核心中心：采购车增删改查、结算试算、防重 Token、订单主子表
├── b2b-finance          # 资金与结算中心：授信额度 CAS 并发扣减/释放、地址与开票资质
├── b2b-approval         # 审批工作流中心：两阶段订单审批状态机（买方仅查询进度）
├── b2b-common           # 通用基础设施：统一响应信封 ApiResponse、全局异常拦截、多租户上下文
├── docs/                # 架构设计与文档目录
│   ├── adr/             # 架构决策记录 (ADR 0001 ~ 0010)
│   ├── features/        # 功能设计留档与 SQL 设计留档
│   └── api/             # 面向前端的完整 API 接口契约文档
└── docker-compose.yml   # 本地一键拉起 PostgreSQL 16、Redis 7、RocketMQ 5.3
```

---

## 8. 常见问题排查 (FAQ)

1. **启动时提示 `Connection refused: connect` 连接数据库失败？**
   - 检查 PostgreSQL 服务是否在 `localhost:5432` 运行；
   - 检查 `b2b_commerce` 数据库是否已经建立；
   - 检查用户名和密码是否与 `application-dev.yml`（默认 `postgres`/`postgres`）一致。
2. **启动时提示 Redis 连接失败？**
   - 确保本地已启动 Redis 实例并在 `6379` 端口监听；或通过 `docker compose up -d redis` 启动。
3. **提交订单提示 `checkoutToken 不能为空` 或 `结算 Token 无效`？**
   - 遵循企业结算防重规范，提交订单前必须先调用 `POST /api/v1/checkout/preview` 预领单次原子 Token，再在 `POST /api/v1/orders` 中回传该 Token。
