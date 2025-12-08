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

@CommandMapping({"FunctionControl"})
@Component
@RequiredArgsConstructor
public class FunctionControlCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(FunctionControlCommand.class);
    private final FunctionManager functionManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (!event.getCommandParameters().isEmpty()){
                String function = event.getCommandParameters().get(0);
                Boolean isEnabled = functionManager.switchEnabled(function);
                if (isEnabled != null){
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Function.Control] 已切换功能状态: " + (isEnabled ? "启用" : "未启用"), false);
                    logger.info("\t\t\t\t├─[Function.Control] 已切换功能状态 - {}", isEnabled ? "启用" : "未启用");
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Function.Control] 无此功能", false);
                    logger.info("\t\t\t\t├─[Function.Control] 无此功能 - {}", function);
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Function.Control] 参数不足", false);
                logger.info("\t\t\t\t├─[Function.Control] 参数不足");
            }
        }else
            logger.info("\t\t\t\t├─[Function.Control] 无 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 2;
    }

    @Override
    public String getHelp() {
        return "/FunctionControl 命令\n功能: 转换功能启用状态\n限权: " + getAccess() + "\n格式: /FunctionControl [功能控制标志]";
    }
}
