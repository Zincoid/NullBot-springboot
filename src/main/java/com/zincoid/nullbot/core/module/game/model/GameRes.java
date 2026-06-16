package com.zincoid.nullbot.core.module.game.model;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class GameRes {

    private boolean ok;
    private Map<Long, List<String>> messages;  // groupId -> messages

    public static GameRes success() {
        return new GameRes(true, new LinkedHashMap<>());
    }

    public static GameRes fail(Long groupId, String message) {
        return new GameRes(false, Map.of(groupId, List.of("❌" + message)));
    }

    public GameRes add(Long groupId, String msg) {
        if (groupId != null && msg != null && !msg.isEmpty())
            messages.computeIfAbsent(groupId, k -> new ArrayList<>()).add(msg);
        return this;
    }

    public void send() {
        send(BotCtx.getBot());
    }

    public void send(Bot bot) {
        for (var entry : messages.entrySet())
            if (entry.getKey() != null)
                for (String msg : entry.getValue())
                    if (msg != null)
                        bot.sendGroupMsg(entry.getKey(), msg, false);
    }
}
