package com.zincoid.nullbot.core.module.ai.chat.message;

import com.zincoid.nullbot.core.enums.Role;
import com.zincoid.nullbot.core.utils.ImgUtil;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@ToString(callSuper = true)
public class QQMessage extends AbstractMessage {

    private static final String DATA_URI_PREFIX = "data:";

    private boolean isPrivate;
    private Integer messageId;
    private Long groupId;
    private Long userId;
    private String userName;
    private List<String> images = List.of();

    private QQMessage(Role role, String content) {
        super(role, content);
    }

    public Map<String, Object> toMap() {
        return toMap(false);
    }

    @Override
    public Map<String, Object> toMap(boolean vision) {
        Map<String, Object> map = new HashMap<>();
        String r = super.role.getValue();
        map.put("role", r);
        if (!"user".equals(r)) {
            map.put("content", content);
            return map;
        }
        StringBuilder _content = new StringBuilder("[%s][%s(%s)]: %s"
                .formatted(messageId, userName, userId, super.content));
        List<Map<String, Object>> parts = new ArrayList<>();
        if (vision) {
            for (String image : images) {
                if (!image.startsWith(DATA_URI_PREFIX)) {
                    _content.append("[图片失效]");
                    continue;
                }
                parts.add(Map.of(
                        "type", "image_url",
                        "image_url", Map.of("url", image)
                ));
            }
        }
        if (parts.isEmpty()) {
            map.put("content", _content.toString());
        } else {
            parts.addFirst(Map.of(
                    "type", "text",
                    "text", _content.toString()
            ));
            map.put("content", parts);
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

    public QQMessage img(Collection<String> sources) {
        this.images = sources == null ? List.of() : sources.stream()
                .map(source -> source.startsWith(DATA_URI_PREFIX) ? source : ImgUtil.toDataUri(source))
                .filter(Objects::nonNull)
                .map(ImgUtil::compressDataUri)
                .toList();
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
