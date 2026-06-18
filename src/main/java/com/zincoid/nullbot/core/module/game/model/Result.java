package com.zincoid.nullbot.core.module.game.model;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import com.zincoid.nullbot.core.module.system.BotOperator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Data
@AllArgsConstructor
public class Result {

    private boolean ok;
    private Map<Long, List<String>> messages;  // groupId -> messages

    public static Result success() {
        return new Result(true, new LinkedHashMap<>());
    }
    public static Result success(Collection<Long> groupIds, String message) {
        Result res = success();
        for (Long gid : groupIds) res.add(gid, message);
        return res;
    }
    public static Result fail() {
        return new Result(false, new LinkedHashMap<>());
    }
    public static Result fail(Long groupId, String message) {
        return fail().add(groupId, "❌" + message);
    }

    public Result add(Long groupId, String msg) {
        if (groupId != null && msg != null && !msg.isEmpty())
            messages.computeIfAbsent(groupId, k -> new ArrayList<>()).add(msg);
        return this;
    }

    public void send(Bot bot) {
        for (var entry : messages.entrySet())
            if (entry.getKey() != null)
                for (String msg : entry.getValue())
                    if (msg != null)
                        bot.sendGroupMsg(entry.getKey(), msg, false);
    }

    public void send() {
        send(BotCtx.getBot());
    }

    public void send(BotOperator botOperator) {
        send(botOperator.getBot());
    }
}
