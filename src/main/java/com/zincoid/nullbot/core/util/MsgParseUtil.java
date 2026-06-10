package com.zincoid.nullbot.core.util;

import com.mikuac.shiro.common.utils.MessageConverser;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.exception.BotWarnException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MsgParseUtil {

    private static final Pattern STANDARD_CQ_PATTERN = Pattern.compile("\\[CQ:.*?]");
    private static final Pattern SAYING_PATTERN = Pattern.compile("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}]\\[No\\.\\d+][\\s\\S]*");

    private MsgParseUtil() {}

    // =================== @QQ号提取方法 ===================

    public static List<Long> extractAtNumbers(List<ArrayMsg> arrayMsg) {
        return ShiroUtils.getAtList(arrayMsg);
    }

    // =================== 消息格式化方法 ===================

    public static String formatMsg(Bot bot, List<ArrayMsg> arrayMsgs) {
        StringBuilder message = new StringBuilder();
        for (ArrayMsg msg : arrayMsgs) {
            switch (msg.getType()) {
                case image -> message.append("[图片]");
                case video -> message.append("[视频]");
                case text -> message.append(msg.getStringData("text"));
                case reply -> {
                    long replyId = msg.getLongData("id");
                    MsgResp replyMsg = bot.getMsg((int) replyId).getData();
                    message.append("[引用 ")
                            .append(replyMsg.getSender().getNickname())
                            .append(": ");
                    for (ArrayMsg rm : replyMsg.getArrayMsg()) {
                        switch (rm.getType()) {
                            case image -> message.append("[图片]");
                            case video -> message.append("[视频]");
                            case text -> message.append(rm.getStringData("text"));
                            case at -> message.append("@").append(rm.getLongData("qq"));
                            default -> message.append("(不支持内容)");
                        }
                    }
                    message.append("]");
                }
                case at -> {
                    long qq = msg.getLongData("qq");
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

    public static String formatSaying(Bot bot, List<ArrayMsg> arrayMsg) {
        String rawMsg = ShiroUtils.unescape(MessageConverser.arraysToString(arrayMsg));
        if (SAYING_PATTERN.matcher(rawMsg).matches())
            throw new BotWarnException("禁止套娃");
        StringBuilder sb = new StringBuilder();
        for (ArrayMsg msg : arrayMsg) {
            switch (msg.getType()) {
                case text -> sb.append(msg.getStringData("text"));
                case at -> {
                    long qq = msg.getLongData("qq");
                    if (qq == 0) {
                        sb.append("@全体成员");
                        continue;
                    }
                    try {
                        String nickname = bot.getStrangerInfo(qq, true).getData().getNickname();
                        sb.append("@").append(nickname).append("(").append(qq).append(")");
                    } catch (Exception e) {
                        sb.append("@").append(qq);
                    }
                }
                default -> {}  // image, video, record 等 → 跳过
            }
        }
        if (sb.toString().trim().isEmpty())
            throw new BotWarnException("禁止空文本");
        return sb.toString();
    }

    // =================== 资源 URL 提取方法 ===================

    public static Map<String, String> extractFileMap(List<ArrayMsg> arrayMsg) {
        if (arrayMsg == null || arrayMsg.isEmpty()) return Collections.emptyMap();
        return arrayMsg.stream()
                .filter(it -> "file".equals(it.getRawType()))
                .map(MsgParseUtil::toFileUrlEntry)
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));
    }

    public static Map<String, String> extractImgMap(List<ArrayMsg> arrayMsg) {
        return extractMediaMap(arrayMsg, MsgTypeEnum.image);
    }

    public static Map<String, String> extractVidMap(List<ArrayMsg> arrayMsg) {
        return extractMediaMap(arrayMsg, MsgTypeEnum.video);
    }

    public static Map<String, String> extractRecMap(List<ArrayMsg> arrayMsg) {
        return extractMediaMap(arrayMsg, MsgTypeEnum.record);
    }

    private static Map<String, String> extractMediaMap(List<ArrayMsg> arrayMsg, MsgTypeEnum type) {
        if (arrayMsg == null || arrayMsg.isEmpty()) return Collections.emptyMap();
        return arrayMsg.stream()
                .filter(it -> it.getType() == type)
                .map(MsgParseUtil::toFileUrlEntry)
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));
    }

    private static AbstractMap.SimpleEntry<String, String> toFileUrlEntry(ArrayMsg it) {
        String file = it.getStringData("file");
        file = file.isEmpty() ? null : file;
        String url = it.getStringData("url");
        url = url.isEmpty() ? null : ShiroUtils.unescape(url);
        return new AbstractMap.SimpleEntry<>(file, url);
    }

    // =================== 违规CQ码检查方法 ===================

    public static boolean validateCq(String rawMsg) {
        if (rawMsg == null || !rawMsg.contains("CQ:")) return true;
        String withoutStandard = STANDARD_CQ_PATTERN.matcher(rawMsg).replaceAll("");
        return !withoutStandard.contains("CQ:");
    }
}
