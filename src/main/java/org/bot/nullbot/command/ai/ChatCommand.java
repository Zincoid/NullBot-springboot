package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.component.storage.SysMsgStorage;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.dispatcher.CommandRegistry;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.entity.EmbeddedCommandEvent;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandMapping({"Chat", "聊天"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final ChatStorage chatStorage;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();

            if(chatStorage.isUserBanned(userId)) {
                LocalDateTime until = chatStorage.getUserBannedUntil(userId);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                String formattedUntil = until != null ? until.format(formatter) : "";
                bot.sendGroupMsg(groupId, "[封禁中] ⚠️你已被封禁至！\n" + formattedUntil, false);
                log.info("\t\t\t\t├─[AI.Chat] 已被封禁至: {}", until);
                return;
            }

            String message = MessageParseUtil.parseGroupArrayMsgForAI(bot, groupMessageEvent.getArrayMsg());
            String userName = groupMessageEvent.getSender().getNickname();
            Integer messageId = groupMessageEvent.getMessageId();

            String response = deepSeekClient.chat(messageId, groupId, userId, userName, message, bot, event);

            bot.sendGroupMsg(groupId, response, false);
            log.info("\t\t\t\t├─[AI.Chat] 已回复: {}", response.replaceAll("\\R", " "));
        }else
            log.info("\t\t\t\t├─[AI.Chat] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Chat 命令
                功能: 与AI对话
                限权: %d
                格式: Chat [对话内容] 或 @Null [对话内容] 或 戳一戳
                中文命令: 聊天""", getAccess()
        );
    }
}
