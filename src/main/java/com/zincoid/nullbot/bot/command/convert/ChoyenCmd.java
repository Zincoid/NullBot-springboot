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
@CmdMapping({"Choyen", "5000兆"})
@Component
@RequiredArgsConstructor
public class ChoyenCmd implements Cmd {

    private final RenderingService renderingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) throws Exception {
        Long groupId = event.getGroupId();
        String base64 = renderingService.choyen(args.next(), args.next());
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("☑ [Choyen] 图像处理已完成");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Choyen 命令
                功能: 5000兆円梗图生成
                限权: %d 级
                格式: Choyen [文本1] [文本2]
                别名: 5000兆""", getAccess()
        );
    }
}
