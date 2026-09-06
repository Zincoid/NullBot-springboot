package com.zincoid.nullbot.bot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.service.render.RenderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Generate", "图像生成", "生成"})
@Component
@RequiredArgsConstructor
public class GenerateCmd implements Cmd {

    private final RenderingService renderingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) throws Exception {
        Long groupId = event.getGroupId();
        String method = args.next();
        String base64 = switch (method) {
            case "choyen", "5000兆" -> renderingService.choyen(args.next(), args.next());
            case "pucci", "普奇" -> renderingService.pucci(args.rest());
            default -> throw new BotWarnException("无此操作");
        };
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("☑ [Generate] 图像生成已完成");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Generate 命令
                功能: 图像生成
                限权: %d 级
                格式: Generate [方式] [文本...]
                方式:
                1. 5000兆円/choyen (两段文本)
                2. 普奇/pucci (一段文本)
                别名: 图像生成/生成""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Generate 命令
                功能: 图像生成
                格式: Generate [方式] [文本...]
                方式:
                1. 5000兆円/choyen (两段文本)
                2. 普奇/pucci (一段文本)
                示例: Generate choyen 上面的文字 下面的文字""";
    }
}
