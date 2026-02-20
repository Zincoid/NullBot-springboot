package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Chat", "对话"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCommand implements Command
{
    @Value("${nullbot.command.prefix}")
    private String commandPrefix;
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String message = MessageParseUtil.parseGroupArrayMsgForAI(bot, event.getArrayMsg());
        String response;
        try {
            response = deepSeekClient.chat(
                    event.getMessageId(),
                    event.getGroupId(),
                    event.getUserId(),
                    event.getSender().getNickname(),
                    message,
                    bot,
                    event
            );
        } catch (Exception e) {
            throw new NullBotMsgException("[AI] ❌出错:\n" + e.getMessage());
        }
        if (message.contains(commandPrefix))
            bot.sendGroupMsg(event.getGroupId(), """
                                [AI] ⚠️检测到指令前缀
                                - 使用指令时请不要@Null
                                - @Null仅触发AI对话
                                - Null仅可执行部分指令""",
                    false
            );
        log.info("\t\t\t\t├─[Chat] 已回复: {}", response.replaceAll("\\R", " "));
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
