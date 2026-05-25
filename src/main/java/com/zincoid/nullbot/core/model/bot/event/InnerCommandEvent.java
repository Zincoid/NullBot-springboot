package com.zincoid.nullbot.core.model.bot.event;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InnerCommandEvent {

    private Bot bot;
    private CommandEvent<?> event;

    public static InnerCommandEvent of(String command) {
        return new InnerCommandEvent(
                BotCtxUtil.getBot(),
                new CommandEvent<>(BotCtxUtil.getEvent(), command,
                        !BotCtxUtil.getIsPrivate() && BotCtxUtil.getSetting().isInnerCmdAuth(),
                        false)
        );
    }

    public static InnerCommandEvent of(String command, boolean authRequired) {
        return new InnerCommandEvent(
                BotCtxUtil.getBot(),
                new CommandEvent<>(BotCtxUtil.getEvent(), command, authRequired, false)
        );
    }
}
