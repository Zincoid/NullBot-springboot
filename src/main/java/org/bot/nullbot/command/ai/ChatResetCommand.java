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

@CommandMapping({"ChatReset", "重置聊天"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatResetCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();
            String target = deepSeekClient.clearHistory(groupId, userId);
            bot.sendGroupMsg(groupId, "[聊天历史] ♻️" + target + "聊天已重置！", false);
            log.info("\t\t\t\t├─[AI.ChatReset] 已清除 - {} 历史聊天记录", target);
        }else
            log.info("\t\t\t\t├─[AI.ChatReset] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "◉ ChatReset 命令\n功能: 重置AI聊天记忆\n限权: " + getAccess() + "\n格式: ChatReset\n中文命令: 重置聊天";
    }
}
