package org.bot.nullbot.function;

import com.mikuac.shiro.core.Bot;

@FunctionalInterface
public interface BotFunction<T extends Bot, A, B, R> {
    R apply(T bot, A a, B b);
}
