#!/bin/bash

# Docker-based test script for MCP HL7 Integration
set -e

echo "=========================================="
echo "MCP HL7 Integration - Docker Test"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${YELLOW}ℹ${NC} $1"
}

# Clean up function
cleanup() {
    print_info "Cleaning up..."
    docker-compose -f docker-compose-full.yml down
    rm -rf docker-build
}

# Set up trap to clean up on exit
trap cleanup EXIT

# Step 1: Create build directory
print_info "Creating build directory..."
mkdir -p docker-build

# Step 2: Build the module
print_info "Building OpenMRS Legacy UI Module..."
docker build -f Dockerfile.build --target builder -t legacyui-builder .
docker run --rm -v "$(pwd)/docker-build:/output" legacyui-builder \
    sh -c "cp /build/omod/target/*.omod /output/legacyui.omod && echo 'Module built successfully'"

if [ -f docker-build/legacyui.omod ]; then
    print_status "Module built successfully"
else
    print_error "Module build failed"
    exit 1
fi

# Step 3: Start MCP server
print_info "Starting MCP HL7 Analysis Server..."
docker-compose -f docker-compose-full.yml up -d mcp-hl7-server

# Wait for MCP server to be ready
print_info "Waiting for MCP server to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:3000/health > /dev/null 2>&1; then
        print_status "MCP server is ready"
        break
    fi
    echo -n "."
    sleep 1
done

# Step 4: Test MCP server
print_info "Testing MCP server analysis endpoint..."

# Check server mode
HEALTH_RESPONSE=$(curl -s http://localhost:3000/health)
if echo "$HEALTH_RESPONSE" | grep -q "mode"; then
    MODE=$(echo $HEALTH_RESPONSE | grep -o '"mode":"[^"]*"' | cut -d'"' -f4)
    print_status "MCP server running in $MODE mode"
else
    print_info "Mode information not available"
fi

# Test with good message
RESPONSE=$(curl -s -X POST http://localhost:3000/analyze \
  -H "Content-Type: application/json" \
  -d '{"message":"MSH|^~\\&|EPIC|EPICADT|SMS|SMSADT|20230101120000||ADT^A01|MSG00001|P|2.3\rPID|||123456||DOE^JOHN^A||19800101|M||W|123 MAIN ST^^CITY^ST^12345"}')

if echo "$RESPONSE" | grep -q "summary"; then
    print_status "MCP server analysis working"
    echo "  Response: $(echo $RESPONSE | jq -r '.summary' 2>/dev/null || echo $RESPONSE)"
else
    print_error "MCP server analysis failed"
    echo "  Response: $RESPONSE"
fi

# Test with bad message (missing PID)
print_info "Testing with malformed HL7 message..."
RESPONSE=$(curl -s -X POST http://localhost:3000/analyze \
  -H "Content-Type: application/json" \
  -d '{"message":"MSH|^~\\&|EPIC|EPICADT|SMS|SMSADT|20230101120000||ADT^A01|MSG00001|P|2.3"}')

if echo "$RESPONSE" | grep -q "Missing Patient Identification"; then
    print_status "Error detection working correctly"
else
    print_error "Error detection not working as expected"
fi

# Step 5: Optional - Start full stack
read -p "Do you want to start the full OpenMRS stack? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_info "Starting full OpenMRS stack..."
    docker-compose -f docker-compose-full.yml up -d

    print_info "Waiting for OpenMRS to be ready (this may take a few minutes)..."
    for i in {1..180}; do
        if curl -s http://localhost:8080/openmrs > /dev/null 2>&1; then
            print_status "OpenMRS is ready"
            print_info "Access OpenMRS at: http://localhost:8080/openmrs"
            print_info "Default credentials: admin/Admin123"
            break
        fi
        echo -n "."
        sleep 2
    done

    print_info "To view logs: docker-compose -f docker-compose-full.yml logs -f"
    print_info "To stop: docker-compose -f docker-compose-full.yml down"

    # Keep script running
    read -p "Press any key to stop the services..."
fi

print_status "Test complete!"