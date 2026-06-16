package com.zincoid.nullbot.core.module.game.model;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class MatchRes {

    private boolean ok;
    private Set<Long> groupIds;
    private String message;

    public static MatchRes success(Set<Long> groupIds, String message) {
        return new MatchRes(true, groupIds, message);
    }

    public static MatchRes success(String message) {
        return success(Set.of(BotCtx.getGroupId()), message);
    }

    public static MatchRes fail(Set<Long> groupIds, String message) {
        return new MatchRes(false, groupIds, "❌" + message);
    }

    public static MatchRes fail(String message) {
        return fail(Set.of(BotCtx.getGroupId()), message);
    }

    public void send() {
        send(BotCtx.getBot());
    }

    public void send(Bot bot) {
        Set<Long> sent = new HashSet<>();
        for (Long gid : groupIds)
            if (gid != null && sent.add(gid))
                bot.sendGroupMsg(gid, message, false);
    }
}
