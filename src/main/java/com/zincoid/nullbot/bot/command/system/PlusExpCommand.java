package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.service.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"PlusExp", "加经验"})
@Component
@RequiredArgsConstructor
public class PlusExpCommand implements Command {

    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        long userId = params.nextLong();
        int exp = params.nextInt();
        if (!userService.exist(userId))
            throw new NullBotException("[加经验] ❌用户不存在");
        int i = userService.plusExperience(userId, exp);
        String userName = event.getSender().getNickname();
        StringBuilder sb = new StringBuilder(userName + " 获得 " + exp + "Exp！");
        while (i > 0) {
            sb.append("\n- LEVEL UP！");
            i--;
        }
        bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        log.info("☑ [PlusExp] 经验已给予 - {} -> {} Exp", userId, exp);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ PlusExp 命令
                功能: 给予经验值
                限权: %d 级
                格式: PlusExp [QQ号] [经验值]
                别名: 加经验""", getAccess()
        );
    }
}
