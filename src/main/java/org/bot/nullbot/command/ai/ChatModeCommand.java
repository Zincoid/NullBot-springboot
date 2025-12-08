package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.ai.DeepSeekClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"ChatMode"})
@Component
@RequiredArgsConstructor
public class ChatModeCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ChatModeCommand.class);
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String mode = deepSeekClient.changeMode();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[AI.ChatMode] 聊天模式已切换至: " + mode, false);
            logger.info("\t\t\t\t├─[AI.ChatMode] 聊天模式已切换 - {}", mode);
        }else
            logger.info("\t\t\t\t├─[AI.ChatMode] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "ChatMode 命令\n功能: 切换AI聊天模式\n限权: " + getAccess() + "\n格式: ChatMode\nAI模式: Group-群聊会话 Personal-个人会话 Monitor-监听群聊";
    }
}
