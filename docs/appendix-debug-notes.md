# Appendix — Debug & Behavioral Notes

> 本文件用于记录 **不影响主文件理解，但对未来追查与复盘非常重要的工程细节**。
> 内容偏向观察、实验结果、踩坑与被推翻的假设。

---

## Day 1–2｜Testing & JDBC 行为观察

### Repository Integration Test 的真实验证范围

* 实际验证内容：

  * SQL 是否可执行（INSERT / UPDATE / SELECT）
  * NamedParameterJdbcTemplate parameter binding 行为
  * RowMapper 映射是否正确
  * schema 与 entity 是否一致
* 结论：Repository Test ≠ Unit Test，而是 **JDBC 行为验证**。

### H2 In-Memory DB 行为

* 每个 test method 会启动 **独立 H2 in-memory instance**
* HikariPool 在每个 test lifecycle 内创建与销毁
* 好处：

  * 测试彼此完全隔离
  * 不会互相污染数据

---

## Day 2｜Transaction & Rollback 观察

### @Transactional 生效条件

* 必须由 **Spring-managed bean** 呼叫
* 自己 class 内部方法互调，不会触发 proxy
* Repository 层不适合承担 transaction boundary

### Rollback 实际行为

* RuntimeException / unchecked exception 才会触发 rollback（预设）
* Checked exception 需明确配置 rollbackFor
* 透过 test 实际验证 rollback，而非仅凭注解假设

---

## Day 3｜Concurrency Test 实作细节

### 并发库存测试设计

* 场景：

  * 初始库存 = 1
  * 两个 thread 同时执行 decreaseStock
* 预期结果：

  * 1 成功
  * 1 失败
  * 最终库存 = 0

### AtomicInteger 使用原因

* successCount / failCount 用于统计 thread-safe 结果
* 避免在并发环境中出现计数错误

### 数据一致性结论

* 并发安全来自：

  * DB 层原子 SQL（UPDATE … WHERE stock >= ?）
* 而非：

  * JVM synchronized
  * application-level lock

---

## Debug Record｜踩坑与修正

### NPE：findById 中 Map.of()

* 现象：

  * 并发测试中出现 NullPointerException
* 原因：

  * Map.of 不允许 null value
  * ResultSet 某 column 为 null 时直接抛异常
* 修正方式：

  * 改为使用 HashMap 或明确处理 nullable field

---

## 被推翻的假设（重要）

* ❌ Repository 层加 @Transactional 可以解决并发
* ❌ JVM lock 能解决多实例或 DB-level 并发
* ❌ 没写测试也能“确定” transaction 正常

---

## 当前结论状态

* 并发库存一致性：已由测试验证
* Transaction rollback 行为：已由异常测试验证
* 本文件仅作为 **工程追查与行为记录**，不作为设计规范

> 设计结论请以 README.md 与 onboarding-notes.md 为准
