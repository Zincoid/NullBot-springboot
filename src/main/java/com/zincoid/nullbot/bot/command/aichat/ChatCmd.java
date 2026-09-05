package com.zincoid.nullbot.bot.command.aichat;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.bot.gateway.processor.CmdRegistry;
import com.zincoid.nullbot.core.module.ai.chat.manage.AiCostManager;
import com.zincoid.nullbot.core.module.ai.chat.client.impl.QQChatClient;
import com.zincoid.nullbot.core.module.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.properties.bot.CmdProperties;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Chat", "对话"})
@Component
public class ChatCmd implements Cmd {

    private final QQChatClient qqChatClient;
    private final CmdRegistry cmdRegistry;
    private final CmdProperties cmdProperties;
    private final AiCostManager aiCostManager;

    public ChatCmd(@Lazy QQChatClient qqChatClient, @Lazy CmdRegistry cmdRegistry,
                   CmdProperties cmdProperties, AiCostManager aiCostManager) {
        this.qqChatClient = qqChatClient;
        this.cmdRegistry = cmdRegistry;
        this.cmdProperties = cmdProperties;
        this.aiCostManager = aiCostManager;
    }

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        if (aiCostManager.isOutOfBalance())
            throw new BotWarnException("AI 欠费已停用");
        QQMessage message = QQMessage.user(args.rest())
                .with(event.getGroupId(), event.getUserId(), event.getSender().getNickname())
                .id(event.getMessageId());
        String response = qqChatClient.handle(message).call().getContent();
        for (ArrayMsg msg : event.getArrayMsg()) {
            if (msg.getType() != MsgTypeEnum.text) continue;
            String text = msg.getStringData("text").trim();
            if (!text.startsWith(cmdProperties.getPrefix()) || cmdRegistry.isCmdOf(text, ChatCmd.class)) continue;
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
        if (aiCostManager.isOutOfBalance()) {
            bot.sendPrivateMsg(event.getUserId(), "💤AI欠费已停用", false);
            return;
        }
        QQMessage message = QQMessage.user(args.rest())
                .with(event.getUserId(), event.getPrivateSender().getNickname())
                .id(event.getMessageId());
        String response = qqChatClient.handle(message).call().getContent();
        log.info("☑ [Chat] 私聊已回复: {}", response);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Chat 命令
                功能: 与AI对话
                限权: %d 级
                格式:
                1. Chat [内容]
                2. @Null [内容]
                3. 戳一戳
                别名: 对话
                注意：
                - 欠费后可用 Recover 指令恢复""", getAccess()
        );
    }
}
