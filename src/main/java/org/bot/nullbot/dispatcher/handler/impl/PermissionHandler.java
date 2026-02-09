package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.GroupService;
import org.bot.nullbot.service.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(0)
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionHandler implements Handler
{
    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        int commandAccess = command.getAccess();

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            int groupAccess = groupService.getGroupAccess(groupMessageEvent.getGroupId());
            if(groupAccess >= commandAccess){
                log.info("\t\t├─[PermissionHandler] 群限权满足");
            }else{
                log.info("\t\t├─[PermissionHandler] 群限权不足");
                return;
            }
            if(event.isAuthRequired()){
                int userAccess = userService.getUserAccess(groupMessageEvent.getSender().getUserId());
                if (userAccess >= commandAccess) {
                    log.info("\t\t├─[PermissionHandler] 用户限权满足");
                    chain.doHandle(bot, event, command);
                }else{
                    log.info("\t\t├─[PermissionHandler] 用户限权不足");
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Access] \uD83D\uDEAB限权不足\n需要限权等级: " + commandAccess + "\n你的限权等级: " + userAccess, false);
                }
            }else{
                log.info("\t\t├─[PermissionHandler] 无需验证用户限权");
                chain.doHandle(bot, event, command);
            }
        } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            int groupAccess = groupService.getGroupAccess(pokeNoticeEvent.getGroupId());
            if(groupAccess >= commandAccess){
                log.info("\t\t├─[PermissionHandler] 群限权满足");
            }else{
                log.info("\t\t├─[PermissionHandler] 群限权不足");
                return;
            }
            if(event.isAuthRequired()){
                int userAccess = userService.getUserAccess(pokeNoticeEvent.getUserId());
                if (userAccess >= commandAccess) {
                    log.info("\t\t├─[PermissionHandler] 用户限权满足");
                    chain.doHandle(bot, event, command);
                }else{
                    log.info("\t\t├─[PermissionHandler] 用户限权不足");
                    bot.sendGroupMsg(pokeNoticeEvent.getGroupId(), "[Access] \uD83D\uDEAB限权不足\n需要限权等级: " + commandAccess + "\n你的限权等级: " + userAccess, false);
                }
            }else{
                log.info("\t\t├─[PermissionHandler] 无需验证用户限权");
                chain.doHandle(bot, event, command);
            }
        } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent  groupMsgDeleteNoticeEvent) {
            int groupAccess = groupService.getGroupAccess(groupMsgDeleteNoticeEvent.getGroupId());
            if(groupAccess >= commandAccess){
                log.info("\t\t├─[PermissionHandler] 群限权满足");
                chain.doHandle(bot, event, command);
            }else{
                log.info("\t\t├─[PermissionHandler] 群限权不足");
            }
        } else {
            log.info("\t\t├─[PermissionHandler] 默认通过的事件类型");
            chain.doHandle(bot, event, command);
        }
    }
}
