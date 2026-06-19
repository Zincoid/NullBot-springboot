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

@Slf4j
@CmdMapping({"Essence", "基质规划", "基质"})
@Component
@RequiredArgsConstructor
public class EssenceCmd implements Cmd {

    private final CapturingService capturingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String weapon = args.rest();
        String base64 = capturingService.essence(weapon);
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [Essence] 基质已查询 - Weapon: {}", weapon);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Essence 命令
                功能: 终末地基质刷取推荐
                限权: %d 级
                格式: Essence [武器名]
                别名: 基质规划/基质""", getAccess()
        );
    }
}
