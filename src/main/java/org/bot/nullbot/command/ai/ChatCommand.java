package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@CommandMapping({"Chat", "对话"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final ChatStorage chatStorage;
    private final SettingManager settingManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getSender().getUserId();
            Long groupId = groupMessageEvent.getGroupId();

            if(chatStorage.isUserBanned(userId)) {
                LocalDateTime until = chatStorage.getUserBannedUntil(userId);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                String formattedUntil = until != null ? until.format(formatter) : "";
                bot.sendGroupMsg(groupId, "[AI] ⚠️你已被停用至！\n" + formattedUntil, false);
                log.info("\t\t\t\t├─[AI.Chat] 已被停用至 - {}", until);
                return;
            }

            String message = MessageParseUtil.parseGroupArrayMsgForAI(bot, groupMessageEvent.getArrayMsg());
            String userName = groupMessageEvent.getSender().getNickname();
            Integer messageId = groupMessageEvent.getMessageId();

            String response = deepSeekClient.chat(
                    messageId, groupId, userId, userName, message, bot, event,
                    settingManager.getChatOption(groupId)
            );

            log.info("\t\t\t\t├─[AI.Chat] 已回复: {}", response.replaceAll("\\R", " "));
        }else
            log.info("\t\t\t\t├─[AI.Chat] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Chat 命令
                功能: 与AI对话
                限权: %d 级
                格式: Chat [内容] 或 @Null [内容] 或 戳一戳
                中文命令: 对话""", getAccess()
        );
    }
}
