package com.zincoid.nullbot.core.function;

import com.mikuac.shiro.core.Bot;

@FunctionalInterface
public interface BotGroupEntityConsumer<T> {

    void accept(Bot bot, Long groupId, T entity);
}
