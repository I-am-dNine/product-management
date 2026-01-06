#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[TEST] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
}

# Ensure Python 3 is installed for JSON parsing
if ! command -v python3 &> /dev/null; then
    error "Python 3 is required for this script to parse JSON."
    exit 1
fi

log "Checking API Health..."
HEALTH_RESPONSE=$(curl -s "$BASE_URL/health")
echo "Response: $HEALTH_RESPONSE"
if [[ "$HEALTH_RESPONSE" != *"OK"* ]]; then
    error "Health check failed! Is the server running?"
    exit 1
fi
echo ""

log "1. List Products (Initial)"
curl -s "$BASE_URL/products" | python3 -m json.tool
echo ""

# --- Scenario 1: Simple Create and Delete ---
log "2. Create Simple Product (POST /products)"
curl -s -X POST "$BASE_URL/products" \
     -H "$CONTENT_TYPE" \
     -d '{"name": "SimpleProduct", "price": 100.00, "stock": 10}'
echo " (Created)"
echo ""

log "3. Fetching ID of SimpleProduct..."
# Fetch the list and find the ID of the product we just created
SIMPLE_ID=$(curl -s "$BASE_URL/products" | python3 -c "import sys, json; print([p['id'] for p in json.load(sys.stdin) if p['name'] == 'SimpleProduct'][-1])")
echo "Created Simple Product ID: $SIMPLE_ID"
echo ""

# --- Scenario 2: Idempotent Create ---
log "4. Create Product with Idempotency Key (POST /products/idemKey)"
IDEM_KEY="create-key-$(date +%s)"
create_response=$(curl -s -X POST "$BASE_URL/products/idemKey?idempotencyKey=$IDEM_KEY" \
     -H "$CONTENT_TYPE" \
     -d '{"name": "IdemProduct", "price": 200.00, "stock": 20}')
echo "Response: $create_response"
IDEM_ID=$(echo $create_response | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])")
echo "Created Idem Product ID: $IDEM_ID"
echo ""

log "5. Re-submit Same Creation Request (Idempotency Check)"
# Using the same key, should handle gracefully (logic depends on backend, assumed safe/same return)
curl -s -X POST "$BASE_URL/products/idemKey?idempotencyKey=$IDEM_KEY" \
     -H "$CONTENT_TYPE" \
     -d '{"name": "IdemProduct", "price": 200.00, "stock": 20}' | python3 -m json.tool
echo ""

# --- Scenario 3: Update ---
log "6. Update Idem Product (PUT /products/$IDEM_ID)"
curl -s -X PUT "$BASE_URL/products/$IDEM_ID" \
     -H "$CONTENT_TYPE" \
     -d '{"name": "IdemProductUpdated", "price": 250.00, "stock": 20}'
echo " (Updated)"
echo ""

log "7. Verify Update (GET /products/$IDEM_ID)"
curl -s "$BASE_URL/products/$IDEM_ID" | python3 -m json.tool
echo ""

# --- Scenario 4: Stock Decrease (Idempotent) ---
log "8. Decrease Stock (POST /products/$IDEM_ID/decrease-stock)"
DECREASE_KEY="stock-key-$(date +%s)"
curl -v -X POST "$BASE_URL/products/$IDEM_ID/decrease-stock?idempotencyKey=$DECREASE_KEY" \
     -H "$CONTENT_TYPE" \
     -d '{"quantity": 5}'
echo ""

log "9. Re-submit Stock Decrease (Idempotency Check)"
# Using same key, stock should NOT decrease further
curl -v -X POST "$BASE_URL/products/$IDEM_ID/decrease-stock?idempotencyKey=$DECREASE_KEY" \
     -H "$CONTENT_TYPE" \
     -d '{"quantity": 5}'
echo ""

log "10. Verify Final Stock (Should be 15 if started at 20 and decreased 5 once)"
curl -s "$BASE_URL/products/$IDEM_ID" | python3 -m json.tool
echo ""

# --- Cleanup ---
log "11. Delete Simple Product (DELETE /products/$SIMPLE_ID)"
curl -s -X DELETE "$BASE_URL/products/$SIMPLE_ID"
echo " (Deleted)"
echo ""

log "12. Delete Idem Product (DELETE /products/$IDEM_ID)"
curl -s -X DELETE "$BASE_URL/products/$IDEM_ID"
echo " (Deleted)"
echo ""

log "13. List Products (Final State)"
curl -s "$BASE_URL/products" | python3 -m json.tool
echo ""

log "Test Sequence Completed."
