package com.zincoid.nullbot.bot.command;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.exception.BotWarnException;

public interface Cmd {

    default void run(Bot bot, GroupMessageEvent event, CmdArgs args) throws Exception { throw new BotWarnException("指令不支持群聊事件"); }
    default void run(Bot bot, PokeNoticeEvent event, CmdArgs args) throws Exception { throw new BotWarnException("指令不支持戳一戳事件"); }
    default void run(Bot bot, GroupMsgDeleteNoticeEvent event, CmdArgs args) throws Exception { throw new BotWarnException("指令不支持群撤回事件"); }
    default void run(Bot bot, PrivateMessageEvent event, CmdArgs args) throws Exception { throw new BotWarnException("指令不支持私聊事件"); }

    default String getHelp() { return "暂无帮助文档"; }
    default String getHelpForAI() { return getHelp(); }
    default Integer getAccess() { return 0; }
}
