# Testing & Transaction Strategy

## Transaction at Service Layer

### Transaction Boundary

- Transaction boundary is located at `ProductService`
- `@Transactional` defines a **business use-case scope**
- Transaction behavior (commit / rollback) is part of service semantics

### Verification Strategy

- Rollback behavior is verified via `ProductServiceTest`
- Tests use `@SpringBootTest` to run with Spring-managed transactions
- Explicit assertion confirms transaction is active when expected

---

## Repository Testing

### Purpose

- Validate JDBC behavior, not business logic

### What Repository Tests Cover

- SQL execution correctness (`INSERT / SELECT / DELETE`)
- JDBC parameter binding
- `RowMapper` correctness
- Schema alignment

### Test Type

- Integration Test
- Uses real DataSource (H2)
- No transaction logic asserted here

---

## Service Testing Strategy

### ProductServiceTest

- Enabled in Phase 2
- Verifies:
    - Transaction boundary presence
    - Rollback behavior on failure
- Does **not** mock transaction behavior

### Design Rationale

- Transaction is a business concern, not an implementation detail
- Testing rollback = validating business invariants

---

## Historical Note – Phase 1 (Deprecated)

### TxService Experiment

- Transaction boundary was temporarily placed in `ProductTxService`
- Purpose:
    - Isolate and validate Spring transaction semantics
    - Confirm rollback behavior independently of orchestration logic

### Outcome

- Transaction behavior verified successfully
- Approach deprecated after confirmation
- Responsibility merged back into `ProductService`

---

## Test Classification Rules

- Repository → Integration Test
- Transaction Logic → Spring-managed Service Test
- Orchestration-only logic → not tested in isolation
