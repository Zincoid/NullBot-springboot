package com.zincoid.nullbot.core.model.result;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchResult {

    private boolean ok;
    private Long selfGroupId;
    private Long oppGroupId;
    private String message;

    public static MatchResult success(Long selfGroupId, Long oppGroupId, String message) {
        return new MatchResult(true, selfGroupId, oppGroupId, message);
    }

    public static MatchResult success(Long oppGroupId, String message) {
        return success(BotCtx.getGroupId(), oppGroupId, message);
    }

    public static MatchResult success(String message) {
        return success(BotCtx.getGroupId(), message);
    }

    public static MatchResult fail(Long groupId, String message) {
        return new MatchResult(false, groupId, null, "❌" + message);
    }

    public static MatchResult fail(String message) {
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
