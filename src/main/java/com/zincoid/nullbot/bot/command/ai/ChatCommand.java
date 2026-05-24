package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Chat", "对话"})
@Component
@Slf4j
public class ChatCommand implements Command {

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;
    private final QQAiClient qqAiClient;

    public ChatCommand(@Lazy QQAiClient qqAiClient) {
        this.qqAiClient = qqAiClient;
    }

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Integer messageId = event.getMessageId();
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String content = String.join(" ", params);
        String response;
        try {
            QQMessage message = QQMessage.user(content).with(groupId, userId, userName).id(messageId);
            response = qqAiClient.chat(BotCtxUtil.getChatId(), message, event, BotCtxUtil.getSetting());
        } catch (Exception e) {
            throw new NullBotMsgException("[AI] ❌出错: " + e.getMessage());
        }
        for (ArrayMsg msg : event.getArrayMsg()) {
            if (msg.getType() != MsgTypeEnum.text) continue;
            String text = msg.getData().get("text").asString().trim();
            if (!text.startsWith(commandPrefix) || text.startsWith(commandPrefix + "Chat") || text.startsWith(commandPrefix + "对话")) continue;
            bot.sendGroupMsg(groupId, """
                            [AI] ⚠️检测到指令前缀
                            - 使用指令时请不要@Null
                            - @Null仅触发AI对话
                            - Null仅可执行部分指令""",
                    false
            );
            break;
        }
        log.info("\t\t\t\t├─[Chat] 群聊已回复: {}", response);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) {
        Integer messageId = event.getMessageId();
        Long userId = event.getUserId();
        String userName = event.getPrivateSender().getNickname();
        String content = String.join(" ", params);
        String response;
        try {
            QQMessage message = QQMessage.user(content).with(userId, userName).id(messageId);
            response = qqAiClient.chat(BotCtxUtil.getChatId(), message, event);
        } catch (Exception e) {
            throw new NullBotMsgException("[AI] ❌出错: " + e.getMessage());
        }
        log.info("\t\t\t\t├─[Chat] 私聊已回复: {}", response);
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
