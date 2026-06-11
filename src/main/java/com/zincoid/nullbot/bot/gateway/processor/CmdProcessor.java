package com.zincoid.nullbot.bot.gateway.processor;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.gateway.handler.Handler;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmdProcessor {

    private final CmdRegistry registry;
    private final List<Handler> handlers;

    /** 处理用户指令 */
    public void processQQ(Bot bot, CmdEvent<?> event) throws Exception {
        doProcess("▶", "■", "QQ", bot, event);
    }

    /** 处理内部指令 */
    @EventListener
    public void processInner(CmdEvent<?> event) throws Exception {
        doProcess("▷", "□", "Inner", BotCtx.getBot(), event);
    }

    /** 处理测试指令 */
    public void processTest(CmdEvent<?> event) throws Exception {
        doProcess("▶", "■", "Test", null, event);
    }

    // =========== 工具 ===========

    private void doProcess(String start, String end, String tag, Bot bot, CmdEvent<?> event) throws Exception {
        Cmd cmd = registry.getCmd(event.getCmdType());
        if (cmd != null) {
            log.info("{} [CmdProcessor::{}] {} 指令处理中...", start, tag, event.getCmdType());
            chainProcess(bot, event, cmd);
            log.info("{} [CmdProcessor::{}] {} 指令已处理", end, tag, event.getCmdType());
        } else {
            log.info("{} [CmdProcessor::{}] {} 指令不存在", end, tag, event.getCmdType());
        }
    }

    public void chainProcess(Bot bot, CmdEvent<?> event, Cmd cmd) throws Exception {
        CmdHandlerChain chain = new CmdHandlerChain(handlers);
        chain.doHandle(bot, event, cmd);
    }
}
