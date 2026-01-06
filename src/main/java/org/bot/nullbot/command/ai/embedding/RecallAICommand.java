package org.bot.nullbot.command.ai.embedding;

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

@CommandMapping({"db3fbe2b"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class RecallAICommand implements Command
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
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[撤回AI消息] ❌参数非正", false);
                        log.info("\t\t\t\t├─[RecallAI] 参数非正");
                        return;
                    }
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[撤回AI消息] ❌参数格式错误", false);
                    log.info("\t\t\t\t├─[RecallAI] 参数格式错误");
                    return;
                }
            }

            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getSender().getUserId();

            List<ChatMessage> messages = chatStorage.getAIMessagesForRecall(deepSeekClient.getScope(), groupId, userId, n);
            for (ChatMessage message : messages) bot.deleteMsg(message.getMessageId());

            log.info("\t\t\t\t├─[RecallAI] 已撤回AI消息 -> {}条", n);
        }else
            log.info("\t\t\t\t├─[RecallAI] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ db3fbe2b 命令
                功能: 撤回AI发送的最近消息(仅文本消息)
                限权: %d 级
                格式: db3fbe2b [可选: 条数(默认为1)]
                示例: db3fbe2b 1
                注意: 已撤回的消息依然会存在于之后发给你的消息列表里！
                撤回你自己的消息必须用这个，不要用撤回用户消息的那个指令！""", getAccess()
        );
    }
}
