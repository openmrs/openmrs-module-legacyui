import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';

const server = new Server({
  name: 'hl7-analyzer',
  version: '1.0.0',
});

server.setRequestHandler('analyze_hl7', async (request) => {
  const { message } = request.params;

  const segments = message.split('\r');
  const errors = [];
  const suggestions = [];

  if (!message.startsWith('MSH')) {
    errors.push('Missing MSH header');
    suggestions.push('Add MSH segment at the beginning');
  }

  if (!message.includes('PID')) {
    errors.push('Missing patient identification (PID)');
    suggestions.push('Add PID segment after MSH');
  }

  if (message.includes('||||||')) {
    errors.push('Too many empty fields detected');
    suggestions.push('Review field requirements and remove unnecessary delimiters');
  }

  return {
    parsed: segments.length + ' segments found',
    errors,
    suggestions,
    summary: `HL7 message with ${errors.length} issues detected`
  };
});

const transport = new StdioServerTransport();
await server.connect(transport);