package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import com.zincoid.nullbot.core.component.ai.chat.message.BaseMessage;
import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.enums.Emoji;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.bot.interaction.BotInputer;
import com.zincoid.nullbot.core.model.bot.interaction.BotPageSelector;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CommandMapping({"ChatHistory", "聊天历史"})
@Component
public class ChatHistoryCommand implements Command {

    private static final int PAGE_SIZE = 10;  // 查询单页大小
    private static final int WAIT_TIMEOUT_SECONDS = 60;  // 等待超时时间

    private final QQAiClient qqAiClient;

    public ChatHistoryCommand(@Lazy QQAiClient qqAiClient) {
        this.qqAiClient = qqAiClient;
    }

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        List<Message> history = qqAiClient.history(BotCtxUtil.getChatId());
        if (history.isEmpty())
            throw new BotInfoException(Emoji.INFO, "暂无历史");

        List<String> strings = history.stream().map(msg -> {
                    if (msg instanceof QQMessage qMsg)
                        return qMsg.getRole() == Role.USER
                                ? qMsg.getUserName() + ": " + qMsg.getContent()
                                : "Null: " + msg.getContent();
                    if (msg instanceof BaseMessage bMsg)
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
                功能: 获取AI聊天历史
                限权: %d 级
                格式: ChatHistory [可选: 页码]
                别名: 聊天历史""", getAccess()
        );
    }
}
