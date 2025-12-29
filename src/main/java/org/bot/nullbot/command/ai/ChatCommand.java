package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.entity.EmbeddedCommandEvent;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandMapping({"Chat", "聊天"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String message = MessageParseUtil.parseGroupArrayMsgForAI(bot, groupMessageEvent.getArrayMsg());
            String userName = groupMessageEvent.getSender().getNickname();
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();
            Integer messageId = groupMessageEvent.getMessageId();
            String response = deepSeekClient.chat(messageId, groupId, userId, userName, message);

            // 内嵌指令执行
            Matcher m = Pattern.compile("\\{(.*?)}").matcher(response);
            while (m.find()) {
                String command = m.group(1);  // 提取{}内的内容
                eventPublisher.publishEvent(new EmbeddedCommandEvent(bot, new CommandEvent<>(event.getEvent(), command)));
            }

            // 删除所有命令明文
            String processedResponse = response.replaceAll("\\{.*?}", "");

            bot.sendGroupMsg(groupId, processedResponse, false);
            log.info("\t\t\t\t├─[AI.Chat] 已回复: {}", processedResponse.replaceAll("\\R", " "));
        }else
            log.info("\t\t\t\t├─[AI.Chat] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Chat 命令\n功能: 与AI对话\n限权: " + getAccess() + "\n格式: Chat [对话内容] 或 @Null [对话内容] 或 戳一戳\n中文命令: 聊天";
    }
}
