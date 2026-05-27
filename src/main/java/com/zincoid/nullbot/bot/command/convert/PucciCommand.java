package com.zincoid.nullbot.bot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.render.HtmlRenderer;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Pucci", "普奇"})
@Component
@RequiredArgsConstructor
public class PucciCommand implements Command {

    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) throws Exception {
        Long groupId = event.getGroupId();

        String base64 = htmlRenderer.load("static/html/pucci.html")
                .set("text1", "普奇！！回答我！")
                .set("text2", "为什么你要加速时间！！")
                .set("text3", args.nextString())
                .resource("background", "static/image/pucci.png")
                .render("#wrap");

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
