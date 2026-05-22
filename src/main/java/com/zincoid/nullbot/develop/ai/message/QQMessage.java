package com.zincoid.nullbot.develop.ai.message;

import com.zincoid.nullbot.develop.ai.enums.Role;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class QQMessage extends AbstractMessage {

    private Integer messageId;
    private Long groupId;
    private Long userId;
    private String userName;

    public QQMessage(Role role, String content) {
        super(role, content);
    }

    @Override
    public Role getRole() {
        return super.role;
    }

    @Override
    public String getContent() {
        return super.content;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        String r = super.role.getValue();
        map.put("role", r);
        if ("user".equals(r)) {
            map.put("content", "[%s][%s(%s)]: %s".formatted(
                    messageId, userName, userId, super.content));
        } else {
            map.put("content", content);
        }
        return map;
    }

    // 构建方法

    public static QQMessage user(String content) {
        return new QQMessage(Role.USER, content);
    }

    public static QQMessage assistant(String content) {
        return new QQMessage(Role.ASSISTANT, content);
    }

    public static QQMessage system(String content) {
        return new QQMessage(Role.SYSTEM, content);
    }

    public QQMessage info(Integer messageId, Long groupId, Long userId, String userName) {
        this.messageId = messageId;
        this.groupId = groupId;
        this.userId = userId;
        this.userName = userName;
        return this;
    }

    public QQMessage info(Integer messageId) {
        this.messageId = messageId;
        return this;
    }
}
