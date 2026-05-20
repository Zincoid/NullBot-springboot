package com.zincoid.nullbot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.annotation.CommandMapping;
import com.zincoid.nullbot.command.Command;
import com.zincoid.nullbot.component.render.HtmlRenderer;
import com.zincoid.nullbot.component.resource.ResourceLoader;
import com.zincoid.nullbot.config.prop.FileStorageProperties;
import com.zincoid.nullbot.exception.NullBotMsgException;
import com.zincoid.nullbot.util.HtmlTemplateUtil;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

@CommandMapping({"Choyen", "5000兆"})
@Component
@Slf4j
@RequiredArgsConstructor
public class ChoyenCommand implements Command {

    private final FileStorageProperties fileStorageProperties;

    private final ResourceLoader resourceLoader;
    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        if (params.size() < 2)
            throw new NullBotMsgException("[5000兆] ❌需要两个参数");

        String base64;
        String tempFilePath = fileStorageProperties.getTempPath();
        try {
            Path htmlPath = resourceLoader.getCached("static/html/5000choyen.html", tempFilePath + "/html");
            Map<String, String> variables = new HashMap<>();
            variables.put("topText", params.get(0));
            variables.put("bottomText", params.get(1));
            String html = HtmlTemplateUtil.loadTemplate(htmlPath.toString());
            html = HtmlTemplateUtil.replaceVariables(html, variables);
            base64 = htmlRenderer.renderElement(html, "#templateContainer");

        } catch (Exception e) {
            throw new NullBotMsgException("[5000兆] ❌处理时出错: " + e.getMessage());
        }
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("\t\t\t\t├─[Choyen] 处理完成 - {} {}", params.get(0), params.get(1));
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
