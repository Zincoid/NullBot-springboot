package org.bot.nullbot.command;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import org.bot.nullbot.exception.NullBotLogException;

import java.util.List;

public interface Command
{
    default void execute(Bot bot, GroupMessageEvent event, List<String> params) throws Exception { throw new NullBotLogException("暂无群消息事件响应方式"); }
    default void execute(Bot bot, PokeNoticeEvent event, List<String> params) throws Exception { throw new NullBotLogException("暂无群戳戳事件响应方式"); }
    default void execute(Bot bot, GroupMsgDeleteNoticeEvent event, List<String> params) throws Exception { throw new NullBotLogException("暂无群撤回事件响应方式"); }

    default String getHelp() { return "暂无帮助文档"; }
    default String getHelpForAI() { return getHelp(); }

    default Integer getAccess() { return 0; }
}
