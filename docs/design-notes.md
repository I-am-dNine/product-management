# Design Notes

本文件記錄 **目前仍然成立的設計結論**，不包含實驗過程與已淘汰方案。

---

## 1. 為何選擇 JDBC 而非 JPA

* 明確掌握 SQL 行為與資料庫特性
* 避免 ORM 隱藏 transaction / flush 行為
* 適合作為理解 Transaction 與並發的學習基礎

---

## 2. Transaction Boundary 設計

### 結論

* `@Transactional` 僅放在 Service 層
* Repository 為純資料存取元件

### 理由

* Transaction 是業務流程的語意邊界
* Repository 不應承擔流程控制責任

---

## 3. Service 設計原則

* Service 負責：

  * 業務流程順序
  * 例外拋出時機
  * Transaction 一致性

* Service 不負責：

  * SQL 細節
  * Row mapping

---

## 4. 並發安全策略

### 問題定義

* 多請求同時扣減同一商品庫存

### 採用方案

* 資料庫原子操作（UPDATE + 條件）
* 不使用 Java synchronized / lock

### 理由

* DB 為最終一致性來源
* 避免 JVM lock 在多實例環境失效

---

## 5. 測試分層設計

### Repository Integration Test

* 驗證：SQL、schema、RowMapper、並發行為

### Service Unit Test

* 驗證：商業邏輯與 Repository 呼叫

### Transaction Integration Test

* 驗證：rollback 是否真正生效

---

## 6. 已確認的設計原則（Summary）

* Transaction boundary ≠ Repository
* rollback 必須用 Integration Test 驗證
* 並發問題優先交給資料庫解決
* 測試的目的不是覆蓋率，而是驗證假設

---

## 備註

所有實驗過程、踩雷紀錄與替代方案已移至 appendix / debug notes，不再於此文件維護。
