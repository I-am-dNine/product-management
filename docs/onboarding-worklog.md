# Onboarding Worklog

**Product Management Mini Project**  
**Duration:** 1 Week

---

## 2025/12/29 | Environment Setup & Basic CRUD

### 🎯 目标

- 建立可运行的 Spring Boot 后台服务
- 完成 Product 管理的基本 CRUD
- 打通 JDBC 与 RDBMS（Docker）

### 🛠 实作内容

- 初始化 Spring Boot（Maven）专案结构
- 采用三层架构：
  - **Controller**（REST API）
  - **Service**（业务逻辑）
  - **Repository**（JDBC / SQL）
- 使用 Docker Compose 启动 MySQL
- 配置 JDBC DataSource，成功连线 DB
- 建立 Product table schema
- 实作 Product CRUD API：
  - `POST /products`
  - `GET /products/{id}`
  - `GET /products`
  - `PUT /products/{id}`
  - `DELETE /products/{id}`
- 使用 curl 实测 API 行为

### 🐞 遇到的问题与处理

**问题 1：** API 呼叫时报错 Table doesn't exist

**处理：**
- 确认 schema 是否初始化
- 理解 MySQL container 启动顺序与 DB 名称

**问题 2：** JDBC 连线成功但资料未写入

**处理：**
- 检查 SQL、transaction auto-commit 行为
- 确认 Repository 层职责单一

### 🧠 当日学习重点

- Spring Boot 专案的基本启动流程
- JDBC 与 DB 的真实执行路径
- 三层架构的职责边界（不要混写）

---

## 2025/12/31 | Testing Strategy & Concurrency Safety

### 🎯 目标

- 让系统行为「可被验证」，而不只是「看起来能跑」
- 处理并发库存一致性问题（避免超卖）

### 🛠 实作内容

- 补齐 Product CRUD 的测试覆盖
- 明确测试分层策略：
  - **Service 层：** Unit Test（Mockito，不启动 Spring context）
  - **Repository 层：** Integration Test（H2 in-memory DB）
- 使用 `./mvnw test` 理解 Maven test lifecycle 与 log
- 实作并发库存测试：
  - 多线程同时扣减同一商品库存
  - 验证最终不会出现 `stock < 0`

### 🧪 并发测试设计说明

**测试明确约束业务不变量：**
- 初始 `stock = 1`
- 并发 2 个请求
- 结果应为：
  - 1 success
  - 1 failure
  - 最终 `stock = 0`

**并发安全策略：**
- 使用 DB 原子更新（`UPDATE … WHERE stock >= 1`）
- 由 DB 保证一致性，而非 Java `synchronized`

### 🧠 关键设计决策

- 并发问题优先交由 DB 处理，而不是 JVM 锁
- 测试不是验证实现，而是验证「业务规格（spec）」
- Repository test 必须能验证 SQL / schema / mapping

### 🧠 当日学习重点

- Unit Test vs Integration Test 的真实差异
- 并发安全 ≠ transaction
- DB 原子性是高并发系统的第一道防线

---

## 2026/1/2 | Transaction Boundary & Rollback Verification

### 🎯 目标

- 理解 transaction 的真实职责
- 用测试「验证」而不是「假设」rollback 行为

### 🛠 实作内容

- 在 Service 层加入 `@Transactional`
- 设计实验方法：
  1. 先扣库存
  2. 再主动抛出 `RuntimeException`
- 撰写 Service Test 验证：
  - 异常发生后，库存是否 rollback
- **实测结果：** rollback 正确生效（库存回到原值）

### 🔍 Transaction 行为理解

**`@Transactional` 负责：**
- transaction lifecycle
- commit / rollback
- **不负责并发安全**

**rollback 生效条件：**
- `public` method
- 非 self-invocation
- `RuntimeException`（未被 catch）

### 🧠 关键设计取舍

- **并发安全：** DB 原子操作负责
- **业务一致性：** Service 层 transaction 负责
- transaction boundary 应放在「业务组合层」

### 🧠 当日学习重点

- transaction ≠ 万能解法
- boundary 放错层会导致 rollback 失效
- 用测试验证 transaction，是理解 Spring 的最快方式

---

## 总结 | 目前 Onboarding 小专案进度

### ✅ 已完成

- CRUD 功能完成
- 测试分层明确
- 并发库存安全已验证
- transaction rollback 行为已实证

### ⏭ 下一步可延伸

- 幂等性（重复送单）
- 订单流程（Order + Inventory）
- 查询负载优化（cache / read strategy）
