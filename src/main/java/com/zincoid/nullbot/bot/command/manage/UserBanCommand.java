package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"UserBan", "ban", "禁言"})
@Component
@Slf4j
public class UserBanCommand implements Command {

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.size() < 2)
            throw new NullBotException("[用户禁言] ❌参数不足");
        try {
            long userId = Long.parseLong(params.get(0));
            int time = Integer.parseInt(params.get(1));
            bot.setGroupBan(event.getGroupId(), userId, time * 60);
            log.info("├─[UserBan] 已执行禁言 - {} -> {} Min", userId, time);
        } catch (NumberFormatException e) {
            throw new NullBotException("[用户禁言] ❌参数格式错误");
        }
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ UserBan 命令
                功能: 用户禁言 (时长设为0时解除禁言)
                限权: %d 级
                格式: UserBan [QQ号] [时长(分钟)]
                别名: ban/禁言""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ UserBan 命令
                功能: 用户禁言 (时长设为0时解除禁言)
                格式: UserBan [QQ号] [时长(分钟)]
                注意: 你想禁言某人时可主动调用""";
    }
}
