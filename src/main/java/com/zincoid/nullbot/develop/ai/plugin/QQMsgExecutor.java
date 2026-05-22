package com.zincoid.nullbot.develop.ai.plugin;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.common.MsgId;
import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.component.ai.TtsClient;
import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.component.tool.BotOperator;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
import com.zincoid.nullbot.core.model.bot.event.EmbeddedCommandEvent;
import com.zincoid.nullbot.core.util.Base64Util;
import com.zincoid.nullbot.develop.ai.message.QQMessage;
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

    private final BotOperator botOperator;
    private final TtsClient ttsClient;
    private final ResourceLoader resourceLoader;
    private final ApplicationEventPublisher eventPublisher;

    private static final Pattern USER_INFO_PATTERN;
    private static final Pattern SEGMENT_PATTERN;

    static {
        USER_INFO_PATTERN = Pattern.compile("\\[\\d+]\\[.+?\\(\\d+\\)]:");
        SEGMENT_PATTERN = Pattern.compile("(\\{.*?}|[^{]+)");
    }

    public List<QQMessage> basic(QQMessage message, Long targetId, boolean isPrivate,
                          boolean voice) {

        Bot bot = botOperator.getBot(3, 5000);
        String content = message.getContent();
        if (content.contains("{Discard}"))
            return List.of(QQMessage.assistant("回复被拒绝"));
        Integer messageId;
        if (messageFilter(content)) {
            content = "回复被过滤";
            messageId = sendMsg(bot, targetId, buildFilteredMsg(), isPrivate, voice);
        } else {
            content = content.replaceAll("(\r?\n)+", "\n").trim();
            messageId = sendMsg(bot, targetId, content, isPrivate, voice);
        }
        return List.of(QQMessage.assistant(content).info(messageId));
    }

    public List<QQMessage> chain(QQMessage message, Long targetId, boolean isPrivate,
                          Event event, boolean voice, boolean embeddingAuth) {

        Bot bot = botOperator.getBot(3, 5000);
        String content = message.getContent();
        if (content.contains("{Discard}"))
            return List.of(QQMessage.assistant("回复被拒绝"));
        if (messageFilter(content)) {
            Integer messageId = sendMsg(bot, targetId, buildFilteredMsg(), isPrivate, voice);
            return List.of(QQMessage.assistant("回复被过滤").info(messageId));
        }
        content = content.replaceAll("(\r?\n)+", "\n").trim();
        Matcher matcher = SEGMENT_PATTERN.matcher(content);
        List<QQMessage> messages = new ArrayList<>();
        while (matcher.find()) {
            String segment = matcher.group(1).trim();
            if (segment.startsWith("{") && segment.endsWith("}")) {
                String command = segment.substring(1, segment.length() - 1).trim();
                if (command.isEmpty()) continue;
                eventPublisher.publishEvent(new EmbeddedCommandEvent(bot,
                        new CommandEvent<>(event, command, embeddingAuth, false)));
                messages.add(QQMessage.assistant(segment));
            } else {
                if (segment.isEmpty()) continue;
                Integer messageId = sendMsg(bot, targetId, segment, isPrivate, voice);
                messages.add(QQMessage.assistant(segment).info(messageId));
            }
        }
        return messages;
    }

    // =================== 工具方法 ===================

    private Integer sendMsg(Bot bot, Long targetId, String message,
                            boolean isPrivate, boolean voice) {
        ActionData<MsgId> msgIdActionData;
        if (isPrivate) {
            msgIdActionData = bot.sendPrivateMsg(
                    targetId,
                    voice ? MsgUtils.builder()
                            .voice("base64://" + ttsClient.synthesize(message)).build() : message,
                    false
            );
        } else {
            msgIdActionData = bot.sendGroupMsg(
                    targetId,
                    voice ? MsgUtils.builder()
                            .voice("base64://" + ttsClient.synthesize(message)).build() : message,
                    false
            );
        }
        return msgIdActionData.getData().getMessageId();
    }

    boolean messageFilter(String message) {
        return USER_INFO_PATTERN.matcher(message).find();
    }

    // private String buildRefusedMsg() {
    //     return MsgUtils.builder()
    //             .text("[AI] ⚠️对话被拒绝")
    //             .img("base64://" + Base64Util.from(resourceLoader
    //                     .getCached("static/image/Filtered.jpg")))
    //             .build();
    // }

    private String buildFilteredMsg() {
        return MsgUtils.builder()
                .text("[AI] ⚠️回复被过滤")
                .img("base64://" + Base64Util.from(resourceLoader
                        .getCached("static/image/Filtered.jpg")))
                .build();
    }
}
