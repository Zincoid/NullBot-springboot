package com.zincoid.nullbot.bot.command.aichat;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.ai.chat.client.impl.QQChatClient;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"ChatReset", "重置聊天"})
@Component
public class ChatResetCmd implements Cmd {

    private final QQChatClient qqChatClient;

    public ChatResetCmd(@Lazy QQChatClient qqChatClient) {
        this.qqChatClient = qqChatClient;
    }

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String chatId = BotCtx.getChatId();
        qqChatClient.clear(chatId);
        bot.sendGroupMsg(event.getGroupId(), "♻️历史已重置", false);
        log.info("☑ [ChatReset] 历史已重置 - ChatId: {}", chatId);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatReset 命令
                功能: 重置聊天历史
                限权: %d 级
                格式: ChatReset
                别名: 重置聊天""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ ChatReset 命令
                功能: 重置聊天历史
                格式: ChatReset""";
    }
}
