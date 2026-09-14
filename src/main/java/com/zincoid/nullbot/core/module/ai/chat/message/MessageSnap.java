package com.zincoid.nullbot.core.module.ai.chat.message;

import com.zincoid.nullbot.core.enums.Role;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolCall;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MessageSnap {

    private String type;
    private Role role;
    private String content;

    private Boolean isPrivate;
    private Integer messageId;
    private Long groupId;
    private Long userId;
    private String userName;
    private List<String> images;

    private String reasoningContent;
    private List<ToolCall> toolCalls;
    private String toolCallId;

    public static MessageSnap of(Message message) {
        MessageSnap dto = new MessageSnap();
        if (message instanceof QQMessage q) {
            dto.type = "QQ";
            dto.role = q.getRole();
            dto.content = q.getContent();
            dto.isPrivate = q.isPrivate();
            dto.messageId = q.getMessageId();
            dto.groupId = q.getGroupId();
            dto.userId = q.getUserId();
            dto.userName = q.getUserName();
            dto.images = q.getImages();
            return dto;
        }
        if (message instanceof StdMessage s) {
            dto.type = "STD";
            dto.role = s.getRole();
            dto.content = s.getContent();
            dto.reasoningContent = s.getReasoningContent();
            dto.toolCalls = s.getToolCalls();
            dto.toolCallId = s.getToolCallId();
            return dto;
        }
        throw new IllegalArgumentException("未知消息类型 (CLASS): "
                + message.getClass().getName());
    }

    public Message toMessage() {
        if ("QQ".equals(type)) {
            QQMessage message = role == Role.ASSISTANT
                    ? QQMessage.assistant(content) : QQMessage.user(content);
            if (messageId != null) message.id(messageId);
            if (Boolean.TRUE.equals(isPrivate)) message.with(userId, userName);
            else message.with(groupId, userId, userName);
            if (images != null && !images.isEmpty()) message.img(images);
            return message;
        }
        if ("STD".equals(type)) {
            StdMessage message = switch (role) {
                case TOOL -> StdMessage.tool(toolCallId, content);
                case SYSTEM -> StdMessage.system(content);
                case ASSISTANT -> toolCalls != null && !toolCalls.isEmpty()
                        ? StdMessage.assistant(toolCalls)
                        : StdMessage.assistant(content);
                default -> StdMessage.user(content);
            };
            if (reasoningContent != null && !reasoningContent.isEmpty())
                message.withReasoning(reasoningContent);
            return message;
        }
        throw new IllegalArgumentException("未知消息类型 (TYPE): " + type);
    }
}
