package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@CommandMapping({"PokeReact"})
@Component
@RequiredArgsConstructor
@Slf4j
public class PokeReactCommand implements Command
{
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
            if(!Objects.equals(event.getTargetId(), event.getSelfId())) return;  // 仅检测戳Bot
            String response;
            try {
                response = deepSeekClient.chatGroup(
                        null,
                        event.getGroupId(),
                        event.getUserId(),
                        bot.getStrangerInfo(event.getUserId(), true).getData().getNickname(),
                        "揉了你一下",
                        bot,
                        event
                );
            } catch (Exception e) {
                throw new NullBotMsgException("[AI] ❌出错:\n" + e.getMessage());
            }
            log.info("\t\t\t\t├─[PokeReact] 已回复戳一戳: {}", response.replaceAll("\\R", " "));
    }

    // 特殊命令 无帮助
}
