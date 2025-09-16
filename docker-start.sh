#!/bin/bash

echo "🚀 Starting OpenMRS Legacy UI Module..."

# Build the module
echo "📦 Building module..."
mvn clean install -DskipTests

# Start services
echo "🐳 Starting Docker services..."
docker-compose -f docker-compose-openmrs.yml up -d

echo "⏳ Waiting for services to start..."
sleep 30

# Check if services are running
if docker ps | grep -q "openmrs-module-legacyui_openmrs_1"; then
    echo "✅ OpenMRS is running at http://localhost:8080/openmrs"
    echo "👤 Login: admin / Admin123"
else
    echo "❌ OpenMRS failed to start. Check logs:"
    echo "   docker logs openmrs-module-legacyui_openmrs_1"
fi

if docker ps | grep -q "openmrs-module-legacyui_mysql_1"; then
    echo "✅ MySQL is running on port 3306"
else
    echo "❌ MySQL failed to start. Check logs:"
    echo "   docker logs openmrs-module-legacyui_mysql_1"
fi