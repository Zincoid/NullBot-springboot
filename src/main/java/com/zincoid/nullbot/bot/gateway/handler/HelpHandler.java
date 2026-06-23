package com.zincoid.nullbot.bot.gateway.handler;

import com.mikuac.shiro.core.Bot;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.gateway.processor.CmdHandlerChain;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Order(1)
@Component
public class HelpHandler implements Handler {

    @Override
    public void handle(Bot bot, Cmd cmd, CmdEvent<?> event, CmdHandlerChain chain) throws Exception {
        List<String> params = event.getCmdParams();
        if (!params.isEmpty() && ("--help".equals(params.getFirst()) || "-H".equals(params.getFirst()))) {
            EventScope scope = event.getEventScope();
            if (scope == EventScope.GROUP) {
                log.info("├─[HelpHandler] 群聊帮助已输出");
                bot.sendGroupMsg(event.getGroupId(), cmd.getHelp(), false);
            } else if (scope == EventScope.PRIVATE) {
                log.info("├─[HelpHandler] 私聊帮助不可用");
                bot.sendPrivateMsg(event.getUserId(), "⚠️私聊暂无帮助", false);
            } else {
                log.info("├─[HelpHandler] 未知事件无帮助");
            }
            return;
        }
        log.info("├─[HelpHandler] 非帮助命令");
        chain.doHandle(bot, event, cmd);
    }
}
