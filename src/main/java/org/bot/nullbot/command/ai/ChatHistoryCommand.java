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

@CommandMapping({"ChatHistory", "聊天历史"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();
            String history = deepSeekClient.getHistoryAsString(groupId, userId);
            bot.sendGroupMsg(groupId, "[聊天历史] ✅已获取！" + history, false);
            log.info("\t\t\t\t├─[AI.ChatHistory] 已获取 - 历史聊天记录");
        }else
            log.info("\t\t\t\t├─[AI.ChatHistory] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatHistory 命令
                功能: 获取AI聊天历史
                限权: %d
                格式: ChatHistory
                中文命令: 聊天历史""", getAccess()
        );
    }
}
