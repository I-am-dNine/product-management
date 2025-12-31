1. 当前 Transaction Boundary （Phase 1）
• Boundary 在 ProductTxService
• 以 @SpringBootTest 验证 rollback 行为

2. 为什么冻结 ProductServiceTest
• 当前仅为 orchestration，无业务决策
• 避免为暂时架构付出长期测试成本

3. Test 分类准则
• Repository → Integration Test
• Transaction Logic → Spring-managed Test
• Orchestrator 暂不测

4. 下一阶段计划（Phase 2）
• 合并 TxService → ProductService
• Transaction boundary 回到 ProductService
• 启用 ProductServiceTest，删除 TxServiceTest