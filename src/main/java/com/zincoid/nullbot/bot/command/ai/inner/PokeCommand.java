package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CommandMapping({"1a0d3829"})  // 加密 仅供AI调用
@Component
public class PokeCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        poke(bot, params, event.getGroupId(), false);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) {
        poke(bot, params, event.getUserId(), true);
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
        if (event.getGroupId() != null) {
            poke(bot, params, event.getGroupId(), false);
        } else {
            poke(bot, params, event.getUserId(), true);
        }
    }

    private void poke(Bot bot, List<String> params, Long targetId, boolean isPrivate) {
        if (params.isEmpty())
            throw new NullBotMsgException("[戳戳] ❌参数不足");
        long pokeId;
        try {
            pokeId = Long.parseLong(params.getFirst());
        } catch (NumberFormatException e) {
            throw new NullBotMsgException("[对称] ❌参数格式错误");
        }
        String response = MsgUtils.builder().poke(pokeId).build();
        if (isPrivate) {
            bot.sendPrivateMsg(targetId, response, false);
        } else {
            bot.sendGroupMsg(targetId, response, false);
        }
        log.info("├─[Poke] 已戳戳: {}", pokeId);
    }

    @Override
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

    @Override
    public String getHelpForAI() {
        return """
                ◉ 1a0d3829 命令
                功能: 戳一戳某人
                格式: 1a0d3829 [QQ号]
                示例: 1a0d3829 1234567890""";
    }
}
