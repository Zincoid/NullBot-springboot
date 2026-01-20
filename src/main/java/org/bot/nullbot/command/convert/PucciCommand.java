package org.bot.nullbot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.render.HtmlRenderer;
import org.bot.nullbot.component.resource.ResourceLoader;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.HtmlTemplateUtil;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandMapping({"Pucci", "普奇"})
@Component
@Slf4j
@RequiredArgsConstructor
public class PucciCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    private final ResourceLoader resourceLoader;
    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            List<String> params = event.getCommandParameters();
            if (params.isEmpty()) throw new NullBotMsgException("[普奇] ❌参数不足");

            String base64;
            String tempFilePath = fileStorageProperties.getTempPath();
            try {
                Path htmlPath = resourceLoader.getCached("static/html/pucci.html", tempFilePath + "/html");
                Map<String, String> variables = new HashMap<>();
                variables.put("text1", "普奇！！回答我！");
                variables.put("text2", "为什么你要加速时间！！");
                variables.put("text3", params.getFirst());
                String html = HtmlTemplateUtil.loadTemplate(htmlPath.toString());
                html = HtmlTemplateUtil.replaceVariables(html, variables);
                base64 = htmlRenderer.renderElement(html, "#wrap");
            } catch (Exception e) {
                throw new NullBotMsgException("[普奇] ❌处理时出错: " + e.getMessage());
            }
            String response = MsgUtils.builder().img("base64://" + base64).build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("\t\t\t\t├─[Pucci] 处理完成 - {} {}", params.get(0), params.get(1));
        }else
            throw new NullBotLogException("[普奇] ❌未设计 - 非群消息事件响应方式");
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
