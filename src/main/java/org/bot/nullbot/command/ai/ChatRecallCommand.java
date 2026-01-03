package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ChatRecall", "recall", "聊天撤回", "撤回"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRecallCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final ChatStorage chatStorage;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            int n = 1;
            if(!event.getCommandParameters().isEmpty()){
                try {
                    n = Integer.parseInt(event.getCommandParameters().getFirst());
                    if(n <= 0){
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[聊天撤回] ❌参数非正", false);
                        log.info("\t\t\t\t├─[AI.ChatRecall] 参数非正");
                        return;
                    }
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[聊天撤回] ❌参数格式错误", false);
                    log.info("\t\t\t\t├─[AI.ChatRecall] 参数格式错误");
                    return;
                }
            }

            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getSender().getUserId();

            List<ChatMessage> messages = chatStorage.getAIMessagesForRecall(deepSeekClient.getScope(), groupId, userId, n);
            for (ChatMessage message : messages) bot.deleteMsg(message.getMessageId());

            log.info("\t\t\t\t├─[AI.ChatRecall] 已撤回 - {}条AI消息", n);
        }else
            log.info("\t\t\t\t├─[AI.ChatRecall] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatRecall 命令
                功能: 撤回AI发送的最近消息(仅文本消息 默认撤回1条)
                限权: %d
                格式: ChatRecall [可选: 条数]
                或 recall [可选: 条数]
                中文命令: 聊天撤回/撤回""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ ChatRecall 命令
                功能: 撤回AI发送的最近消息(仅文本消息 默认撤回1条)
                限权: %d
                格式: ChatRecall [可选: 条数]
                示例: ChatRecall 2""", getAccess()
        );
    }
}
