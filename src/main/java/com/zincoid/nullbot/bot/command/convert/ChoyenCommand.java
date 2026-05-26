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
import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.util.HtmlTemplateUtil;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

@Slf4j
@CommandMapping({"Choyen", "5000兆"})
@Component
@RequiredArgsConstructor
public class ChoyenCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final ResourceLoader resourceLoader;
    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) throws Exception {
        Long groupId = event.getGroupId();
        String tempFilePath = fileStorageProperties.getTempPath();

        Path htmlPath = resourceLoader
                .getCached("static/html/5000choyen.html", tempFilePath + "/html");
        Map<String, String> variables = new HashMap<>();
        variables.put("topText", args.nextString());
        variables.put("bottomText", args.nextString());

        String html = HtmlTemplateUtil.loadTemplate(htmlPath.toString());
        String base64 = htmlRenderer.renderElement(
                HtmlTemplateUtil.replaceVariables(html, variables),
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
