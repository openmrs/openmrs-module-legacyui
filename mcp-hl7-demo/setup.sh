#!/bin/bash

echo "Setting up MCP HL7 Demo for OpenMRS Legacy UI"
echo "=============================================="

# Install dependencies
echo "Installing Node.js dependencies..."
npm install

# Create test messages
echo "Creating test HL7 messages..."

# Good message
cat > test-good.hl7 << 'EOF'
MSH|^~\&|EPIC|EPICADT|SMS|SMSADT|20230101120000||ADT^A01|MSG00001|P|2.3
PID|||123456||DOE^JOHN^A||19800101|M||W|123 MAIN ST^^CITY^ST^12345
EOF

# Bad message 1 - missing PID
cat > test-bad-missing-pid.hl7 << 'EOF'
MSH|^~\&|EPIC|EPICADT|SMS|SMSADT|20230101120000||ADT^A01|MSG00001|P|2.3
EOF

# Bad message 2 - delimiter issues
cat > test-bad-delimiters.hl7 << 'EOF'
MSH|^~\&|EPIC||||||ADT^A01|MSG00001|P|2.3
PID|||||||DOE^JOHN
EOF

echo "Test messages created."

# Start the bridge server
echo ""
echo "To start the MCP bridge server, run:"
echo "  node bridge.js"
echo ""
echo "To test the MCP server directly, run:"
echo "  node index.js"
echo ""
echo "To enable MCP in OpenMRS, add to runtime properties:"
echo "  mcp.hl7.enabled=true"
echo ""
echo "Test endpoints:"
echo "  POST http://localhost:3000/analyze"
echo "  Body: {\"message\": \"<HL7 message content>\"}"