package com.zincoid.nullbot.core.module.ai.chat.message;

import com.zincoid.nullbot.core.enums.Role;
import com.zincoid.nullbot.core.utils.ImgUtil;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
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
        map.put("role", super.role.getValue());
        if (super.role != Role.USER) {
            map.put("content", super.content);
            return map;
        }
        String _content = "[%s][%s(%s)]: %s".formatted(
                messageId, userName, userId, super.content);
        if (!vision) {
            map.put("content", _content);
            return map;
        }
        List<Map<String, Object>> parts = new ArrayList<>();
        images.forEach(image -> parts.add(Map.of(
                "type", "image_url",
                "image_url", Map.of("url", image)
        )));
        parts.addFirst(Map.of(
                "type", "text",
                "text", _content
        ));
        map.put("content", parts);
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
                .map(source -> {
                    if (source.startsWith(DATA_URI_PREFIX))
                        return source;
                    try {
                        return ImgUtil.toDataUri(source);
                    } catch (RuntimeException e) {
                        log.warn("▽ [QQMessage] 图源失效已丢弃: {} - {}",
                                source, e.getMessage());
                        this.content += " [图片失效]";
                        return null;
                    }
                })
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
