package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@CommandMapping({"PokeReact"})
@Component
@Slf4j
public class PokeReactCommand implements Command {

    private final QQAiClient qqAiClient;

    public PokeReactCommand(@Lazy QQAiClient qqAiClient) {
        this.qqAiClient = qqAiClient;
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
        if (!Objects.equals(event.getTargetId(), event.getSelfId())) return;  // 仅检测戳Bot
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        String response;
        try {
            if (groupId != null) {
                QQMessage message = QQMessage.user("揉了你一下").with(groupId, userId, userName);
                response = qqAiClient.chat(message);
            } else {
                QQMessage message = QQMessage.user("揉了你一下").with(userId, userName);
                response = qqAiClient.chat(message);
            }
        } catch (Exception e) {
            throw new NullBotMsgException("[AI] ❌出错: " + e.getMessage());
        }
        log.info("├─[PokeReact] 已回复{}戳戳: {}", groupId != null ? "群聊" : "私聊", response);
    }

    // 特殊命令 无帮助
}
