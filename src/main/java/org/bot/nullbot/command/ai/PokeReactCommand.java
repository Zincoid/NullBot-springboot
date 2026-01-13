package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.exception.NullBotLogException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@CommandMapping({"PokeReact"})
@Component
@RequiredArgsConstructor
@Slf4j
public class PokeReactCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final ChatStorage chatStorage;
    private final SettingManager settingManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            if(!Objects.equals(pokeNoticeEvent.getTargetId(), pokeNoticeEvent.getSelfId())) return;  // 仅检测戳Bot

            Long userId = pokeNoticeEvent.getUserId();
            Long groupId = pokeNoticeEvent.getGroupId();

            if(chatStorage.isUserBanned(userId)) {
                LocalDateTime until = chatStorage.getUserBannedUntil(userId);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
                String formattedUntil = until != null ? until.format(formatter) : "";
                bot.sendGroupMsg(groupId, "[AI] ⚠️你已被停用至！\n" + formattedUntil, false);
                log.info("\t\t\t\t├─[PokeReact] 已被停用至 - {}", until);
                return;
            }

            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            String response = deepSeekClient.chat(
                    null, groupId, userId, userName, "揉了你一下", bot, event,
                    settingManager.getChatOption(groupId)
            );

            log.info("\t\t\t\t├─[PokeReact] 已回复戳一戳: {}", response.replaceAll("\\R", " "));
        }else
            throw new NullBotLogException("[戳戳反馈] ❌未设计 - 非戳一戳事件响应方式");
    }

    // 特殊命令 无帮助
}
