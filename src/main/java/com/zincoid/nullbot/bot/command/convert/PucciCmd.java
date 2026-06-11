package com.zincoid.nullbot.bot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.service.render.RenderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Pucci", "普奇"})
@Component
@RequiredArgsConstructor
public class PucciCmd implements Cmd {

    private final RenderingService renderingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) throws Exception {
        Long groupId = event.getGroupId();
        String base64 = renderingService.pucci(args.nextFullString());
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("☑ [Pucci] 图像处理已完成");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Pucci 命令
                功能: 普奇神父梗图生成
                限权: %d 级
                格式: Pucci [文本]
                别名: 普奇""", getAccess()
        );
    }
}
