package com.zincoid.nullbot.core.model.bot.event;

import com.mikuac.shiro.core.Bot;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmbeddedCommandEvent {
    private Bot bot;
    private CommandEvent<?> event;
}
