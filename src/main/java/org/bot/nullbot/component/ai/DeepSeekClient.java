package org.bot.nullbot.component.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.config.DeepSeekConfig;
import org.bot.nullbot.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Data
@Component
@RequiredArgsConstructor
public class DeepSeekClient
{
    private final DeepSeekConfig deepSeekConfig;
    private final ChatStorage chatStorage;

    private final ObjectMapper objectMapper = new ObjectMapper();  // 用于JSON序列化的ObjectMapper
    private final HttpClient httpClient = HttpClient.newBuilder()  // HTTP客户端
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private Mode mode = Mode.Group;  // 会话模式

    public enum Mode {
        Group, Personal, Monitor;
        public Mode next() {
            int nextOrdinal = (this.ordinal() + 1) % values().length;
            return values()[nextOrdinal];
        }
    }

    /**
     * 切换聊天模式
     * @return 新的模式
     */
    public String changeMode() {
        mode = mode.next();
        return mode.toString();
    }

    /**
     * 与DeepSeek进行对话（连续对话）
     * @param groupId 群ID，用于区分不同的对话历史
     * @param userId 用户ID，用于区分不同的对话历史
     * @param userMessage 用户当前消息
     * @return AI回复内容
     */
    public String chat(Integer messageId, Long groupId, Long userId, String userName, String userMessage) throws Exception {
        List<ChatMessage> chatMessages = switch (mode) {
            case Group -> chatStorage.getGroupHistory(groupId);
            case Personal -> chatStorage.getUserHistory(userId);
            case Monitor -> chatStorage.getMonitorHistory(groupId);
        };
        chatMessages.add(new ChatMessage(messageId, "user", userMessage, userId, userName));  // 将用户当前消息添加到历史
        try {
            List<Map<String, String>> _messages = buildMessages(chatMessages);  // 添加系统信息构建完整的请求消息列表
            String response = sendRequest(_messages);  // 发送请求到API
            chatMessages.add(new ChatMessage(null ,"assistant", response, null, null));  // 将AI回复添加到历史
            if(mode == Mode.Monitor)  // 限制历史记录长度
                chatStorage.trimHistory(chatMessages, deepSeekConfig.getMaxMonitorLength());
            else
                chatStorage.trimHistory(chatMessages, deepSeekConfig.getMaxHistoryLength());
            return response;
        } catch (Exception e) {
            chatMessages.removeLast();  // 如果请求失败, 移除刚才添加的用户消息
            throw e;
        }
    }

    /**
     * 添加系统信息构建发送给API的消息列表
     * @param chatMessages 信息列表
     * @return 发送给API的消息列表
     */
    private List<Map<String, String>> buildMessages(List<ChatMessage> chatMessages) {
        List<Map<String, String>> _messages = new ArrayList<>();
        String systemMessage = switch (mode) {
            case Group -> deepSeekConfig.getSystemMessage().getGroup();
            case Personal -> deepSeekConfig.getSystemMessage().getPersonal();
            case Monitor -> deepSeekConfig.getSystemMessage().getMonitor();
        };
        _messages.add(new ChatMessage(null, "system", systemMessage, null, null).toMapForAI());  // 系统消息
        for (ChatMessage msg : chatMessages) _messages.add(msg.toMapForAI());  // 历史消息
        return _messages;
    }

    /**
     * 发送HTTP请求到DeepSeek API
     * @param _messages 请求消息列表（包括历史）
     * @return AI回复内容
     */
    private String sendRequest(List<Map<String, String>> _messages) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();  // 使用ObjectMapper构建JSON, 避免手动转义
        requestBody.put("model", "deepseek-chat");
        requestBody.put("max_tokens", deepSeekConfig.getMaxTokens());
        requestBody.set("messages", objectMapper.valueToTree(_messages));

        // 可选：其他参数
        // requestBody.put("temperature", 0.9);
        // requestBody.put("stream", false);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()  // 创建请求
                .uri(URI.create(deepSeekConfig.getApiUrl()))
                .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());  // 发送请求

        if (response.statusCode() == 200) {  // 处理响应
            JsonNode rootNode = objectMapper.readTree(response.body());
            return rootNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } else
            throw new RuntimeException("API请求失败: " + response.statusCode() + " - " + response.body());
    }

    /**
     * 清空指定对话历史
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 清除目标
     */
    public String clearHistory(Long groupId, Long userId) {
        return switch (mode) {
            case Group -> {
                chatStorage.clearGroupHistory(groupId);
                yield "[Group模式] 群聊 " + groupId;
            }
            case Personal -> {
                chatStorage.clearUserHistory(userId);
                yield "[Personal模式] 用户 " + userId;
            }
            case Monitor -> {
                chatStorage.clearMonitorHistory(groupId);
                yield "[Monitor模式] 群聊 " + groupId;
            }
        };
    }

    /**
     * 获取历史对话
     *  @param groupId 群ID
     *  @param userId 用户ID
     *  @return 历史记录
     */
    public String getHistoryAsString(Long groupId, Long userId) {
        return switch (mode) {
            case Group -> chatStorage.getGroupHistoryAsString(groupId);
            case Personal -> chatStorage.getUserHistoryAsString(userId);
            case Monitor -> chatStorage.getMonitorHistoryAsString(groupId);
        };
    }

    // /**
    //  * 与DeepSeek进行对话（简单非连续对话）
    //  * @param userMessage 用户消息
    //  * @return AI回复内容
    //  */
    // public String chatSingle(String userMessage) throws Exception {
    //     // 构建JSON请求体
    //     String requestBody = objectMapper.writeValueAsString(Map.of(
    //             "model", "deepseek-chat",
    //             "messages", List.of(Map.of(
    //                     "role", "user",
    //                     "content", userMessage
    //             )),
    //             "max_tokens", 200
    //     ));
    //
    //     // 创建HTTP请求
    //     HttpRequest request = HttpRequest.newBuilder()
    //             .uri(URI.create(deepSeekConfig.getApiUrl()))
    //             .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
    //             .header("Content-Type", "application/json")
    //             .header("Accept", "application/json")
    //             .POST(HttpRequest.BodyPublishers.ofString(requestBody))
    //             .timeout(Duration.ofSeconds(60))  // 添加请求超时
    //             .build();
    //
    //     // 发送请求并处理响应
    //     HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    //     if (response.statusCode() == 200) {
    //         JsonNode rootNode = objectMapper.readTree(response.body());
    //         return rootNode
    //                 .path("choices")
    //                 .get(0)
    //                 .path("message")
    //                 .path("content")
    //                 .asText();
    //     } else
    //         throw new RuntimeException("API请求失败: " + response.statusCode() + " - " + response.body());
    // }
}
