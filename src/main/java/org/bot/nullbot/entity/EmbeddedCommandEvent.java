package org.bot.nullbot.entity;

import com.mikuac.shiro.core.Bot;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EmbeddedCommandEvent {
    private Bot bot;
    private CommandEvent<?> event;
}
