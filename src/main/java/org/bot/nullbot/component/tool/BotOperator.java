package org.bot.nullbot.component.tool;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotOperator
{
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

    // =================== 获取方法 ===================

    public Bot getBot() { return botContainer.robots.get(botId); }

    // =================== 消息方法 ===================

    public void sendAllGroupMsg(String message) {
        Bot bot = getBot();
        for (GroupInfoResp group : bot.getGroupList().getData())
            bot.sendGroupMsg(group.getGroupId(), message, false);
    }

    public void sendGroupMsg(Long groupId, String message) {
        getBot().sendGroupMsg(groupId, message, false);
    }

    public void sendPrivateMsg(Long userId, String message) {
        getBot().sendPrivateMsg(userId, message, false);
    }
}
