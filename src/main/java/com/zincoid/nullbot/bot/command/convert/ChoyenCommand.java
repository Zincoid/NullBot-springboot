package com.zincoid.nullbot.bot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.core.util.HtmlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.render.HtmlRenderer;
import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

@Slf4j
@CommandMapping({"Choyen", "5000兆"})
@Component
@RequiredArgsConstructor
public class ChoyenCommand implements Command {

    private final ResourceLoader resourceLoader;
    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) throws Exception {
        Long groupId = event.getGroupId();

        Path htmlPath = resourceLoader.getCache("static/html/5000choyen.html");
        String html = HtmlUtil.loadTemplate(htmlPath.toString());

        Map<String, String> variables = Map.of(
                "topText", args.nextString(),
                "bottomText", args.nextString()
        );

        String base64 = htmlRenderer.renderElement(
                HtmlUtil.replaceVariables(html, variables),
                "#templateContainer"
        );

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
