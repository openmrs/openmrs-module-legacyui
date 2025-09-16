// Test client for the HL7 MCP Mock Server

const testMessages = {
    good: 'MSH|^~\\&|EPIC|EPICADT|SMS|SMSADT|20230101120000||ADT^A01|MSG00001|P|2.3\rPID|||123456||DOE^JOHN^A||19800101|M||W|123 MAIN ST^^CITY^ST^12345',
    missingPID: 'MSH|^~\\&|EPIC|EPICADT|SMS|SMSADT|20230101120000||ADT^A01|MSG00001|P|2.3',
    delimiters: 'MSH|^~\\&|EPIC||||||ADT^A01|MSG00001|P|2.3\rPID|||||||DOE^JOHN',
    empty: ''
};

async function testAnalysis(name, message) {
    console.log(`\nTesting: ${name}`);
    console.log('Message:', message.substring(0, 50) + '...');

    try {
        const response = await fetch('http://localhost:3000/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message })
        });

        const result = await response.json();
        console.log('Result:', JSON.stringify(result, null, 2));
    } catch (error) {
        console.error('Test failed:', error.message);
    }
}

async function runTests() {
    console.log('HL7 MCP Mock Server Test Client');
    console.log('================================');

    // Check health first
    try {
        const health = await fetch('http://localhost:3000/health');
        const status = await health.json();
        console.log('Server status:', status);
    } catch (error) {
        console.error('Server not running. Start it with: npm start');
        process.exit(1);
    }

    // Run tests
    await testAnalysis('Good Message', testMessages.good);
    await testAnalysis('Missing PID', testMessages.missingPID);
    await testAnalysis('Delimiter Issues', testMessages.delimiters);
    await testAnalysis('Empty Message', testMessages.empty);
}

runTests();