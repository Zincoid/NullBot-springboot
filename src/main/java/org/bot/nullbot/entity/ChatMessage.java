package org.bot.nullbot.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class ChatMessage
{
    private final Integer messageId;
    private final String role;  // "user" 或 "assistant" 或 "system"
    private final String content;
    private final Long userId;
    private final String userName;

    public Map<String, String> toMapForAI() {  // 转换为Map用于JSON序列化
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        if ("user".equals(role))
            map.put("content", "[" + userName + " (" + userId + ")]: " + content);
        else
            map.put("content", content);
        return map;
    }
}
