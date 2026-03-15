package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.exception.NullBotMsgException;
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
        String response;
        try {
            response = deepSeekClient.chatGroup(
                    event.getMessageId(),
                    event.getGroupId(),
                    event.getUserId(),
                    event.getSender().getNickname(),
                    String.join(" ", params),
                    bot,
                    event
            );
        } catch (Exception e) {
            throw new NullBotMsgException("[AI] ❌出错: " + e.getMessage());
        }

        for (ArrayMsg msg : event.getArrayMsg()) {
            if (msg.getType() == MsgTypeEnum.text &&
                    msg.getData().get("text").trim().startsWith(commandPrefix)) {
                bot.sendGroupMsg(event.getGroupId(), """
                                [AI] ⚠️检测到指令前缀
                                - 使用指令时请不要@Null
                                - @Null仅触发AI对话
                                - Null仅可执行部分指令""",
                        false
                );
                break;
            }
        }

        log.info("\t\t\t\t├─[Chat] 群聊已回复: {}", response.replaceAll("\\R", " "));
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) {
        String response;
        try {
            response = deepSeekClient.chatPrivate(
                    event.getMessageId(),
                    event.getUserId(),
                    event.getPrivateSender().getNickname(),
                    String.join(" ", params),
                    bot,
                    event
            );
        } catch (Exception e) {
            throw new NullBotMsgException("[AI] ❌出错: " + e.getMessage());
        }
        log.info("\t\t\t\t├─[Chat] 私聊已回复: {}", response.replaceAll("\\R", " "));
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
