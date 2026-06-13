package com.zincoid.nullbot.core.module.ai.chat.message;

import com.zincoid.nullbot.core.enums.Role;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString(callSuper = true)
public class QQMessage extends AbstractMessage {

    private boolean isPrivate;
    private Integer messageId;
    private Long groupId;
    private Long userId;
    private String userName;

    private QQMessage(Role role, String content) {
        super(role, content);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
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

    // ====================== 构建方法 ======================

    public static QQMessage send(QQMessage message, String content) {
        return message.isPrivate
                ? QQMessage.assistant(content).with(message.userId, message.userName)
                : QQMessage.assistant(content).with(message.groupId, message.userId, message.userName);
    }

    public static QQMessage user(String content) {
        return new QQMessage(Role.USER, content);
    }

    public static QQMessage assistant(String content) {
        return new QQMessage(Role.ASSISTANT, content);
    }

    // ====================== 设置方法 ======================

    public QQMessage id(Integer messageId) {
        this.messageId = messageId;
        return this;
    }

    public QQMessage with(Long groupId, Long userId, String userName) {
        this.isPrivate = false;
        this.groupId = groupId;
        this.userId = userId;
        this.userName = userName;
        return this;
    }

    public QQMessage with(Long userId, String userName) {
        this.isPrivate = true;
        this.userId = userId;
        this.userName = userName;
        return this;
    }
}
