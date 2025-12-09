package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.control.FunctionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"FunctionCheck", "功能检查"})
@Component
@RequiredArgsConstructor
public class FunctionCheckCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(FunctionCheckCommand.class);
    private final FunctionManager functionManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (!event.getCommandParameters().isEmpty()) {
                String function = event.getCommandParameters().get(0);
                String status = functionManager.getStatus();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[功能检查]\n" + status, false);
                logger.info("\t\t\t\t├─[Function.Check] 已输出 - 功能状态列表");
            } else
                logger.info("\t\t\t\t├─[Function.Check] 未设计 - 非群消息事件响应方式");
        }
    }

    @Override
    public String getHelp() {
        return "◉ FunctionCheck 命令\n功能: 检查功能启用状态\n限权: " + getAccess() + "\n格式: FunctionCheck\n中文命令: 功能检查";
    }
}
