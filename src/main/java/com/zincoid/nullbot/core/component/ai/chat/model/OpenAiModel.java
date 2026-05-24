package com.zincoid.nullbot.core.component.ai.chat.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolCall;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import com.zincoid.nullbot.core.properties.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiModel implements Model {

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public ModelResponse invoke(List<Message> messages, boolean thinking, int maxTokens) {
        return invoke(messages, null, thinking, maxTokens);
    }

    @Override
    public ModelResponse invoke(List<Message> messages, List<ToolDef> tools, boolean thinking, int maxTokens) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();

            List<Map<String, Object>> _messages = messages.stream()
                    .map(Message::toMap).toList();
            requestBody.set("messages", objectMapper.valueToTree(_messages));

            requestBody.put("model", openAiProperties.getModel());
            ObjectNode thinkingNode = objectMapper.createObjectNode();
            if (thinking) {
                thinkingNode.put("type", "enabled");
                requestBody.put("reasoning_effort", "high");
            } else {
                thinkingNode.put("type", "disabled");
            }
            requestBody.set("thinking", thinkingNode);
            requestBody.put("stream", false);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", 0.8);
            requestBody.put("frequency_penalty", 0.3);
            requestBody.put("presence_penalty", 0.2);

            if (tools != null && !tools.isEmpty()) {
                ArrayNode toolsNode = objectMapper.createArrayNode();
                for (ToolDef tool : tools) {
                    ObjectNode toolNode = objectMapper.createObjectNode();
                    toolNode.put("type", "function");
                    ObjectNode fnNode = objectMapper.createObjectNode();
                    fnNode.put("name", tool.getName());
                    fnNode.put("description", tool.getDescription());
                    fnNode.set("parameters", tool.getParameters());
                    toolNode.set("function", fnNode);
                    toolsNode.add(toolNode);
                }
                requestBody.set("tools", toolsNode);
            }

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String baseUrl = openAiProperties.getApiUrl();
            String fullUrl = baseUrl.endsWith("/chat/completions")
                    ? baseUrl
                    : (baseUrl.endsWith("/")
                    ? baseUrl + "chat/completions"
                    : baseUrl + "/chat/completions");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode messageNode = rootNode.path("choices").get(0).path("message");

                JsonNode toolCallsNode = messageNode.path("tool_calls");
                if (!toolCallsNode.isMissingNode() && toolCallsNode.isArray()) {
                    List<ToolCall> toolCalls = new ArrayList<>();
                    for (JsonNode tcNode : toolCallsNode) {
                        String id = tcNode.path("id").asText();
                        JsonNode fnNode = tcNode.path("function");
                        String name = fnNode.path("name").asText();
                        String arguments = fnNode.path("arguments").asText();
                        toolCalls.add(new ToolCall(id, name, arguments));
                    }
                    return ModelResponse.of(toolCalls);
                }

                String content = messageNode.path("content").asText(null);
                return ModelResponse.of(content != null ? content : "");
            } else {
                throw new RuntimeException("OpenAI API请求失败: " + response.statusCode() + " - " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
