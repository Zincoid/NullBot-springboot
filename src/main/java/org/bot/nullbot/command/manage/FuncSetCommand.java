package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.control.FunctionManager;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"FuncSet", "全局设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class FuncSetCommand implements Command
{
    private final FunctionManager functionManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.isEmpty()) throw new NullBotMsgException("[全局设置] ❌参数不足");

            Long groupId = groupMessageEvent.getGroupId();
            String option = params.get(0);
            if ("-view".equals(option)) {
                String status = functionManager.getStatus();
                bot.sendGroupMsg(groupId, "[全局设置] ℹ️已获取！\n" + status, false);
                log.info("\t\t\t\t├─[FuncSet] 已获取全局设置");
                return;
            }
            if ("-change".equals(option)) {
                if (params.size() < 2) throw new NullBotMsgException("[全局设置] ❌参数不足");
                String func = params.get(1);
                Boolean isEnabled = functionManager.switchEnabled(func);
                if (isEnabled == null) throw new NullBotMsgException("[全局设置] ❌无此选项");
                bot.sendGroupMsg(groupId, "[全局设置] \uD83D\uDD04状态已切换: " + (isEnabled ? "ON" : "OFF"), false);
                log.info("\t\t\t\t├─[FuncSet] 已更改全局设置 {} -> {}", func, isEnabled ? "ON" : "OFF");
                return;
            }
            throw new NullBotMsgException("[全局设置] ❌无此操作");
        }else
            throw new NullBotLogException("[全局设置] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ FuncSet 命令
                功能: 设置全局功能
                限权: %d 级
                格式: FuncSet [操作类型] [可选: 参数]
                操作类型和参数:
                - [-view] 获取全局设置
                - [-change] [功能标志] 更改启用状态
                标志: AIAutoReply/ImgCollect/MsgCollect/KeyDetect/PokeDetect/RecallDetect
                中文命令: 功能控制""", getAccess()
        );
    }
}
