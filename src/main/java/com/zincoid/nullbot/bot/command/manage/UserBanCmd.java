package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"UserBan", "ban", "禁言"})
@Component
public class UserBanCmd implements Cmd {

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        long userId = args.nextLong();
        int time = args.nextInt();
        bot.setGroupBan(event.getGroupId(), userId, time * 60);
        log.info("☑ [UserBan] 禁言已执行 - {} -> {} Min", userId, time);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ UserBan 命令
                功能: 用户禁言
                限权: %d 级
                格式: UserBan [QQ号] [时长(分钟)]
                注意: 时长设为0时解除禁言
                别名: ban/禁言""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ UserBan 命令
                功能: 用户禁言
                格式: UserBan [QQ号] [时长(分钟)]
                注意:
                - 时长设为0时解除禁言
                - 你想禁言某人时可主动调用""";
    }
}
