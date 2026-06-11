package com.zincoid.nullbot.bot.command.aichat;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.module.ai.chat.message.QQMessage;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Chat", "对话"})
@Component
public class ChatCmd implements Cmd {

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;
    private final QQAiClient qqAiClient;

    public ChatCmd(@Lazy QQAiClient qqAiClient) {
        this.qqAiClient = qqAiClient;
    }

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        QQMessage message = QQMessage.user(args.nextFullString())
                .with(event.getGroupId(), event.getUserId(), event.getSender().getNickname())
                .id(event.getMessageId());
        String response = qqAiClient.chat(message);
        for (ArrayMsg msg : event.getArrayMsg()) {
            if (msg.getType() != MsgTypeEnum.text) continue;
            String text = msg.getStringData("text").trim();
            if (!text.startsWith(commandPrefix) || text.startsWith(commandPrefix + "Chat") || text.startsWith(commandPrefix + "对话")) continue;
            bot.sendGroupMsg(event.getGroupId(), """
                            ⚠️检测到指令前缀
                            - 使用指令不要@Null
                            - @Null仅触发对话""",
                    false
            );
            break;
        }
        log.info("☑ [Chat] 群聊已回复: {}", response);
    }

    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        QQMessage message = QQMessage.user(args.nextFullString())
                .with(event.getUserId(), event.getPrivateSender().getNickname())
                .id(event.getMessageId());
        String response = qqAiClient.chat(message);
        log.info("☑ [Chat] 私聊已回复: {}", response);
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
