package org.openmrs.module.legacyui.api.mcp;

import org.springframework.stereotype.Component;
import org.openmrs.api.context.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component("mcpDemo")
public class MCPDemo {

    public Map<String, Object> analyzeHL7(String message) {
        // Check if MCP is enabled via runtime properties
        String mcpEnabled = Context.getRuntimeProperties().getProperty("mcp.hl7.enabled", "false");
        if (!"true".equals(mcpEnabled)) {
            return Collections.singletonMap("error", "MCP not enabled. Set mcp.hl7.enabled=true in runtime properties");
        }

        try {
            // Get MCP mode (mock or real)
            String mcpMode = Context.getRuntimeProperties().getProperty("mcp.hl7.mode",
                System.getenv("MCP_MODE"));
            if (mcpMode == null) {
                mcpMode = "mock"; // Default to mock mode
            }

            // Get MCP server URL based on mode
            String mcpUrl;
            if ("real".equalsIgnoreCase(mcpMode)) {
                // Real MCP server endpoint (JSON-RPC)
                mcpUrl = Context.getRuntimeProperties().getProperty("mcp.hl7.server.url",
                    "http://localhost:3001/mcp");
                if ("true".equals(System.getenv("DOCKER_ENV"))) {
                    mcpUrl = "http://mcp-hl7-real:3001/mcp";
                }
            } else {
                // Mock server endpoint (REST API)
                mcpUrl = Context.getRuntimeProperties().getProperty("mcp.hl7.server.url",
                    "http://localhost:3000/analyze");
                if ("true".equals(System.getenv("DOCKER_ENV"))) {
                    mcpUrl = "http://mcp-hl7-server:3000/analyze";
                }
            }

            URL url = new URL(mcpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Escape the message for JSON
            String escapedMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

            // Create payload based on mode
            String jsonPayload;
            if ("real".equalsIgnoreCase(mcpMode)) {
                // JSON-RPC format for real MCP server
                jsonPayload = "{"
                    + "\"jsonrpc\":\"2.0\","
                    + "\"method\":\"analyze_hl7\","
                    + "\"params\":{\"message\":\"" + escapedMessage + "\"},"
                    + "\"id\":1"
                    + "}";
            } else {
                // REST format for mock server
                jsonPayload = "{\"message\":\"" + escapedMessage + "\"}";
            }

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(jsonPayload);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse JSON response based on mode
            if ("real".equalsIgnoreCase(mcpMode)) {
                return parseJsonRpcResponse(response.toString());
            } else {
                return parseJsonResponse(response.toString());
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "MCP analysis failed: " + e.getMessage());
            return errorResponse;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResponse(String json) {
        // Simple JSON parsing for the expected response structure
        Map<String, Object> result = new HashMap<>();

        try {
            // This is a simplified parser - in production, use a proper JSON library
            // For now, we'll just extract the key fields using string manipulation

            // Extract errors array
            if (json.contains("\"errors\":")) {
                int start = json.indexOf("\"errors\":") + 9;
                int end = json.indexOf("]", start) + 1;
                if (end > start) {
                    String errorsJson = json.substring(start, end);
                    result.put("errors", parseJsonArray(errorsJson));
                }
            }

            // Extract suggestions array
            if (json.contains("\"suggestions\":")) {
                int start = json.indexOf("\"suggestions\":") + 14;
                int end = json.indexOf("]", start) + 1;
                if (end > start) {
                    String suggestionsJson = json.substring(start, end);
                    result.put("suggestions", parseJsonArray(suggestionsJson));
                }
            }

            // Extract summary
            if (json.contains("\"summary\":")) {
                int start = json.indexOf("\"summary\":\"") + 11;
                int end = json.indexOf("\"", start);
                if (end > start) {
                    result.put("summary", json.substring(start, end));
                }
            }

            // Extract parsed
            if (json.contains("\"parsed\":")) {
                int start = json.indexOf("\"parsed\":\"") + 10;
                int end = json.indexOf("\"", start);
                if (end > start) {
                    result.put("parsed", json.substring(start, end));
                }
            }

        } catch (Exception e) {
            result.put("error", "Failed to parse response: " + e.getMessage());
        }

        return result;
    }

    private java.util.List<String> parseJsonArray(String jsonArray) {
        java.util.List<String> list = new java.util.ArrayList<>();

        // Remove brackets and split by comma
        String content = jsonArray.trim();
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1);

        if (content.trim().isEmpty()) return list;

        // Split by comma but respect quotes
        String[] items = content.split("\",\"");
        for (String item : items) {
            item = item.trim();
            if (item.startsWith("\"")) item = item.substring(1);
            if (item.endsWith("\"")) item = item.substring(0, item.length() - 1);
            list.add(item);
        }

        return list;
    }

    private Map<String, Object> parseJsonRpcResponse(String json) {
        // Parse JSON-RPC response format
        Map<String, Object> result = new HashMap<>();

        try {
            // Extract the result field from JSON-RPC response
            if (json.contains("\"result\":")) {
                int start = json.indexOf("\"result\":") + 9;

                // Find the matching closing brace for the result object
                int braceCount = 0;
                int resultStart = -1;
                int resultEnd = -1;

                for (int i = start; i < json.length(); i++) {
                    char c = json.charAt(i);
                    if (c == '{') {
                        if (resultStart == -1) {
                            resultStart = i;
                        }
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0 && resultStart != -1) {
                            resultEnd = i + 1;
                            break;
                        }
                    }
                }

                if (resultStart != -1 && resultEnd != -1) {
                    String resultJson = json.substring(resultStart, resultEnd);
                    return parseJsonResponse(resultJson);
                }
            }

            // Check for JSON-RPC error
            if (json.contains("\"error\":")) {
                result.put("error", "MCP JSON-RPC error occurred");
            }

        } catch (Exception e) {
            result.put("error", "Failed to parse JSON-RPC response: " + e.getMessage());
        }

        return result;
    }
}