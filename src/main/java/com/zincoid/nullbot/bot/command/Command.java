package com.zincoid.nullbot.bot.command;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.exception.BotWarnException;

public interface Command {

    default void execute(Bot bot, GroupMessageEvent event, CommandArgs args) throws Exception { throw new BotWarnException("暂无群聊事件响应方式"); }
    default void execute(Bot bot, PokeNoticeEvent event, CommandArgs args) throws Exception { throw new BotWarnException("暂无戳戳事件响应方式"); }
    default void execute(Bot bot, GroupMsgDeleteNoticeEvent event, CommandArgs args) throws Exception { throw new BotWarnException("暂无群撤回事件响应方式"); }
    default void execute(Bot bot, PrivateMessageEvent event, CommandArgs args) throws Exception { throw new BotWarnException("暂无私聊事件响应方式"); }

    default String getHelp() { return "暂无帮助文档"; }
    default String getHelpForAI() { return getHelp(); }
    default Integer getAccess() { return 0; }
}
