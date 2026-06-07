package com.zincoid.nullbot.core.util;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import tools.jackson.databind.JsonNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MsgParseUtil {

    private static final Pattern STANDARD_CQ_PATTERN = Pattern.compile("\\[CQ:.*?]");
    private static final Pattern AT_CQ_PATTERN = Pattern.compile("\\[CQ:at,qq=(\\d+)]");
    private static final Pattern IMAGE_CQ_PATTERN = Pattern.compile("\\[CQ:image([^]]+)]");
    private static final Pattern VIDEO_CQ_PATTERN = Pattern.compile("\\[CQ:video([^]]+)]");
    private static final Pattern RECORD_CQ_PATTERN = Pattern.compile("\\[CQ:record([^]]+)]");
    private static final Pattern FILE_CQ_PATTERN = Pattern.compile("\\[CQ:file([^]]+)]");
    private static final Pattern SAYING_PATTERN = Pattern.compile("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}]\\[No\\.\\d+][\\s\\S]*");
    private static final Pattern FILE_PARAM_PATTERN = Pattern.compile("file=([^,]+)");
    private static final Pattern URL_PARAM_PATTERN = Pattern.compile("url=([^,]+)");

    private MsgParseUtil() {}

    // =================== @QQ号提取方法 ===================

    public static List<Long> extractAtNumbers(String rawMsg) {
        List<Long> qqNumbers = new ArrayList<>();
        Matcher matcher = AT_CQ_PATTERN.matcher(rawMsg);
        while (matcher.find()) {
            try {
                long qq = Long.parseLong(matcher.group(1));
                qqNumbers.add(qq);
            } catch (NumberFormatException e) {
                // 忽略格式错误的数字
                System.err.println("无效的QQ号: " + matcher.group(1));
            }
        }
        return qqNumbers;
    }

    // =================== 消息格式化方法 ===================

    public static String formatUserMsg(Bot bot, List<ArrayMsg> arrayMsgs) {
        StringBuilder message = new StringBuilder();
        for (ArrayMsg msg : arrayMsgs) {
            JsonNode data = msg.getData();
            switch (msg.getType()) {
                case image -> message.append("[图片]");
                case video -> message.append("[视频]");
                case text -> message.append(data.get("text").asString());
                case reply -> {
                    int replyId = data.get("id").asInt();
                    MsgResp replyMsg = bot.getMsg(replyId).getData();
                    message.append("[引用 ")
                            .append(replyMsg.getSender().getNickname())
                            .append(": ")
                            .append(replyMsg.getRawMessage()  // 回复消息内的@目前只转换为QQ号
                                    .replaceAll("\\[CQ:at,qq=(\\d+)]", "@$1")
                                    .replaceAll("\\[CQ:.*?]", "(不支持内容)"))
                            .append("]");
                }
                case at -> {
                    long qq = data.get("qq").asLong();
                    if (qq == bot.getSelfId()) {
                        message.append("@你");
                        continue;
                    }
                    String nickname = bot.getStrangerInfo(qq, true).getData().getNickname();
                    message.append("@").append(nickname).append("(").append(qq).append(")");
                }
                default -> message.append("[不支持内容]");
            }
        }
        return message.toString();
    }

    // =================== 语录格式化方法 ===================

    public static String formatSaying(Bot bot, String rawMsg) {
        if(SAYING_PATTERN.matcher(ShiroUtils.unescape(rawMsg)).matches())
            throw new BotWarnException("禁止套娃");

        Matcher matcher = AT_CQ_PATTERN.matcher(rawMsg);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            try {
                long qq = Long.parseLong(matcher.group(1));
                // 获取用户昵称
                String nickname = bot.getStrangerInfo(qq, true).getData().getNickname();
                // 构建替换文本: @昵称(QQ号)
                String replacement = "@" + nickname + "(" + qq + ")";
                matcher.appendReplacement(result, replacement);
            } catch (Exception e) {
                // 获取昵称失败: 使用QQ号
                String fallback = "@" + matcher.group(1);
                matcher.appendReplacement(result, fallback);
            }
        }
        matcher.appendTail(result);
        // 移除其他CQ码 (非@CQ码)
        String finalResult = result.toString();
        finalResult = finalResult.replaceAll("\\[CQ:(?!at\\b).*?]", "");

        if(finalResult.trim().isEmpty())
            throw new BotWarnException("禁止空文本");
        return finalResult;
    }

    // =================== 资源 URL 提取方法 ===================

    public static Map<String, String> extractImgMap(String rawMsg) {
        return parseCqCodeAsMap(rawMsg, IMAGE_CQ_PATTERN);
    }

    public static Map<String, String> extractVidMap(String rawMsg) {
        return parseCqCodeAsMap(rawMsg, VIDEO_CQ_PATTERN);
    }

    public static Map<String, String> extractRecMap(String rawMsg) {
        return parseCqCodeAsMap(rawMsg, RECORD_CQ_PATTERN);
    }

    public static Map<String, String> extractFileMap(String rawMsg) {
        return parseCqCodeAsMap(rawMsg, FILE_CQ_PATTERN);
    }

    private static Map<String, String> parseCqCodeAsMap(String rawMsg, Pattern cqPattern) {
        return cqPattern
                .matcher(rawMsg == null ? "" : rawMsg)
                .results()
                .map(match -> match.group(1))
                .map(params -> new AbstractMap.SimpleEntry<>(
                        FILE_PARAM_PATTERN.matcher(params).results()
                                .findFirst().map(m -> m.group(1)).orElse(null),
                        URL_PARAM_PATTERN.matcher(params).results()
                                .findFirst().map(m -> m.group(1).replace("&amp;", "&")).orElse(null)
                ))
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));
    }

    // =================== 违规CQ码检查方法 ===================

    public static boolean validateCq(String rawMsg) {
        if (rawMsg == null || !rawMsg.contains("CQ:")) return true;
        String withoutStandard = STANDARD_CQ_PATTERN.matcher(rawMsg).replaceAll("");
        return !withoutStandard.contains("CQ:");
    }
}
