package com.zincoid.nullbot.bot.command.assist;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.service.render.CapturingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CmdMapping({"PRTS", "PRTS查询"})
@Component
@RequiredArgsConstructor
public class PRTSCmd implements Cmd {

    private final CapturingService capturingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String keyword;
        String base64;
        if (List.of("语音", "档案", "密录", "悖论").contains(args.get(0))) {
            keyword = args.rest(1);
            base64 = capturingService.prtsOpt(args.get(0), keyword);
        } else {
            keyword = args.rest(0);
            base64 = capturingService.prtsAny(keyword);
        }
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [PRTS] 资料已查询 - Keyword: {}", keyword);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ PRTS 命令
                功能: 明日方舟PRTS查询
                限权: %d 级
                格式:
                1. PRTS [查询内容]
                2. PRTS [可选: 条目] [干员名]
                条目: 语音/档案/密录/悖论
                别名: PRTS查询""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ PRTS 命令
                功能: 通过PRTS网站查询明日方舟相关信息
                格式: PRTS [查询内容]
                示例: PRTS 莱伊""";
    }
}
