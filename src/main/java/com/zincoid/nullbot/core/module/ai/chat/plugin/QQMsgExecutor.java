package com.zincoid.nullbot.core.module.ai.chat.plugin;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import com.zincoid.nullbot.core.module.ai.tts.TtsClient;
import com.zincoid.nullbot.core.module.resource.loader.ResourceLoader;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.utils.Base64Util;
import com.zincoid.nullbot.core.module.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.utils.MsgUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QQMsgExecutor {

    private final BotOperator botOperator;
    private final ResourceLoader resourceLoader;
    private final ApplicationEventPublisher eventPublisher;
    private final TtsClient ttsClient;

    private static final Pattern SEGMENT_PATTERN;
    private static final Pattern NEWLINE_PATTERN;
    private static final List<Pattern> FILTERED_PATTERNS;

    static {
        SEGMENT_PATTERN = Pattern.compile("(<cmd>.*?</cmd>|(?:(?!<cmd>).)+)");
        NEWLINE_PATTERN = Pattern.compile("(\r?\n)+");

        FILTERED_PATTERNS = new ArrayList<>();
        Set<String> allCmds = QQCmdAllows.getAll().stream().map(Pattern::quote).collect(Collectors.toSet());
        FILTERED_PATTERNS.add(Pattern.compile("(?<!<cmd>)\\b(" + String.join("|", allCmds) + ")\\b(?!</cmd>)"));
        FILTERED_PATTERNS.add(Pattern.compile("\\[\\d+]\\[.+?\\(\\d+\\)]:"));
    }

    // =================== 执行方法 ===================

    public QQMessage direct(QQMessage message, boolean voice) {
        boolean isPrivate = message.isPrivate();
        Long targetId = isPrivate ? message.getUserId() : message.getGroupId();
        String content = message.getContent();
        if (content.contains("<discard />"))
            return QQMessage.assistant("回复被拒绝");
        Integer messageId;
        if (filter(content)) {
            content = "回复被过滤";
            messageId = send(targetId, filtered(), isPrivate, voice);
        } else {
            content = NEWLINE_PATTERN.matcher(content).replaceAll("\n").trim();
            messageId = send(targetId, content, isPrivate, voice);
        }
        return QQMessage.assistant(content).id(messageId);
    }

    public List<QQMessage> chain(QQMessage message, boolean voice) {
        boolean isPrivate = message.isPrivate();
        Long targetId = isPrivate ? message.getUserId() : message.getGroupId();
        String content = message.getContent();
        if (content.contains("<discard />"))
            return List.of(QQMessage.assistant("回复被拒绝"));
        if (filter(content)) {
            Integer messageId = send(targetId, filtered(), isPrivate, voice);
            return List.of(QQMessage.assistant("回复被过滤").id(messageId));
        }
        content = NEWLINE_PATTERN.matcher(content).replaceAll("\n").trim();
        Matcher matcher = SEGMENT_PATTERN.matcher(content);
        List<QQMessage> messages = new ArrayList<>();
        while (matcher.find()) {
            String segment = matcher.group(1).trim();
            if (segment.startsWith("<cmd>") && segment.endsWith("</cmd>")) {
                String cmd = segment.substring(
                        "<cmd>".length(), segment.length() - "</cmd>".length()).trim();
                if (cmd.isEmpty()) continue;
                eventPublisher.publishEvent(CmdEvent.of(cmd));
                messages.add(QQMessage.assistant(segment));
            } else {
                if (segment.isEmpty()) continue;
                Integer messageId = send(targetId, segment, isPrivate, voice);
                messages.add(QQMessage.assistant(segment).id(messageId));
            }
        }
        return messages;
    }

    // =================== 工具方法 ===================

    private Integer send(Long targetId, String message, boolean isPrivate, boolean voice) {
        return  isPrivate
                ? botOperator.sendPrivateMsg(targetId, voice ? voiced(message) : message)
                : botOperator.sendGroupMsg(targetId, voice ? voiced(message) : message);
    }

    boolean filter(String message) {
        if (!MsgUtil.validateCq(message)) return true;
        for (Pattern pattern : FILTERED_PATTERNS)
            if (pattern.matcher(message).find()) return true;
        return false;
    }

    // =================== 消息方法 ===================

    private String filtered() {
        return MsgUtils.builder()
                // .text("[AI] ⚠️回复被过滤")
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
