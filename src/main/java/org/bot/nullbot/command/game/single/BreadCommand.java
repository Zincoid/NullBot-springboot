package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"Bread", "面包"})
@Component
@RequiredArgsConstructor
@Slf4j
public class BreadCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {

        }else
            log.info("\t\t\t\t├─[Bread] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Bread 命令
                功能: 面包小游戏
                限权: %d 级
                格式: Bread
                中文命令: 面包""", getAccess()
        );
    }
}
