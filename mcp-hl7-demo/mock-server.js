// Mock MCP Server for HL7 Analysis Demo
// This simulates MCP functionality without requiring the actual SDK

import express from 'express';

const app = express();
app.use(express.json());

// Mock HL7 analysis function
function analyzeHL7(message) {
    const errors = [];
    const suggestions = [];

    // Basic HL7 validation checks
    if (!message || message.length === 0) {
        errors.push('Empty message');
        suggestions.push('Provide a valid HL7 message');
        return { errors, suggestions, summary: 'Invalid: Empty message' };
    }

    // Check for MSH header
    if (!message.startsWith('MSH')) {
        errors.push('Missing MSH header segment');
        suggestions.push('Add MSH segment at the beginning of the message');
    }

    // Check for PID segment
    if (!message.includes('PID')) {
        errors.push('Missing Patient Identification (PID) segment');
        suggestions.push('Add PID segment after MSH segment');
    }

    // Check for excessive delimiters
    if (message.includes('||||||')) {
        errors.push('Excessive empty fields detected');
        suggestions.push('Review field requirements and remove unnecessary delimiters');
    }

    // Check for segment separators
    const segments = message.split(/\r\n|\r|\n/);
    if (segments.length < 2) {
        errors.push('Message appears to be missing segment separators');
        suggestions.push('Ensure segments are separated by carriage return (\\r) or newline');
    }

    // Check for valid segment structure
    segments.forEach((segment, index) => {
        if (segment.length > 0 && segment.length < 4) {
            errors.push(`Segment ${index + 1} appears malformed: "${segment}"`);
            suggestions.push(`Review segment ${index + 1} for proper formatting`);
        }
    });

    // Generate summary
    const summary = errors.length === 0
        ? `Valid HL7 message with ${segments.length} segments`
        : `HL7 message with ${errors.length} issue(s) detected in ${segments.length} segments`;

    return {
        parsed: `${segments.length} segments found`,
        errors,
        suggestions,
        summary
    };
}

// Main analysis endpoint
app.post('/analyze', (req, res) => {
    try {
        const { message } = req.body;

        if (!message) {
            return res.status(400).json({
                error: 'No message provided'
            });
        }

        const result = analyzeHL7(message);
        res.json(result);

    } catch (error) {
        res.status(500).json({
            error: 'Analysis failed: ' + error.message
        });
    }
});

// Health check endpoint
app.get('/health', (req, res) => {
    const mode = process.env.MCP_MODE || 'mock';
    res.json({
        status: 'ok',
        service: 'HL7 MCP Mock Server',
        mode: mode,
        version: '1.0.0'
    });
});

const PORT = process.env.PORT || 3000;
const MODE = process.env.MCP_MODE || 'mock';

app.listen(PORT, () => {
    console.log(`HL7 MCP Server running in ${MODE.toUpperCase()} mode on port ${PORT}`);
    console.log(`Health check: http://localhost:${PORT}/health`);
    console.log(`Analysis endpoint: POST http://localhost:${PORT}/analyze`);

    if (MODE === 'mock') {
        console.log('🎭 Running in MOCK mode - using simulated MCP responses');
        console.log('💡 To switch to real MCP, set MCP_MODE=real environment variable');
    }
});