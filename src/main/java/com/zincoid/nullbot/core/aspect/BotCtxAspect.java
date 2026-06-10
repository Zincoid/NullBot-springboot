package com.zincoid.nullbot.core.aspect;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.Event;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import com.zincoid.nullbot.core.annotation.BotContext;
import com.zincoid.nullbot.core.service.system.SettingService;
import com.zincoid.nullbot.core.context.BotCtx;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BotCtxAspect {

    private final SettingService settingService;

    @Around("@annotation(context) || @within(context)")
    public Object load(ProceedingJoinPoint joinPoint, BotContext context) throws Throwable {
        Bot bot = (Bot) joinPoint.getArgs()[0];
        Event event = (Event) joinPoint.getArgs()[1];
        try {
            BotCtx.setCore(bot, event);
            switch (event) {
                case GroupMessageEvent e -> BotCtx.setGroup(e.getUserId(), e.getGroupId(), settingService.get(e.getGroupId()));
                case PrivateMessageEvent e -> BotCtx.setPrivate(e.getUserId());
                case GroupMsgDeleteNoticeEvent e -> BotCtx.setGroup(e.getUserId(), e.getGroupId(), settingService.get(e.getGroupId()));
                case PokeNoticeEvent e -> {
                    if (e.getGroupId() == null) BotCtx.setPrivate(e.getUserId());
                    else BotCtx.setGroup(e.getUserId(), e.getGroupId(), settingService.get(e.getGroupId()));
                }
                default -> {
                    BotCtx.setUnknown();
                    log.warn("  [BotCtxAspect] 未知事件类型: {}", event.getClass().getSimpleName());
                }
            }
            return joinPoint.proceed();
        } finally {
            BotCtx.remove();
        }
    }
}
