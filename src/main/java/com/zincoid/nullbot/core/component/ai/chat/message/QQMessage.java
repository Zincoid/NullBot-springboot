package com.zincoid.nullbot.core.component.ai.chat.message;

import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public class QQMessage extends AbstractMessage {

    private boolean isPrivate;
    private Integer messageId;
    private Long groupId;
    private Long userId;
    private String userName;

    public QQMessage(Role role, String content) {
        super(role, content);
    }

    @Override
    public Role getRole() {
        return super.getRole();
    }

    @Override
    public String getContent() {
        return super.getContent();
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

    public static QQMessage from(Message message) {
        return new QQMessage(message.getRole(), message.getContent());
    }

    public static QQMessage user(String content) {
        return new QQMessage(Role.USER, content);
    }

    public static QQMessage assistant(String content) {
        return new QQMessage(Role.ASSISTANT, content);
    }

    public static QQMessage system(String content) {
        return new QQMessage(Role.SYSTEM, content);
    }

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
