package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@CommandMapping({"Chat", "对话"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCommand implements Command
{
    @Value("${nullbot.command.prefix}")
    private String commandPrefix;

    private final DeepSeekClient deepSeekClient;
    private final ChatStorage chatStorage;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();

            if(chatStorage.isUserBanned(userId)) {
                LocalDateTime until = chatStorage.getUserBannedUntil(userId);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedUntil = until != null ? until.format(formatter) : "";
                bot.sendGroupMsg(groupId, "[AI] ⛔️你已被停用至！\n" + formattedUntil, false);
                log.info("\t\t\t\t├─[Chat] 已被停用至 - {}", until);
                return;
            }

            String message = MessageParseUtil.parseGroupArrayMsgForAI(bot, groupMessageEvent.getArrayMsg());
            String userName = groupMessageEvent.getSender().getNickname();
            Integer messageId = groupMessageEvent.getMessageId();

            String response;
            try {
                response = deepSeekClient.chat(messageId, groupId, userId, userName, message, bot, event);
            } catch (Exception e) {
                throw new NullBotMsgException("[AI] ❌出错:\n" + e.getMessage());
            }

            if (message.contains(commandPrefix))
                bot.sendGroupMsg(groupId, """
                                [AI] ⚠️检测到指令前缀
                                - 使用指令时请不要@Null
                                - @Null仅触发AI对话
                                - Null仅可执行部分指令""",
                        false
                );

            log.info("\t\t\t\t├─[Chat] 已回复: {}", response.replaceAll("\\R", " "));
        }else
            throw new NullBotLogException("[对话] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Chat 命令
                功能: 与AI对话
                限权: %d 级
                格式: Chat [内容] 或 @Null [内容] 或 戳一戳
                别名: 对话""", getAccess()
        );
    }
}
