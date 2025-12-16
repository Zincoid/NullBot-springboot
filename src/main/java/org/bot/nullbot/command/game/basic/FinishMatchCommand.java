package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"FinishMatch", "终止对局"})
@Component
@Slf4j
@RequiredArgsConstructor
public class FinishMatchCommand implements Command
{
    private final Matcher matcher;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getUserId();
            String result = matcher.finishMatch(userId);
            if (result != null) {
                bot.sendGroupMsg(groupId, result, false);
                log.info("\t\t\t\t├─[FinishMatch] 结束对局结果 - {}", result);
            }else
                log.info("\t\t\t\t├─[FinishMatch] 结束对局结果 - 已响应");
        }else
            log.info("\t\t\t\t├─[FinishMatch] 未设计 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "◉ FinishMatch 命令\n功能: 强制终止自己正在进行的对局\n限权: " + getAccess() + "\n格式: FinishMatch\n中文命令: 终止对局";
    }
}
