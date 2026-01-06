package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Bread", "面包"})
@Component
@RequiredArgsConstructor
@Slf4j
public class BreadCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();

            if (params.isEmpty()) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[面包] ❌参数不足", false);
                log.info("\t\t\t\t├─[Bread] 参数不足");
                return;
            }

            if("-buy".equals(params.getFirst())){

                return;
            }

            if("-eat".equals(params.getFirst())){

                return;
            }

            if("-rob".equals(params.getFirst())){

                return;
            }

            if("-gift".equals(params.getFirst())){

                return;
            }

            if("-look".equals(params.getFirst())){

                return;
            }

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
