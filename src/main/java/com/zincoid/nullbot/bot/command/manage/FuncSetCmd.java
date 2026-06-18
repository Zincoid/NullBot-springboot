package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.control.FunctionManager;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"FuncSet", "全局设置"})
@Component
@RequiredArgsConstructor
public class FuncSetCmd implements Cmd {

    private final FunctionManager functionManager;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        if (args.hasOpt("view", "v")) {
            String status = functionManager.getStatus();
            bot.sendGroupMsg(event.getGroupId(), status, false);
            log.info("☑ [FuncSet] 全局设置已获取");
            return;
        }
        if (args.hasOpt("change", "c")) {
            String func = args.next();
            boolean enabled = functionManager.switchEnabled(func);
            bot.sendGroupMsg(event.getGroupId(), "🔄已切换: %s".formatted(enabled ? "ON" : "OFF"), false);
            log.info("☑ [FuncSet] 全局设置已更改 - {} -> {}", func, enabled);
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
                用法: FuncSet [选项] [标志]

                选项:
                  -v, --view     获取全局设置
                  -c, --change   更改启用状态

                标志:
                  AIAutoReply
                  ImgCollect
                  MsgCollect
                  KeywordAct
                  PokeDetect
                  RecallDetect
                  PrivateCmd
                  BottleAutoThrow
                
                别名: 功能控制""", getAccess()
        );
    }
}
