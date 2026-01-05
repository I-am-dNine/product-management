# Product Management Backend (Onboarding Project)

## Purpose

This is an onboarding project to practice:
- Spring Boot + JDBC
- Concurrency-safe inventory handling
- Idempotent request design
- Testing & transaction boundaries

## Tech Stack

- Java 25
- Spring Boot
- JDBC (NamedParameterJdbcTemplate)
- MySQL (Docker)
- JUnit 5 / Mockito

## Implemented Features

- Product CRUD REST API
- Concurrency-safe stock deduction
- Unit & integration testing

## Architecture Design

### Three-Layer Architecture

採用標準三層架構：

```
Controller → Service → Repository
```

### Layer Responsibilities

- **Controller**: 處理 HTTP request / response
- **Service**: 商業邏輯與 Transaction boundary
- **Repository**: 純 JDBC 存取（SQL、RowMapper）

## Transaction Design

### Transaction Boundary

- `@Transactional` 僅放置於 Service 層
- Repository 層不感知 Transaction
- Transaction rollback 行為透過 Integration Test 驗證

### Design Principle

Transaction 是「業務流程」概念，而非資料存取細節。

## Concurrency & Data Consistency

### 庫存扣減策略

使用資料庫原子操作避免超賣：

```sql
UPDATE product
SET stock = stock - :qty
WHERE id = :id AND stock >= :qty
```

若影響筆數為 0，視為庫存不足。

## Testing Strategy

本專案明確區分測試層級，避免混用。

### Repository Integration Test

- 使用 `@SpringBootTest` + H2
- 驗證內容：
  - SQL 正確性
  - schema
  - RowMapper
  - 並發行為

### Service Unit Test

- 不啟動 Spring context
- 使用 Mockito
- 驗證內容：
  - 業務邏輯
  - Repository 呼叫行為

### Transaction Integration Test

- 啟動完整 Spring context
- 使用真實 Repository
- 驗證 exception 發生時 rollback 是否生效

## Learning Objectives

- 理解 JDBC 實際執行行為
- 理解 Spring Transaction 生效條件
- 分辨 Unit Test 與 Integration Test 的責任邊界


## Notes

- 详细的设计决策记录在 `/docs`
- 幂等性本專案為 onboarding / learning 專用，刻意保留實驗與驗證過程之設計紀錄，協助後續回顧與延伸。
