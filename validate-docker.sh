#!/bin/bash

# Validate Docker configuration for MCP HL7 Integration
set -e

echo "=========================================="
echo "Docker Configuration Validation"
echo "=========================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_check() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check Docker and Docker Compose
if command -v docker &> /dev/null; then
    print_check "Docker is installed"
else
    print_error "Docker is not installed"
    exit 1
fi

if command -v docker-compose &> /dev/null; then
    print_check "Docker Compose is installed"
else
    print_error "Docker Compose is not installed"
    exit 1
fi

# Check required files exist
required_files=(
    "Dockerfile.build"
    "docker-compose-full.yml"
    "mcp-hl7-demo/Dockerfile"
    "mcp-hl7-demo/package.json"
    "mcp-hl7-demo/mock-server.js"
    "api/src/main/java/org/openmrs/module/legacyui/api/mcp/MCPDemo.java"
    "omod/src/main/java/org/openmrs/hl7/web/controller/Hl7InErrorListController.java"
    "omod/src/main/webapp/admin/hl7/hl7InErrorList.jsp"
)

echo -e "\nValidating required files..."
all_files_exist=true
for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        print_check "Found: $file"
    else
        print_error "Missing: $file"
        all_files_exist=false
    fi
done

if [ "$all_files_exist" = false ]; then
    print_error "Some required files are missing"
    exit 1
fi

# Validate Docker configurations
echo -e "\nValidating Docker configurations..."

# Check Dockerfile.build
if grep -q "FROM maven:" Dockerfile.build && grep -q "mvn clean package" Dockerfile.build; then
    print_check "Dockerfile.build has correct Maven build steps"
else
    print_error "Dockerfile.build is missing Maven build configuration"
fi

# Check MCP Dockerfile
if [ -f "mcp-hl7-demo/Dockerfile" ]; then
    if grep -q "FROM node:" mcp-hl7-demo/Dockerfile && grep -q "npm install" mcp-hl7-demo/Dockerfile; then
        print_check "MCP Dockerfile has correct Node.js configuration"
    else
        print_error "MCP Dockerfile is missing Node.js configuration"
    fi
fi

# Check docker-compose
if grep -q "mcp-hl7-server:" docker-compose-full.yml && grep -q "openmrs:" docker-compose-full.yml; then
    print_check "Docker Compose has required services"
else
    print_error "Docker Compose is missing required services"
fi

# Validate Java code for hardcoded paths
echo -e "\nChecking for hardcoded paths in Java code..."
LOCALHOST_REFS=$(grep -r "localhost" api/src/main/java/ | grep -v "getProperty" | grep -v "fallback" | grep -v "//" || true)
if [ -n "$LOCALHOST_REFS" ]; then
    print_error "Found hardcoded localhost references without fallbacks:"
    echo "$LOCALHOST_REFS"
else
    print_check "Localhost references are properly handled as fallbacks"
fi

# Check for Docker environment detection
if grep -q "DOCKER_ENV" api/src/main/java/org/openmrs/module/legacyui/api/mcp/MCPDemo.java; then
    print_check "Java code has Docker environment detection"
else
    print_error "Java code missing Docker environment detection"
fi

# Check for runtime property overrides
if grep -q "getProperty.*mcp.hl7.server.url" api/src/main/java/org/openmrs/module/legacyui/api/mcp/MCPDemo.java; then
    print_check "Java code supports runtime property overrides"
else
    print_error "Java code missing runtime property override support"
fi


# Validate MCP server code
echo -e "\nValidating MCP server..."
if [ -f "mcp-hl7-demo/mock-server.js" ]; then
    if grep -q "express()" mcp-hl7-demo/mock-server.js && grep -q "/analyze" mcp-hl7-demo/mock-server.js; then
        print_check "MCP mock server has correct endpoints"
    else
        print_error "MCP mock server is missing required endpoints"
    fi
fi

if [ -f "mcp-hl7-demo/package.json" ]; then
    if grep -q "express" mcp-hl7-demo/package.json; then
        print_check "MCP package.json has Express dependency"
    else
        print_error "MCP package.json is missing Express dependency"
    fi
fi

# Check for environment variable handling
echo -e "\nValidating environment configuration..."
if grep -q "process.env.PORT" mcp-hl7-demo/mock-server.js; then
    print_check "MCP server handles PORT environment variable"
else
    print_warning "MCP server should handle PORT environment variable"
fi

# Validate network configuration
if grep -q "openmrs-net" docker-compose-full.yml; then
    print_check "Docker Compose defines custom network"
else
    print_error "Docker Compose missing custom network configuration"
fi

# Check for health checks
if grep -q "healthcheck" docker-compose-full.yml; then
    print_check "Services have health check configuration"
else
    print_warning "Consider adding health checks to services"
fi

# Port conflict check
echo -e "\nChecking for port usage..."
if netstat -tuln 2>/dev/null | grep -q ":3000 "; then
    print_warning "Port 3000 appears to be in use"
else
    print_check "Port 3000 is available"
fi

if netstat -tuln 2>/dev/null | grep -q ":8080 "; then
    print_warning "Port 8080 appears to be in use"
else
    print_check "Port 8080 is available"
fi

echo -e "\nValidation complete!"
echo "To test the build:"
echo "  ./docker-test.sh"
echo ""
echo "To build manually:"
echo "  docker build -f Dockerfile.build --target builder -t legacyui-builder ."
echo "  docker build -t mcp-hl7-analyzer mcp-hl7-demo/"