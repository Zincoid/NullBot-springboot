package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.FunctionManager;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"FuncSet", "全局设置"})
@Component
@RequiredArgsConstructor
public class FuncSetCommand implements Command {

    private final FunctionManager functionManager;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String option = args.nextString();
        if ("-view".equals(option)) {
            String status = functionManager.getStatus();
            bot.sendGroupMsg(event.getGroupId(), "[全局设置] ℹ️已获取！\n" + status, false);
            log.info("☑ [FuncSet] 已获取全局设置");
            return;
        }
        if ("-change".equals(option)) {
            String func = args.nextString();
            boolean enabled = functionManager.switchEnabled(func);
            bot.sendGroupMsg(event.getGroupId(), """
                    [全局设置] \uD83D\uDD04已切换: %s""".formatted(enabled ? "ON" : "OFF"), false);
            log.info("☑ [FuncSet] 已更改全局设置 {} -> {}", func, enabled ? "ON" : "OFF");
            return;
        }
        throw new BotWarnException("无此操作");
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
                标志: AIAutoReply/ImgCollect/MsgCollect/KeywordAct/PokeDetect/RecallDetect/PrivateCmd/BottleAutoThrow
                别名: 功能控制""", getAccess()
        );
    }
}
