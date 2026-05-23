package com.zincoid.nullbot.core.aspect;

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
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.service.SettingService;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BotCtxAspect {

    private final SettingService settingService;

    @Around("@annotation(context)")
    public Object load(ProceedingJoinPoint joinPoint, BotContext context) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            Long groupId = null;
            Long userId = null;
            if (arg instanceof GroupMessageEvent event) {
                groupId = event.getGroupId();
                userId = event.getUserId();
            } else if (arg instanceof PokeNoticeEvent event) {
                groupId = event.getGroupId();
                userId = event.getUserId();
            } else if (arg instanceof GroupMsgDeleteNoticeEvent event) {
                groupId = event.getGroupId();
                userId = event.getUserId();
            } else if (arg instanceof PrivateMessageEvent event) {
                userId = event.getUserId();
            }
            if (userId == null) continue;
            if (groupId == null) {
                BotCtxUtil.setPrivate(userId);
            } else {
                SettingPO setting = settingService.get(groupId);
                BotCtxUtil.setGroup(userId, groupId, setting);
            }
            break;
        }
        try {
            return joinPoint.proceed();
        } finally {
            BotCtxUtil.remove();
        }
    }
}
