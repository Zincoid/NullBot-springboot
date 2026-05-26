package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@CommandMapping({"PokeReact"})
@Component
public class PokeReactCommand implements Command {

    private final QQAiClient qqAiClient;

    public PokeReactCommand(@Lazy QQAiClient qqAiClient) {
        this.qqAiClient = qqAiClient;
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, CommandArgs params) {
        if (!Objects.equals(event.getTargetId(), event.getSelfId())) return;  // 仅检测戳 Bot 自身
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        String response;
        QQMessage message;
        if (groupId != null) message = QQMessage.user("揉了你一下").with(groupId, userId, userName);
        else message = QQMessage.user("揉了你一下").with(userId, userName);
        response = qqAiClient.chat(message);
        log.info("☑ [PokeReact] {}戳戳已回复: {}", groupId != null ? "群聊" : "私聊", response);
    }
}
