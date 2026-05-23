package com.zincoid.nullbot.core.component.ai.chat.message;

import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
public class BaseMessage extends AbstractMessage {

    private BaseMessage(Role role, String content) {
        super(role, content);
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("role", super.role.getValue());
        map.put("content", super.content);
        return map;
    }

    // ==================== 构建方法 ====================

    public static BaseMessage user(String content) {
        return new BaseMessage(Role.USER, content);
    }
    public static BaseMessage assistant(String content) {
        return new BaseMessage(Role.ASSISTANT, content);
    }
    public static BaseMessage system(String content) {
        return new BaseMessage(Role.SYSTEM, content);
    }
}
