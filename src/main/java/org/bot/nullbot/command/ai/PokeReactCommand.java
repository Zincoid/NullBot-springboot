package org.bot.nullbot.command.ai;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.ai.DeepSeekClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@CommandMapping({"PokeReact"})
@Component
@RequiredArgsConstructor
public class PokeReactCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(PokeReactCommand.class);
    private final DeepSeekClient deepSeekClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            Long userId = pokeNoticeEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            Long groupId = pokeNoticeEvent.getGroupId();
            if(Objects.equals(pokeNoticeEvent.getTargetId(), pokeNoticeEvent.getSelfId())){
                String response = deepSeekClient.chat(groupId, userId, userName, "揉了你一下");
                bot.sendGroupMsg(groupId, response, false);
                logger.info("\t\t\t\t├─[AI.PokeReact] 已回复戳一戳: {}", response);
            }
        }else
            logger.info("\t\t\t\t├─[AI.PokeReact] 未设计 - 非戳一戳消息事件响应方式");
    }

    // 限权: 0
}
