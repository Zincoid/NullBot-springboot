package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.ai.DeepSeekClient;
import org.bot.nullbot.plugin.util.MessageParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"Chat"})
@Component
@RequiredArgsConstructor
public class ChatCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ChatCommand.class);
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String message = MessageParseUtil.parseGroupArrayMsgForAI(bot, groupMessageEvent.getArrayMsg());
            String userName = groupMessageEvent.getSender().getNickname();
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();
            String response = deepSeekClient.chat(groupId, userId, userName, message);
            bot.sendGroupMsg(groupId, response, false);
            logger.info("\t\t\t\t├─[AI.Chat] 已回复: {}", response.replaceAll("\\R", ""));
        }else
            logger.info("\t\t\t\t├─[AI.Chat] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "/Chat 命令\n功能: 与AI聊天\n限权: 0\n格式: /Chat [对话内容] 或 @Null [对话内容] 或 戳一戳";
    }
}
