package org.bot.nullbot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"PlusExp", "加经验"})
@Component
@Slf4j
@RequiredArgsConstructor
public class PlusExpCommand implements Command
{
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.size() < 2)
            throw new NullBotMsgException("[加经验] ❌参数不足");

        long userId;
        int exp;
        try {
            userId = Long.parseLong(params.get(0));
            exp = Integer.parseInt(params.get(1));
        } catch (NumberFormatException e) {
            throw new NullBotMsgException("[加经验] ❌参数格式错误");
        }

        if (!userService.existUser(userId))
            throw new NullBotMsgException("[加经验] ❌用户不存在");

        int i = userService.plusExperience(userId, exp);
        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        StringBuilder sb = new StringBuilder(userName + " 获得 " + exp + "Exp！");
        while (i > 0) {
            sb.append("\n- LEVEL UP！");
            i--;
        }
        bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        log.info("\t\t\t\t├─[PlusExp] 已给予经验 - {} -> {} Exp", userId, exp);
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
