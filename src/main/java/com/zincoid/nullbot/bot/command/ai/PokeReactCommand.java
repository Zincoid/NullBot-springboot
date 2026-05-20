package com.zincoid.nullbot.bot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.ai.DeepSeekClient;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@CommandMapping({"PokeReact"})
@Component
@RequiredArgsConstructor
@Slf4j
public class PokeReactCommand implements Command {

    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
        if (!Objects.equals(event.getTargetId(), event.getSelfId())) return;  // 仅检测戳Bot
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        String response;
        try {
            if (groupId != null)
                response = deepSeekClient.chatGroup(
                        null,
                        groupId,
                        userId,
                        userName,
                        "揉了你一下",
                        bot,
                        event
                );
            else
                response = deepSeekClient.chatPrivate(
                        null,
                        userId,
                        userName,
                        "揉了你一下",
                        bot,
                        event
                );
        } catch (Exception e) {
            throw new NullBotMsgException("[AI] ❌出错: " + e.getMessage());
        }
        log.info("\t\t\t\t├─[PokeReact] 已回复{}戳戳: {}", groupId != null ? "群聊" : "私聊", response.replaceAll("\\R", " "));
    }

    // 特殊命令 无帮助
}
