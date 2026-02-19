package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@CommandMapping({"PokeReact"})
@Component
@RequiredArgsConstructor
@Slf4j
public class PokeReactCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final ChatStorage chatStorage;

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
            if(!Objects.equals(event.getTargetId(), event.getSelfId())) return;  // 仅检测戳Bot

            Long groupId = event.getGroupId();
            Long userId = event.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();

            if (chatStorage.isUserBanned(userId)) {
                LocalDateTime until = chatStorage.getUserBannedUntil(userId);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedUntil = until != null ? until.format(formatter) : "";
                bot.sendGroupMsg(groupId, "[AI] ⛔️你已被停用至！\n" + formattedUntil, false);
                log.info("\t\t\t\t├─[PokeReact] 已被停用至 - {}", until);
                return;
            }

            String response;
            try {
                response = deepSeekClient.chat(null, groupId, userId, userName, "揉了你一下", bot, event);
            } catch (Exception e) {
                throw new NullBotMsgException("[AI] ❌出错:\n" + e.getMessage());
            }

            log.info("\t\t\t\t├─[PokeReact] 已回复戳一戳: {}", response.replaceAll("\\R", " "));
    }

    // 特殊命令 无帮助
}
