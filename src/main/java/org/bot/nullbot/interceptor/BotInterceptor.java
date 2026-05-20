package org.bot.nullbot.interceptor;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotMessageEventInterceptor;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.mikuac.shiro.exception.ShiroException;
import org.springframework.stereotype.Component;

@Component
public class BotInterceptor implements BotMessageEventInterceptor {

    @Override
    public boolean preHandle(Bot bot, MessageEvent event) throws ShiroException {
        return true;
    }

    @Override
    public void afterCompletion(Bot bot, MessageEvent event) throws ShiroException {
    }
}
