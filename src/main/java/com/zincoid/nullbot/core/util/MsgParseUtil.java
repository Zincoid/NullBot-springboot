package com.zincoid.nullbot.core.util;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.exception.NullBotException;
import tools.jackson.databind.JsonNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MsgParseUtil {

    private MsgParseUtil() {}

    // =================== @QQ号提取方法 ===================

    public static List<Long> extractAtNumbers(String rawMsg) {
        List<Long> qqNumbers = new ArrayList<>();
        // 正则表达式匹配 [CQ:at,qq=数字]
        Pattern pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]");
        Matcher matcher = pattern.matcher(rawMsg);
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
        if(Pattern.matches("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}]\\[No\\.\\d+][\\s\\S]*", ShiroUtils.unescape(rawMsg)))
            throw new NullBotException("禁止套娃");

        Pattern atPattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]");
        Matcher matcher = atPattern.matcher(rawMsg);
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
            throw new NullBotException("禁止空文本");
        return finalResult;
    }

    // =================== 资源 URL 提取方法 ===================

    public static Map<String, String> extractImgMap(String rawMsg) {
        return parseCqCodeAsMap(rawMsg, Pattern.compile("\\[CQ:image([^]]+)]"));
    }

    public static Map<String, String> extractVidMap(String rawMsg) {
        return parseCqCodeAsMap(rawMsg, Pattern.compile("\\[CQ:video([^]]+)]"));
    }

    public static Map<String, String> extractRecMap(String rawMsg) {
        return parseCqCodeAsMap(rawMsg, Pattern.compile("\\[CQ:record([^]]+)]"));
    }

    public static Map<String, String> extractFileMap(String rawMsg) {
        return parseCqCodeAsMap(rawMsg, Pattern.compile("\\[CQ:file([^]]+)]"));
    }

    private static Map<String, String> parseCqCodeAsMap(String rawMsg, Pattern cqPattern) {
        return cqPattern
                .matcher(rawMsg == null ? "" : rawMsg)
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
