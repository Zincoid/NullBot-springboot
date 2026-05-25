package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Reply", "回复"})
@Component
@Slf4j
public class ReplyCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty()) throw new NullBotMsgException("[回复] ❌无参数");
        String message = String.join(" ", params.subList(0, params.size()));
        bot.sendGroupMsg(event.getGroupId(), message, false);
        log.info("├─[Reply] 群聊已回复 - {}", message);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) {
        if (params.isEmpty()) throw new NullBotMsgException("[回复] ❌无参数");
        String message = String.join(" ", params.subList(0, params.size()));
        bot.sendPrivateMsg(event.getUserId(), message, false);
        log.info("├─[Reply] 私聊已回复 - {}", message);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Reply 命令
                功能: 文本输出 (用于AI工具调用模式中间回复)
                限权: %d 级
                格式: Reply [内容]
                别名: 回复
                注意：
                1. 仅在工具调用模式下可使用
                2. 需要在回复文本后继续执行工具调用时，使用该指令进行文本回复
                3. 此次回复会视作一次工具调用，对话因此不会中断""", getAccess()
        );
    }
}
