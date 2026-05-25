package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ChatReset", "重置聊天"})
@Component
@Slf4j
public class ChatResetCommand implements Command {

    private final QQAiClient qqAiClient;

    public ChatResetCommand(@Lazy QQAiClient qqAiClient) {
        this.qqAiClient = qqAiClient;
    }

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String chatId = BotCtxUtil.getChatId();
        qqAiClient.clear(chatId);
        bot.sendGroupMsg(event.getGroupId(), """
                    [重置聊天] ♻️记忆存储已重置
                    - ChatID: %s""".formatted(chatId), false);
        log.info("├─[ChatReset] 历史已重置 - ChatID: {}", chatId);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ChatReset 命令
                功能: 重置AI聊天记忆
                限权: %d 级
                格式: ChatReset
                别名: 重置聊天""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ ChatReset 命令
                功能: 重置AI聊天记忆
                格式: ChatReset
                注意: 该指令无法撤回已发送群聊消息""";
    }
}
