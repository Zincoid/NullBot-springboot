package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.enums.ChatScope;
import org.bot.nullbot.exception.NullBotLogException;
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
            ChatScope chatScope = deepSeekClient.clearHistory(groupId, userId);
            Long id = switch (chatScope) {
                case Group, Monitor ->  groupId;
                case Personal -> userId;
            };
            bot.sendGroupMsg(groupId, """
                    [重置聊天] ♻️聊天历史已重置
                    - ChatScope: %s
                    - ID: %s""".formatted(chatScope, id), false);
            log.info("\t\t\t\t├─[ChatReset] 聊天历史已重置 - {}: {}", chatScope, id);
        }else
            throw new NullBotLogException("[重置聊天] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatReset 命令
                功能: 重置AI聊天记忆
                限权: %d 级
                格式: ChatReset
                别名: 重置聊天""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ ChatReset 命令
                功能: 重置AI聊天记忆
                格式: ChatReset
                注意: 该指令无法撤回已发送群聊消息""";
    }
}
