package org.bot.qqbot.plugin.util;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.model.ArrayMsg;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageParseUtil {
    public static String parseGroupArrayMsgForAI(Bot bot, List<ArrayMsg> arrayMsgs) {
        StringBuilder message = new StringBuilder();
        for (ArrayMsg msg : arrayMsgs) {
            Map<String, String> data = msg.getData();
            switch (msg.getType()) {
                case text -> message.append(data.get("text"));
                case reply -> {
                    int replyId = Integer.parseInt(data.get("id"));
                    GetMsgResp replyMsg = bot.getMsg(replyId).getData();
                    message.append("[引用 ")
                            .append(replyMsg.getSender().getNickname())
                            .append(":")
                            .append(replyMsg.getRawMessage().replaceAll("\\[CQ:.*?\\]", ""))
                            .append("] ");
                }
                case at -> {
                    long qq = Long.parseLong(data.get("qq"));
                    String nickname = bot.getStrangerInfo(qq, true).getData().getNickname();
                    message.append("@").append(nickname);
                }
            }
        }
        return message.toString();
    }

    public static Map<String, String> parseGroupRawMessageAsImageMap(String rawMessage) {
        return Pattern.compile("\\[CQ:image([^\\]]+)\\]")
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
