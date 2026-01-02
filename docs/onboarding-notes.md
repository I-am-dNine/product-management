# Onboarding Notes – Design Reasoning & Trade-offs

---

## Design Decisions

### 1. 并发库存问题的处理方式

- 采用 DB 原子更新（`UPDATE … WHERE stock >= 1`）避免超卖
- 并发安全由 DB 保证，而非 Java `synchronized`
- 测试验证：并发请求下仅允许 1 次成功扣减

### 2. Transaction 的职责与边界

- Transaction 用于保证多步骤业务的一致性
- 并非所有并发问题都需要 transaction
- Transaction boundary 放在 Service 层，便于组合与测试

### 3. Rollback 行为验证

- 使用 `@Transactional` + `RuntimeException` 验证 rollback
- 测试确认异常发生时库存不会被扣减
- 加深对 Spring transaction 生效条件的理解

### 4. 设计取舍总结

- **DB 原子性**解决并发写入问题
- **Transaction**解决业务一致性问题
- **测试先行**，用验证而非假设确认系统行为

---

## Testing & Transaction Strategy

### Transaction at Service Layer

#### Transaction Boundary

- Transaction boundary is located at `ProductService`
- `@Transactional` defines a **business use-case scope**
- Transaction behavior (commit / rollback) is part of service semantics

#### Verification Strategy

- Rollback behavior is verified via `ProductServiceTest`
- Tests use `@SpringBootTest` to run with Spring-managed transactions
- Explicit assertion confirms transaction is active when expected

---

### Repository Testing

#### Purpose

- Validate JDBC behavior, not business logic

#### What Repository Tests Cover

- SQL execution correctness (`INSERT / SELECT / DELETE`)
- JDBC parameter binding
- `RowMapper` correctness
- Schema alignment

#### Test Type

- **Integration Test**
- Uses real DataSource (H2)
- No transaction logic asserted here

---

### Service Testing Strategy

#### ProductServiceTest

- Enabled in Phase 2
- Verifies:
  - Transaction boundary presence
  - Rollback behavior on failure
- Does **not** mock transaction behavior

#### Design Rationale

- Transaction is a business concern, not an implementation detail
- Testing rollback = validating business invariants

---

### Historical Note – Phase 1 (Deprecated)

#### TxService Experiment

- Transaction boundary was temporarily placed in `ProductTxService`
- **Purpose:**
  - Isolate and validate Spring transaction semantics
  - Confirm rollback behavior independently of orchestration logic

#### Outcome

- Transaction behavior verified successfully
- Approach deprecated after confirmation
- Responsibility merged back into `ProductService`

---

### Test Classification Rules

- **Repository** → Integration Test
- **Transaction Logic** → Spring-managed Service Test
- **Orchestration-only logic** → not tested in isolation
