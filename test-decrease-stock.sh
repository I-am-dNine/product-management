#!/bin/bash

BASE_URL="http://localhost:8080/products"
echo "=== 测试库存扣减 API ==="
echo ""

# 场景 0: 创建测试产品
echo "【步骤 0】创建测试产品..."
PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试商品",
    "price": 100.00,
    "stock": 10
  }')

PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
if [ -z "$PRODUCT_ID" ]; then
  # 如果创建失败，尝试获取现有产品
  PRODUCT_ID=$(curl -s "$BASE_URL" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
fi

if [ -z "$PRODUCT_ID" ]; then
  echo "❌ 无法获取产品 ID，请先创建产品"
  exit 1
fi

echo "✅ 产品 ID: $PRODUCT_ID"
echo ""

# 查看初始库存
echo "【查看初始库存】"
curl -s "$BASE_URL/$PRODUCT_ID" | grep -o '"stock":[0-9]*' | cut -d':' -f2
INITIAL_STOCK=$(curl -s "$BASE_URL/$PRODUCT_ID" | grep -o '"stock":[0-9]*' | cut -d':' -f2)
echo "初始库存: $INITIAL_STOCK"
echo ""

# 场景 1: 正常扣库存（不同 Idempotency-Key）
echo "【场景 1】正常扣库存（使用不同的 Idempotency-Key）"
echo "第一次扣库存 (key-001)..."
curl -s -X POST "$BASE_URL/$PRODUCT_ID/decrease-stock?idempotencyKey=key-001" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "第二次扣库存 (key-002)..."
curl -s -X POST "$BASE_URL/$PRODUCT_ID/decrease-stock?idempotencyKey=key-002" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "【验证场景 1】查看当前库存..."
CURRENT_STOCK=$(curl -s "$BASE_URL/$PRODUCT_ID" | grep -o '"stock":[0-9]*' | cut -d':' -f2)
echo "当前库存: $CURRENT_STOCK"
EXPECTED_STOCK=$((INITIAL_STOCK - 2))
if [ "$CURRENT_STOCK" -eq "$EXPECTED_STOCK" ]; then
  echo "✅ 场景 1 通过：库存正确扣减了 2 次"
else
  echo "❌ 场景 1 失败：期望库存 $EXPECTED_STOCK，实际 $CURRENT_STOCK"
fi
echo ""

# 场景 2: 重复送单（相同 Idempotency-Key）
echo "【场景 2】重复送单（使用相同的 Idempotency-Key）"
echo "第一次扣库存 (key-003)..."
curl -s -X POST "$BASE_URL/$PRODUCT_ID/decrease-stock?idempotencyKey=key-003" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
BEFORE_STOCK=$(curl -s "$BASE_URL/$PRODUCT_ID" | grep -o '"stock":[0-9]*' | cut -d':' -f2)
echo "扣库存前库存: $BEFORE_STOCK"

echo "第二次扣库存 (key-003 - 相同 key)..."
curl -s -X POST "$BASE_URL/$PRODUCT_ID/decrease-stock?idempotencyKey=key-003" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 1}' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "【验证场景 2】查看当前库存..."
AFTER_STOCK=$(curl -s "$BASE_URL/$PRODUCT_ID" | grep -o '"stock":[0-9]*' | cut -d':' -f2)
echo "扣库存后库存: $AFTER_STOCK"
if [ "$BEFORE_STOCK" -eq "$AFTER_STOCK" ]; then
  echo "✅ 场景 2 通过：重复请求被幂等处理，库存未变化"
else
  echo "❌ 场景 2 失败：库存被重复扣减了"
fi
echo ""

# 场景 3: 超卖防护（库存不足）
echo "【场景 3】超卖防护（库存不足）"
CURRENT_STOCK=$(curl -s "$BASE_URL/$PRODUCT_ID" | grep -o '"stock":[0-9]*' | cut -d':' -f2)
echo "当前库存: $CURRENT_STOCK"
echo "尝试扣减 $((CURRENT_STOCK + 1)) 个库存（超过现有库存）..."

RESPONSE=$(curl -s -X POST "$BASE_URL/$PRODUCT_ID/decrease-stock?idempotencyKey=key-oversell" \
  -H "Content-Type: application/json" \
  -d "{\"quantity\": $((CURRENT_STOCK + 1))}" \
  -w "\nHTTP Status: %{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP Status" | cut -d' ' -f3)
echo "HTTP Status: $HTTP_CODE"

echo ""
echo "【验证场景 3】查看库存是否被扣减..."
FINAL_STOCK=$(curl -s "$BASE_URL/$PRODUCT_ID" | grep -o '"stock":[0-9]*' | cut -d':' -f2)
echo "最终库存: $FINAL_STOCK"
if [ "$CURRENT_STOCK" -eq "$FINAL_STOCK" ]; then
  echo "✅ 场景 3 通过：超卖被阻止，库存未变化"
else
  echo "❌ 场景 3 失败：库存被错误扣减了"
fi
echo ""

echo "=== 测试完成 ==="

