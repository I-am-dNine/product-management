Product Management Backend API – Design Notes

1. Project Scope & Goal
This project implements a backend RESTful API using Spring Boot + JDBC with an RDBMS (MySQL in Docker).

The focus is not only CRUD, but also:
- Data correctness under concurrency
- Request idempotency(冪等性)
- Database load control under frequent reads
- Unit Test Validation function

2. Domain Model Design

Product
Represents a sellable item with inventory control.

Fields
- id
- name
- price
- stock
- created_at
- updated_at 

Order
Introduced to properly support idempotency and stock consumption.

Fields
- id
- idempotency_key (unique)
- product_id
- quantity
- status
- created_at

Although product CRUD is the main requirement, an Order entity is necessary to correctly model:
- duplicate submissions
- concurrent stock deduction
- idempotent behavior


3. RESTful API Design

Product APIs
- POST /products：新增产品
- PUT /products/{id}：修改产品
- DELETE /products/{id}：移除产品
- GET /products/{id}：查询单一产品
- GET /products：查询全部产品

Order API
- POST /orders
  - Header: Idempotency-Key
  - Body: productId, quantity


5. Idempotency Handling Strategy
   
Problem
Clients may accidentally send the same request multiple times, which must not create duplicate orders.

Solution
- When creating an order, the Client must provide the `Idempotency-Key`
- Set in the orders table: UNIQUE (idempotency_key)

Flow
1.Receive request with Idempotency-Key
2.Query order by key
    - Exists → return existing result
    - Not exists → create order
This ensures:
- Safe retries 
- No duplicate orders 
- Deterministic behavior


6. Concurrency & Inventory Safety
   
Problem
Multiple orders consuming the same product stock may cause overselling.

Chosen Approach (Atomic Update)

UPDATE product
SET stock = stock - :quantity
WHERE id = :productId
AND stock >= :quantity

- If update count = 1 → success
- If update count = 0 → insufficient stock

Reasoning
- Atomic operations to avoid competing conditions 
- No additional locking tables 
- JDBC is easy to implement 
- Clear and reliable behavior


7. Read Load Reduction Strategy
   
Problem
Frequently queried product data may increase DB load.

Solution
- Local in-memory cache using ConcurrentHashMap
- Cache scope: getProductById
- TTL-based expiration (short-lived)
- Cache eviction on update/delete


8. Data Access Strategy
- Use NamedParameterJdbcTemplate 
- Avoid ORM for better SQL visibility 
- Explicit SQL for:
  - inventory update
  - idempotency checks
  - transactional behavior
This keeps:
- SQL logic explicit 
- Behavior predictable 
- Debugging straightforward


9. Logging Strategy (log4j2)
- API entry / exit logs
- Order creation attempts
- Idempotency hit detection
- Stock deduction failure events


10. Testing Strategy
    
Unit Tests (JUnit 5)
Test Case 1 – Idempotency
- Same Idempotency-Key 
- Multiple requests 
- Only one order is created

Test Case 2 – Concurrency Safety
- Limited stock 
- Multiple concurrent order attempts 
- No negative stock 
- Only valid number of orders succeed


11. Java Version Consideration
Java 25 is used for development


12. Conclusion
This project demonstrates:
- RESTful API fundamentals
- JDBC-based data access
- Idempotent request handling
- Concurrency-safe inventory logic

