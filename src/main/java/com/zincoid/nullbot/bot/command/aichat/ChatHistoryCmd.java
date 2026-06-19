package com.zincoid.nullbot.bot.command.aichat;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.module.ai.chat.client.impl.QQChatClient;
import com.zincoid.nullbot.core.enums.Role;
import com.zincoid.nullbot.core.module.ai.chat.message.StdMessage;
import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.module.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.enums.Emoji;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.interactor.BotInputer;
import com.zincoid.nullbot.bot.interactor.BotPageSelector;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CmdMapping({"ChatHistory", "聊天历史"})
@Component
public class ChatHistoryCmd implements Cmd {

    private static final int PAGE_SIZE = 10;  // 查询单页大小
    private static final int WAIT_TIMEOUT_SECONDS = 60;  // 等待超时时间

    private final QQChatClient qqChatClient;

    public ChatHistoryCmd(@Lazy QQChatClient qqChatClient) {
        this.qqChatClient = qqChatClient;
    }

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        List<Message> history = qqChatClient.history(BotCtx.getChatId());
        if (history.isEmpty())
            throw new BotInfoException(Emoji.INFO, "暂无历史");

        List<String> strings = history.stream().map(msg -> {
                    if (msg instanceof QQMessage qMsg)
                        return qMsg.getRole() == Role.USER
                                ? qMsg.getUserName() + ": " + qMsg.getContent()
                                : "Null: " + msg.getContent();
                    if (msg instanceof StdMessage bMsg)
                        return switch (bMsg.getRole()) {
                            case USER -> "User: [Extra]";
                            case ASSISTANT -> "Null: [ToolCalls]";
                            case SYSTEM -> "System: [Extra]";
                            case TOOL -> "Tool: [Results]";
                        };
                    return "未知类型消息";
                }
        ).toList();

        BotPageSelector<Message, String> pager = BotPageSelector.builder(
                bot, groupId, "聊天历史", true,
                history, strings, this::sendInfo
        ).userId(userId).size(PAGE_SIZE).current(Integer.MAX_VALUE).build();

        pager.start(new BotInputer(userId).timeout(WAIT_TIMEOUT_SECONDS));  // 新方案
    }

    private void sendInfo(Bot bot, Long groupId, Message message) {
        bot.sendGroupMsg(groupId, message.toMap().toString(), true);
        log.info("☑ [ChatHistory] 记录已获取: {}", message.toMap());
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatHistory 命令
                功能: 获取聊天历史
                限权: %d 级
                格式: ChatHistory [可选: 页码]
                别名: 聊天历史""", getAccess()
        );
    }
}
