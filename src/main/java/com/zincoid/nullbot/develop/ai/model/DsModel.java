package com.zincoid.nullbot.develop.ai.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zincoid.nullbot.develop.ai.message.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class DsModel implements Model {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public String invoke(List<Message> messages, boolean thinking, int maxTokens) {
        try {
            List<Map<String, String>> _messages = messages.stream().map(Message::toMap).toList();

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-v4-flash");
            if (thinking) {
                ObjectNode thinkingNode = objectMapper.createObjectNode();
                thinkingNode.put("type", "enabled");
                requestBody.set("thinking", thinkingNode);
                requestBody.put("reasoning_effort", "high");
            } else {
                requestBody.set("thinking", NullNode.getInstance());
            }
            requestBody.put("stream", false);
            requestBody.put("max_tokens", maxTokens);
            requestBody.set("messages", objectMapper.valueToTree(_messages));
            requestBody.put("frequency_penalty", 0.3);
            requestBody.put("presence_penalty", 0.2);
            requestBody.put("temperature", 0.8);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.deepseek.com/chat/completions"))
                    .header("Authorization", "Bearer sk-9a6e7622692449ee90df32a29a57a9ca")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                return rootNode
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
            } else
                throw new RuntimeException("API请求失败: " + response.statusCode() + " - " + response.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
