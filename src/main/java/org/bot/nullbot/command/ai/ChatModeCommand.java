package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.springframework.stereotype.Component;

@CommandMapping({"ChatMode", "聊天模式"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatModeCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String scope = deepSeekClient.changeScope();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[聊天模式] \uD83D\uDD04已切换至: " + scope, false);
            log.info("\t\t\t\t├─[AI.ChatMode] 聊天模式已切换 - {}", scope);
        }else
            log.info("\t\t\t\t├─[AI.ChatMode] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatMode 命令
                功能: 切换AI聊天模式
                限权: %d
                格式: ChatMode
                AI模式: Group-群聊会话 Personal-个人会话 Monitor-监听群聊
                中文命令: 聊天模式""", getAccess()
        );
    }
}
