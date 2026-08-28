package com.zincoid.nullbot.bot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.request.RequestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"SB6657", "6657", "玩机器"})
@Component
@RequiredArgsConstructor
public class SB6657Cmd implements Cmd {

    private static final String API_URL = "https://hguofichp.cn:10086/machine/getRandOne";

    private final RequestClient requestClient;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        MachineResp resp = requestClient.get(API_URL, MachineResp.class);
        if (resp == null || resp.code() != 200 || resp.data() == null) {
            log.warn("☒ [SB6657] 接口返回异常: {}", resp);
            throw new BotErrorException("接口返回异常");
        }
        bot.sendGroupMsg(event.getGroupId(), resp.data().barrage(), false);
        log.info("☑ [SB6657] 烂梗已发送 -> id: {}", resp.data().id());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SB6657 命令
                功能: 随机玩机器烂梗
                限权: %d 级
                格式: SB6657
                别名: 6657/玩机器""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ SB6657 命令
                功能: 随机玩机器烂梗
                格式: SB6657""";
    }

    private record MachineResp(Integer code, String msg, MachineData data) {}
    private record MachineData(Long id, String barrage, String cnt, String tags, String submitTime) {}
}
