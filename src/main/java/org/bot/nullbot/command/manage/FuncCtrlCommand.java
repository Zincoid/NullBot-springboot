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

@CommandMapping({"FuncCtrl", "功能控制"})
@Component
@RequiredArgsConstructor
@Slf4j
public class FuncCtrlCommand implements Command
{
    private final FunctionManager functionManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (!event.getCommandParameters().isEmpty()){
                String function = event.getCommandParameters().getFirst();
                Boolean isEnabled = functionManager.switchEnabled(function);
                if (isEnabled != null){
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[功能控制] \uD83D\uDD04状态已切换: " + (isEnabled ? "ON" : "OFF"), false);
                    log.info("\t\t\t\t├─[FuncCtrl] 已切换 {} 功能状态 -> {}", function, isEnabled ? "ON" : "OFF");
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[功能控制] ❌无此功能标志", false);
                    log.info("\t\t\t\t├─[FuncCtrl] 无此功能标志 - {}", function);
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[功能控制] ❌参数不足", false);
                log.info("\t\t\t\t├─[FuncCtrl] 参数不足");
            }
        }else
            log.info("\t\t\t\t├─[FuncCtrl] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ FuncCtrl 命令
                功能: 转换全局功能启用状态
                限权: %d 级
                格式: FuncCtrl [功能控制标志]
                标志: imageCollect/keywordDetect/pokeDetect/messageCollect/recallDetect
                中文命令: 功能控制""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ FuncCtrl 命令
                功能: 转换全局功能启用状态
                限权: %d 级
                格式: FuncCtrl [功能控制标志]
                标志: imageCollect/keywordDetect/pokeDetect/messageCollect/recallDetect
                注意: 只有Zincoid可以调用！！！""", getAccess()
        );
    }
}
