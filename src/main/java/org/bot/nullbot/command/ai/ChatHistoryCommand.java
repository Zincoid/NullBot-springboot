package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.entity.BotInputer;
import org.bot.nullbot.entity.BotPageSelector;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ChatHistory", "聊天历史"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryCommand implements Command {

    private final DeepSeekClient deepSeekClient;
    private final BotNextInputer botNextInputer;

    private static final int PAGE_SIZE = 10;  // 查询单页大小
    private static final int WAIT_TIMEOUT = 60;  // 等待超时时间 (单位: Second)

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        List<ChatMessage> history = deepSeekClient.getGroupHistory(groupId, userId);
        if (history == null || history.isEmpty())
            throw new NullBotMsgException("[聊天历史] ⚠️无对话历史");

        BotPageSelector<ChatMessage, String> pager = new BotPageSelector.Builder<>(
                bot, groupId, "聊天历史", true,
                history,
                history.stream()
                        .map(msg ->
                                "user".equals(msg.getRole()) ?
                                        "%s(%s): %s".formatted(
                                                msg.getUserName(),
                                                msg.getUserId(),
                                                msg.getContent()
                                        ) :
                                        "Null: %s".formatted(msg.getContent())
                        ).toList(),
                this::sendInfo
        ).userId(userId).current(Integer.MAX_VALUE).build();

        // pager.init();
        // while (pager.input(botNextInputer, WAIT_TIMEOUT)) {
        //     log.info("\t\t\t\t├─[ChatHistory] 已操作分页器");
        // }
        BotInputer in = new BotInputer(BniMode.PS, userId).timeout(WAIT_TIMEOUT);
        pager.start(in);
    }

    private Void sendInfo(Bot bot, Long groupId, ChatMessage message) {
        bot.sendGroupMsg(groupId, message.toString(), true);
        log.info("\t\t\t\t├─[ChatHistory] 已获取记录 - {}", message.getMessageId());
        return null;
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

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
