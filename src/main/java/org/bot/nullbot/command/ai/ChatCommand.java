package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.DeepSeekClient;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"Chat", "聊天"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String message = MessageParseUtil.parseGroupArrayMsgForAI(bot, groupMessageEvent.getArrayMsg());
            String userName = groupMessageEvent.getSender().getNickname();
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();
            Integer messageId = groupMessageEvent.getMessageId();
            String response = deepSeekClient.chat(messageId, groupId, userId, userName, message);
            bot.sendGroupMsg(groupId, response, false);
            log.info("\t\t\t\t├─[AI.Chat] 已回复: {}", response.replaceAll("\\R", ""));
        }else
            log.info("\t\t\t\t├─[AI.Chat] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Chat 命令\n功能: 与AI对话\n限权: " + getAccess() + "\n格式: Chat [对话内容] 或 @Null [对话内容] 或 戳一戳\n中文命令: 聊天";
    }
}
