package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ChatHistory", "聊天历史"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryCommand implements Command
{
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

        int total = history.size();
        int pages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        int current = pages;

        if (!params.isEmpty()) {
            try {
                current = Math.max(1, Math.min(pages, Integer.parseInt(params.getFirst())));
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[聊天历史] ❌页码格式错误");
            }
        }

        String operation = "INIT";
        while (!"END".equals(operation)) {
            switch (operation) {
                case "UP" -> current--;
                case "DOWN" -> current++;
            }
            int fromIndex = (current - 1) * PAGE_SIZE;
            int toIndex = Math.min(fromIndex + PAGE_SIZE, total);
            List<ChatMessage> historyPage = history.subList(fromIndex, toIndex);
            List<String> contentPage = historyPage.stream()
                    .map(msg ->
                            "user".equals(msg.getRole()) ?
                                    "%s(%s): %s".formatted(msg.getUserName(), msg.getUserId(), msg.getContent()) :
                                    "Null: %s".formatted(msg.getContent())
                    )
                    .toList();
            String content = String.join("\n", contentPage);
            String footer = """
                    [第%s页 / 共%s页 (每页%s条)]
                    注: 发送 UP/DOWN/END 操作""".formatted(current, pages, PAGE_SIZE);
            bot.sendGroupMsg(groupId, "[聊天历史] ✅共%s条记录\n%s\n%s".formatted(total, content, footer), false);
            log.info("\t\t\t\t├─[ChatHistory] 已获取聊天历史 - {}/{}", current, pages);

            List<Pair<Long, String>> inputs = botNextInputer.request(BniMode.PS, userId, WAIT_TIMEOUT, "UP|DOWN|END");
            operation = inputs.isEmpty() ? "END" : inputs.getFirst().getRight();
        }

        bot.sendGroupMsg(groupId, "[聊天历史] ⛔️查询结束", false);
        log.info("\t\t\t\t├─[ChatHistory] 用户 {} 查询结束", userId);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatHistory 命令
                功能: 获取AI聊天历史
                限权: %d 级
                格式: ChatHistory
                别名: 聊天历史""", getAccess()
        );
    }
}
