package org.bot.nullbot.util;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.model.ArrayMsg;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MessageParseUtil
{
    private MessageParseUtil() {}

    // =================== @QQ号提取方法 ===================

    public static List<Long> extractAtQQNumbers(String message) {
        List<Long> qqNumbers = new ArrayList<>();
        // 正则表达式匹配 [CQ:at,qq=数字]
        Pattern pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]");
        Matcher matcher = pattern.matcher(message);
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

    public static String parseArrayMsgToSimple(Bot bot, List<ArrayMsg> arrayMsgs) {
        StringBuilder message = new StringBuilder();
        for (ArrayMsg msg : arrayMsgs) {
            Map<String, String> data = msg.getData();
            switch (msg.getType()) {
                case image -> message.append("[图片]");
                case video -> message.append("[视频]");
                case text -> message.append(data.get("text"));
                case reply -> {
                    int replyId = Integer.parseInt(data.get("id"));
                    GetMsgResp replyMsg = bot.getMsg(replyId).getData();
                    message.append("[引用 ")
                            .append(replyMsg.getSender().getNickname())
                            .append(": ")
                            .append(replyMsg.getRawMessage()  // 回复消息内的@目前只转换为QQ号
                                    .replaceAll("\\[CQ:at,qq=(\\d+)]", "@$1")
                                    .replaceAll("\\[CQ:.*?]", "(不支持内容)"))
                            .append("]");
                }
                case at -> {
                    long qq = Long.parseLong(data.get("qq"));
                    String nickname = bot.getStrangerInfo(qq, true).getData().getNickname();
                    message.append("@").append(nickname).append("(").append(qq).append(")");
                }
                default -> message.append("[不支持内容]");
            }
        }
        return message.toString();
    }

    // =================== 语录格式化方法 ===================

    @Deprecated
    public static String parseRawSaying(String rawSaying) {
        String text = rawSaying.replaceAll("\\[CQ:at,qq=(\\d+)]", "@$1").replaceAll("\\[CQ:.*?]", "");
        if(Pattern.matches("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}]\\[No\\.\\d+][\\s\\S]*", ShiroUtils.unescape(text)))
            throw new IllegalArgumentException("禁止套娃");
        if(text.trim().isEmpty())
            throw new IllegalArgumentException("禁止空文本");
        return text;
    }

    public static String parseRawSaying(Bot bot, String rawSaying) {
        if(Pattern.matches("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}]\\[No\\.\\d+][\\s\\S]*", ShiroUtils.unescape(rawSaying)))
            throw new IllegalArgumentException("禁止套娃");

        Pattern atPattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]");
        Matcher matcher = atPattern.matcher(rawSaying);
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
            throw new IllegalArgumentException("禁止空文本");
        return finalResult;
    }

    // =================== 资源 URL 提取方法 ===================

    public static Map<String, String> parseGroupRawMessageAsImageMap(String rawMessage) {
        return parseCqCodeToMap(rawMessage, Pattern.compile("\\[CQ:image([^]]+)]"));
    }

    public static Map<String, String> parseGroupRawMessageAsVideoMap(String rawMessage) {
        return parseCqCodeToMap(rawMessage, Pattern.compile("\\[CQ:video([^]]+)]"));
    }

    public static Map<String, String> parseGroupRawMessageAsRecordMap(String rawMessage) {
        return parseCqCodeToMap(rawMessage, Pattern.compile("\\[CQ:record([^]]+)]"));
    }

    public static Map<String, String> parseGroupRawMessageAsFileMap(String rawMessage) {
        return parseCqCodeToMap(rawMessage, Pattern.compile("\\[CQ:file([^]]+)]"));
    }

    private static Map<String, String> parseCqCodeToMap(String rawMessage, Pattern cqPattern) {
        return cqPattern
                .matcher(rawMessage == null ? "" : rawMessage)
                .results()
                .map(match -> match.group(1))
                .map(params -> new AbstractMap.SimpleEntry<>(
                        Pattern.compile("file=([^,]+)").matcher(params).results()
                                .findFirst().map(m -> m.group(1)).orElse(null),
                        Pattern.compile("url=([^,]+)").matcher(params).results()
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
}
