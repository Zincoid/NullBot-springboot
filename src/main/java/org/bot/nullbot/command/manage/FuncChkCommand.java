package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.control.FunctionManager;
import org.springframework.stereotype.Component;

@CommandMapping({"FuncChk", "功能检查"})
@Component
@RequiredArgsConstructor
@Slf4j
public class FuncChkCommand implements Command
{
    private final FunctionManager functionManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
                String status = functionManager.getStatus();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[功能检查] ℹ️已获取功能状态！" + status, false);
                log.info("\t\t\t\t├─[FuncChk] 已获取 - 功能状态列表");
            } else
                log.info("\t\t\t\t├─[FuncChk] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ FuncChk 命令
                功能: 检查功能启用状态
                限权: %d 级
                格式: FuncChk
                中文命令: 功能检查""", getAccess()
        );
    }
}
