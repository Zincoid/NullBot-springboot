package com.zincoid.nullbot.core.model.bot.event;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class InnerCommandEvent {

    private final Bot bot;
    private final CommandEvent<?> event;

    public static InnerCommandEvent of(String command) {
        List<String> information = List.of(command.split(" "));
        return InnerCommandEvent.of(
                BotCtx.getBot(),
                CommandEvent.of(
                        BotCtx.getEvent(),
                        information.getFirst(),
                        information.subList(1, information.size()),
                        !BotCtx.getIsPrivate() && BotCtx.getSetting().isInnerCmdAuth(),
                        false
                )
        );
    }

    public static InnerCommandEvent of(String command, boolean authRequired) {
        List<String> information = List.of(command.split(" "));
        return InnerCommandEvent.of(
                BotCtx.getBot(),
                CommandEvent.of(
                        BotCtx.getEvent(),
                        information.getFirst(),
                        information.subList(1, information.size()),
                        authRequired,
                        false
                )
        );
    }
}
