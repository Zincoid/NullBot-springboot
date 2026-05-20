package org.bot.nullbot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.render.HtmlRenderer;
import org.bot.nullbot.component.resource.ResourceLoader;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.HtmlTemplateUtil;
import org.bot.nullbot.util.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@CommandMapping({"Symmetry", "对称"})
@Component
@Slf4j
@RequiredArgsConstructor
public class SymmetryCommand implements Command {

    private final FileStorageProperties fileStorageProperties;

    private final ResourceLoader resourceLoader;
    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        List<String> urls = new ArrayList<>();

        // 引用收集
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() == MsgTypeEnum.reply) {
            MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
            Map<String, String> imageMap = MsgParseUtil.parseRawMsgAsImgMap(replyMsg.getRawMessage());
            urls.addAll(imageMap.values());
        }

        // ID 收集 AT 收集
        if (!params.isEmpty()) {
            try {
                if (List.of("左", "右", "上", "下").contains(params.getFirst())) {
                    if (params.size() > 1) {
                        long qqNumber = Long.parseLong(params.get(1));
                        urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
                    } else {
                        List<Long> qqNumbers = MsgParseUtil.extractAtNumbers(event.getRawMessage());
                        for (Long number : qqNumbers) urls.add(ShiroUtils.getUserAvatar(number, 5));
                    }
                } else {
                    long qqNumber = Long.parseLong(params.get(0));
                    urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
                }
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[对称] ❌参数格式错误");
            }
        } else {
            List<Long> qqNumbers = MsgParseUtil.extractAtNumbers(event.getRawMessage());
            for (Long number : qqNumbers) urls.add(ShiroUtils.getUserAvatar(number, 5));
        }

        if (urls.isEmpty())
            throw new NullBotMsgException("[对称] ❌无引用图片或ID参数或At消息");

        // 开始处理
        String tempPath = fileStorageProperties.getTempPath();
        for (String url : urls) {
            String tempName = UUID.randomUUID().toString();
            String downloadedName;
            try {
                FileInfo fileInfo = DownloadUtil.downloadFile(url, tempPath, tempName, "\t\t\t\t├─ ");
                downloadedName = fileInfo.getFileName();
            } catch (Exception e) {
                throw new NullBotMsgException("[对称] ❌下载时出错: " + e.getMessage());
            }
            String imagePath = tempPath + "/" + downloadedName;
            String base64;
            try {
                Path htmlPath = resourceLoader.getCached("static/html/symmetry.html", tempPath + "/html");
                Map<String, String> variables = new HashMap<>();
                variables.put("mode", "left");
                if (!params.isEmpty()) {
                    switch (params.getFirst()) {
                        case "左" -> variables.put("mode", "left");
                        case "右" -> variables.put("mode", "right");
                        case "上" -> variables.put("mode", "top");
                        case "下" -> variables.put("mode", "bottom");
                    }
                }
                Map<String, String> images = new HashMap<>();
                images.put("image", imagePath);
                String html = HtmlTemplateUtil.loadTemplate(htmlPath.toString());
                html = HtmlTemplateUtil.replaceVariables(html, variables);
                html = HtmlTemplateUtil.replaceImages(html, images);
                base64 = htmlRenderer.renderElement(html, "#mirrorContainer");

            } catch (NullBotMsgException e) {
                throw e;
            } catch (Exception e) {
                throw new NullBotMsgException("[对称] ❌处理时出错: " + e.getMessage());
            } finally {
                FileUtils.deleteQuietly(new File(tempPath + "/" + downloadedName));
            }
            String response = MsgUtils.builder().img("base64://" + base64).build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("\t\t\t\t├─[Symmetry] 处理完成 - {}", downloadedName);
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Symmetry 命令
                功能: 图片对称
                限权: %d 级
                格式:
                1. [引用] Symmetry [可选: 方式]
                2. Symmetry [可选: 方式] [@任何人|QQ号]
                方式: 上/下/左/右 (默认左)
                别名: 对称""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Symmetry 命令
                功能: 头像图片对称处理
                格式: Symmetry [可选: 方式] [QQ号]
                方式: 上/下/左/右 (默认左)
                示例: Symmetry 右 2660181154""";
    }
}
