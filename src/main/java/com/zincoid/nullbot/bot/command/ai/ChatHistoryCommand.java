package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.bot.interaction.BotInputer;
import com.zincoid.nullbot.core.model.bot.interaction.BotPageSelector;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ChatHistory", "聊天历史"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryCommand implements Command {

    private final QQAiClient qqAiClient;
    // private final BotInputManager botInputManager;

    private static final int PAGE_SIZE = 10;  // 查询单页大小
    private static final int WAIT_TIMEOUT = 60;  // 等待超时时间 (单位: Second)

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        List<QQMessage> history = qqAiClient.history(groupId, userId);
        if (history.isEmpty())
            throw new NullBotMsgException("[聊天历史] ⚠️无对话历史");

        BotPageSelector<QQMessage, String> pager = BotPageSelector.builder(
                bot, groupId, "聊天历史", true,
                history,
                history.stream()
                        .map(msg ->
                                msg.getRole() == Role.USER ?
                                        "%s(%s): %s".formatted(
                                                msg.getUserName(),
                                                msg.getUserId(),
                                                msg.getContent()
                                        ) :
                                        "Null: %s".formatted(msg.getContent())
                        ).toList(),
                this::sendInfo
        ).userId(userId).size(PAGE_SIZE).current(Integer.MAX_VALUE).build();

        // pager.init();
        // while (pager.input(botInputManager, WAIT_TIMEOUT)) {
        //     log.info("\t\t\t\t├─[ChatHistory] 已操作分页器");
        // }
        BotInputer in = new BotInputer(userId).timeout(WAIT_TIMEOUT);
        pager.start(in);
    }

    private void sendInfo(Bot bot, Long groupId, QQMessage message) {
        bot.sendGroupMsg(groupId, message.toString(), true);
        log.info("\t\t\t\t├─[ChatHistory] 已获取记录 - {}", message.getMessageId());
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
