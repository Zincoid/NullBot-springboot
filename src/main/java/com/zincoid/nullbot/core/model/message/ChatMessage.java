package com.zincoid.nullbot.core.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ChatMessage {

    private final Integer messageId;
    private final Long userId;
    private final String userName;
    private final String role;  // "user" 或 "assistant" 或 "system"
    private final String content;

    public Map<String, String> toMapForAI() {  // 转换为Map用于JSON序列化
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        if ("user".equals(role))
            map.put("content", "[" + messageId + "][" + userName + " (" + userId + ")]: " + content);
        else
            map.put("content", content);
        return map;
    }
}
