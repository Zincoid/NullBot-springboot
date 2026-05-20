package com.zincoid.nullbot.function;

import com.mikuac.shiro.core.Bot;

@FunctionalInterface
public interface BotConsumer<T extends Bot, A extends Number, B> {
    void accept(T bot, A a, B b);
}
