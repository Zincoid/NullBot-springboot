package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"1a0d3829"})  // 加密 仅供AI调用
@Component
public class PokeCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        poke(bot, event.getGroupId(), params.nextLong(), false);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs params) {
        poke(bot, event.getUserId(), params.nextLong(), true);
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, CommandArgs params) {
        if (event.getGroupId() != null) {
            poke(bot, event.getGroupId(), params.nextLong(), false);
        } else {
            poke(bot, event.getUserId(), params.nextLong(), true);
        }
    }

    private void poke(Bot bot, Long resourceId, Long targetId, boolean isPrivate) {
        if (isPrivate) bot.sendFriendPoke(resourceId, targetId);
        else bot.sendGroupPoke(resourceId, targetId);
        log.info("☑ [Poke] 已戳戳: {}", targetId);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ 1a0d3829 命令
                功能: 戳一戳某人
                格式: 1a0d3829 [QQ号]
                示例: 1a0d3829 1234567890""";
    }
}
