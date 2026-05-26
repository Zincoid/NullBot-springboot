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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@CommandMapping({"Pucci", "普奇"})
@Component
@RequiredArgsConstructor
public class PucciCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final ResourceLoader resourceLoader;
    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) throws Exception {
        Long groupId = event.getGroupId();
        String tempFilePath = fileStorageProperties.getTempPath();

        Path htmlPath = resourceLoader
                .getCached("static/html/pucci.html", tempFilePath + "/html");
        Path bgPath = resourceLoader
                .getCached("static/image/pucci.png", tempFilePath + "/image");
        Map<String, String> variables = new HashMap<>();
        Map<String, String> images = new HashMap<>();

        variables.put("text1", "普奇！！回答我！");
        variables.put("text2", "为什么你要加速时间！！");
        variables.put("text3", params.nextString());
        images.put("background", bgPath.toAbsolutePath().toString());

        String html = HtmlTemplateUtil.loadTemplate(htmlPath.toString());
        html = HtmlTemplateUtil.replaceVariables(html, variables);
        html = HtmlTemplateUtil.replaceImages(html, images);
        String base64 = htmlRenderer.renderElement(html, "#wrap");

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
