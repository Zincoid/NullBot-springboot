package com.zincoid.nullbot.core.module.game.model;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class GameRes {

    private boolean ok;
    private boolean async;
    private Long selfGroupId;
    private Long oppGroupId;
    private String selfMessage;
    private String oppMessage;

    public static GameRes success(boolean async, Long selfGroupId, Long oppGroupId, String selfMessage, String oppMessage) {
        return new GameRes(true, async, selfGroupId, oppGroupId, selfMessage, oppMessage);
    }

    public static GameRes fail(Long groupId, String message) {
        return new GameRes(false, false, groupId, null, "❌" + message, null);
    }

    public void send(Bot bot) {
        if (!ok) {
            if (selfGroupId != null)
                bot.sendGroupMsg(selfGroupId, selfMessage, false);
            return;
        }
        if (selfGroupId != null)
            bot.sendGroupMsg(selfGroupId, selfMessage, false);
        if (oppGroupId != null) {
            if (async) {
                bot.sendGroupMsg(oppGroupId, oppMessage, false);
            } else if (!Objects.equals(selfGroupId, oppGroupId)) {
                bot.sendGroupMsg(oppGroupId, selfMessage, false);
            }
        }
    }

    public void send() {
        send(BotCtx.getBot());
    }
}
