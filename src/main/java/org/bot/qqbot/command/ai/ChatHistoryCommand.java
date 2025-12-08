package org.bot.qqbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.qqbot.annotation.CommandMapping;
import org.bot.qqbot.command.Command;
import org.bot.qqbot.entity.CommandEvent;
import org.bot.qqbot.plugin.component.ai.DeepSeekClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"ChatHistory"})
@Component
@RequiredArgsConstructor
public class ChatHistoryCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ChatHistoryCommand.class);
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();
            String history = deepSeekClient.getHistoryAsString(groupId, userId);
            bot.sendGroupMsg(groupId, history, false);
            logger.info("\t\t\t\t├─[AI.ChatHistory] 已获取 - 历史聊天记录");
        }else
            logger.info("\t\t\t\t├─[AI.ChatHistory] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "/ChatHistory 命令\n功能: 获取AI聊天历史\n格式: /ChatHistory";
    }
}
