package com.zincoid.nullbot.core.functional;

import com.mikuac.shiro.core.Bot;

@FunctionalInterface
public interface BotGroupEntityConsumer<T> {

    void accept(Bot bot, Long groupId, T entity);
}
