package com.zincoid.nullbot.core.module.game.model;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchRes {

    private boolean ok;
    private Long selfGroupId;
    private Long oppGroupId;
    private String message;

    public static MatchRes success(Long selfGroupId, Long oppGroupId, String message) {
        return new MatchRes(true, selfGroupId, oppGroupId, message);
    }

    public static MatchRes success(Long oppGroupId, String message) {
        return success(BotCtx.getGroupId(), oppGroupId, message);
    }

    public static MatchRes success(String message) {
        return success(BotCtx.getGroupId(), message);
    }

    public static MatchRes fail(Long groupId, String message) {
        return new MatchRes(false, groupId, null, "❌" + message);
    }

    public static MatchRes fail(String message) {
        return fail(BotCtx.getGroupId(), message);
    }

    public void send() {
        Bot bot = BotCtx.getBot();
        if (selfGroupId != null)
            bot.sendGroupMsg(selfGroupId, message, false);
        if (ok && oppGroupId != null && !oppGroupId.equals(selfGroupId))
            bot.sendGroupMsg(oppGroupId, message, false);
    }
}
