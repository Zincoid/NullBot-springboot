package org.bot.nullbot.command.debug;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

@CommandMapping({"PlusExp", "加经验"})
@Component
@Slf4j
@RequiredArgsConstructor
public class PlusExpCommand implements Command
{
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().size() < 2) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[加经验] ❌参数不足", false);
                log.info("\t\t\t\t├─[PlusExp] 参数不足");
                return;
            }

            long userId;
            int exp;
            try {
                userId = Long.parseLong(event.getCommandParameters().get(0));
                exp = Integer.parseInt(event.getCommandParameters().get(1));
            } catch (NumberFormatException e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[加经验] ❌参数格式错误", false);
                log.info("\t\t\t\t├─[PlusExp] 参数格式错误");
                return;
            }

            if (!userService.existUser(userId)) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[加经验] ❌用户不存在", false);
                log.info("\t\t\t\t├─[PlusExp] 用户不存在");
                return;
            }

            int i = userService.plusExperience(userId, exp);
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            StringBuilder sb = new StringBuilder(userName + " 获得 " + exp + "Exp！");
            while (i > 0) {
                sb.append("\n- LEVEL UP！");
                i--;
            }
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), sb.toString(), false);
            log.info("\t\t\t\t├─[PlusExp] 已给予经验 - {} -> {} Exp", userId, exp);
        }else
            log.info("\t\t\t\t├─[PlusExp] 未设计 - 非群消息事件响应方式");
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
                中文命令: 加经验""", getAccess()
        );
    }
}
