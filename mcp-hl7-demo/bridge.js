import express from 'express';
import { spawn } from 'child_process';

const app = express();
app.use(express.json());

app.post('/analyze', async (req, res) => {
    const mcp = spawn('node', ['index.js'], { cwd: process.cwd() });

    mcp.stdin.write(JSON.stringify({
        jsonrpc: '2.0',
        method: 'analyze_hl7',
        params: { message: req.body.message },
        id: 1
    }) + '\n');

    let responseData = '';

    mcp.stdout.on('data', (data) => {
        responseData += data.toString();
        try {
            const lines = responseData.split('\n').filter(line => line.trim());
            for (const line of lines) {
                if (line.includes('"result"')) {
                    const response = JSON.parse(line);
                    res.json(response.result);
                    mcp.kill();
                    return;
                }
            }
        } catch (e) {
            // Continue accumulating data
        }
    });

    mcp.stderr.on('data', (data) => {
        console.error('MCP Error:', data.toString());
    });

    mcp.on('error', (error) => {
        console.error('Failed to start MCP process:', error);
        res.status(500).json({ error: 'Failed to start MCP process' });
    });

    setTimeout(() => {
        mcp.kill();
        if (!res.headersSent) {
            res.status(500).json({ error: 'MCP timeout' });
        }
    }, 5000);
});

app.listen(3000, () => {
    console.log('MCP Bridge server running on port 3000');
});