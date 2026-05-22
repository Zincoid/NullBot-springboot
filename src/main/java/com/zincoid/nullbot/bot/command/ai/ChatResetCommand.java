package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.enums.ChatScope;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ChatReset", "重置聊天"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatResetCommand implements Command {

    private final QQAiClient qqAiClient;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        ChatScope chatScope = qqAiClient.reset(groupId, userId);
        Long id = switch (chatScope) {
            case Group, Monitor ->  groupId;
            case Personal -> userId;
        };
        bot.sendGroupMsg(groupId, """
                    [重置聊天] ♻️历史已重置
                    - Chat Scope: %s
                    - Target ID: %s""".formatted(chatScope, id), false);
        log.info("\t\t\t\t├─[ChatReset] 历史已重置 - {}: {}", chatScope, id);
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
