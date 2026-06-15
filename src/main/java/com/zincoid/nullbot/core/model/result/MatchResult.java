package com.zincoid.nullbot.core.model.result;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchResult {

    private boolean ok;
    private String message;
    private Long selfGroupId;
    private Long oppGroupId;

    public static MatchResult success(String message, Long selfGroupId, Long oppGroupId) {
        return new MatchResult(true, message, selfGroupId, oppGroupId);
    }

    public static MatchResult fail(String message, Long selfGroupId) {
        return new MatchResult(false, message, selfGroupId, null);
    }

    public void send() {
        Bot bot = BotCtx.getBot();
        if (selfGroupId != null) bot.sendGroupMsg(selfGroupId, message, false);
        if (ok && oppGroupId != null) bot.sendGroupMsg(oppGroupId, message, false);
    }
}
