package com.zincoid.nullbot.core.component.ai.chat.plugin;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.common.MsgId;
import com.zincoid.nullbot.core.component.ai.voice.TtsClient;
import com.zincoid.nullbot.core.component.resource.loader.ResourceLoader;
import com.zincoid.nullbot.core.model.bot.event.InnerCommandEvent;
import com.zincoid.nullbot.core.util.Base64Util;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class QQMsgExecutor {

    private final ResourceLoader resourceLoader;
    private final ApplicationEventPublisher eventPublisher;
    private final TtsClient ttsClient;

    private static final Pattern INFO_PATTERN;
    private static final Pattern SEGMENT_PATTERN;

    static {
        INFO_PATTERN = Pattern.compile("\\[\\d+]\\[.+?\\(\\d+\\)]:");
        SEGMENT_PATTERN = Pattern.compile("(\\{.*?}|[^{]+)");
    }

    // =================== 执行方法 ===================

    public QQMessage direct(QQMessage message, boolean voice) {
        Bot bot = BotCtxUtil.getBot();
        boolean isPrivate = message.isPrivate();
        Long targetId = isPrivate ? message.getUserId() : message.getGroupId();
        String content = message.getContent();
        if (content.contains("{Discard}"))
            return QQMessage.assistant("回复被拒绝");
        Integer messageId;
        if (filter(content)) {
            content = "回复被过滤";
            messageId = send(bot, targetId, filtered(), isPrivate, voice);
        } else {
            content = content.replaceAll("(\r?\n)+", "\n").trim();
            messageId = send(bot, targetId, content, isPrivate, voice);
        }
        return QQMessage.assistant(content).id(messageId);
    }

    public List<QQMessage> chain(QQMessage message, boolean voice) {
        Bot bot = BotCtxUtil.getBot();
        boolean isPrivate = message.isPrivate();
        Long targetId = message.isPrivate() ? message.getUserId() : message.getGroupId();
        String content = message.getContent();
        if (content.contains("{Discard}"))
            return List.of(QQMessage.assistant("回复被拒绝"));
        if (filter(content)) {
            Integer messageId = send(bot, targetId, filtered(), isPrivate, voice);
            return List.of(QQMessage.assistant("回复被过滤").id(messageId));
        }
        content = content.replaceAll("(\r?\n)+", "\n").trim();
        Matcher matcher = SEGMENT_PATTERN.matcher(content);
        List<QQMessage> messages = new ArrayList<>();
        while (matcher.find()) {
            String segment = matcher.group(1).trim();
            if (segment.startsWith("{") && segment.endsWith("}")) {
                String command = segment.substring(1, segment.length() - 1).trim();
                if (command.isEmpty()) continue;
                eventPublisher.publishEvent(InnerCommandEvent.of(command));
                messages.add(QQMessage.assistant(segment));
            } else {
                if (segment.isEmpty()) continue;
                Integer messageId = send(bot, targetId, segment, isPrivate, voice);
                messages.add(QQMessage.assistant(segment).id(messageId));
            }
        }
        return messages;
    }

    // =================== 工具方法 ===================

    private Integer send(Bot bot, Long targetId, String message, boolean isPrivate, boolean voice) {
        ActionData<MsgId> msgIdActionData = isPrivate
                ? bot.sendPrivateMsg(targetId, voice ? voiced(message) : message, false)
                : bot.sendGroupMsg(targetId, voice ? voiced(message) : message, false);
        return msgIdActionData.getData().getMessageId();
    }

    boolean filter(String message) {
        return INFO_PATTERN.matcher(message).find();
    }

    // =================== 消息方法 ===================

    private String filtered() {
        return MsgUtils.builder()
                .text("[AI] ⚠️回复被过滤")
                .img("base64://" + Base64Util.from(resourceLoader
                        .getCache("static/image/Filtered.jpg")))
                .build();
    }

    private String voiced(String message) {
        return MsgUtils.builder()
                .voice("base64://" + ttsClient.synthesize(message))
                .build();
    }
}
