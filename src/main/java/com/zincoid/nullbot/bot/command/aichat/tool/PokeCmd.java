package com.zincoid.nullbot.bot.command.aichat.tool;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"1a0d3829"})
@Component
public class PokeCmd implements Cmd {

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        poke(bot, event.getGroupId(), args.nextLong(), false);
    }
    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        poke(bot, event.getUserId(), args.nextLong(), true);
    }
    @Override
    public void run(Bot bot, PokeNoticeEvent event, CmdArgs args) {
        if (event.getGroupId() != null) {
            poke(bot, event.getGroupId(), args.nextLong(), false);
        } else {
            poke(bot, event.getUserId(), args.nextLong(), true);
        }
    }

    private void poke(Bot bot, Long resourceId, Long targetId, boolean isPrivate) {
        if (isPrivate) bot.sendFriendPoke(resourceId, targetId);
        else bot.sendGroupPoke(resourceId, targetId);
        log.info("☑ [Poke] 戳戳已发送 -> {}", targetId);
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
