package org.bot.nullbot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.render.HtmlRenderer;
import org.bot.nullbot.component.render.ImageConverter;
import org.bot.nullbot.component.resource.ResourceLoader;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.HtmlTemplateUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

@CommandMapping({"Symmetry", "对称"})
@Component
@Slf4j
@RequiredArgsConstructor
public class SymmetryCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    private final ResourceLoader resourceLoader;
    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();

            List<String> urls = new ArrayList<>();

            // 引用收集
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(replyMsg.getRawMessage());
                urls.addAll(imageMap.values());
            }

            //  ID参数收集 或 AT收集
            if (!event.getCommandParameters().isEmpty()) {
                long qqNumber;
                try {
                    qqNumber = Long.parseLong(event.getCommandParameters().getFirst());
                } catch (NumberFormatException e) {
                    throw new NullBotMsgException("[对称] ❌参数格式错误");
                }
                urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
            }else{
                List<Long> qqNumbers = MessageParseUtil.extractAtQQNumbers(groupMessageEvent.getRawMessage());
                for (Long qqNumber : qqNumbers) urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
            }

            if (urls.isEmpty())
                throw new NullBotMsgException("[对称] ❌无引用图片或ID参数或At消息");

            // 开始处理
            String tempFilePath = fileStorageProperties.getTempPath();
            for (String url : urls) {
                String tempFileName = UUID.randomUUID().toString();
                String downloadedFileName;
                try {
                    FileInfo fileInfo = DownloadUtil.downloadFile(url, tempFilePath, tempFileName, "\t\t\t\t├─ ");
                    downloadedFileName = fileInfo.getFileName();
                } catch (Exception e) {
                    throw new NullBotMsgException("[对称] ❌下载时出错: " + e.getMessage());
                }
                String imagePath = tempFilePath + "/" + downloadedFileName;
                String base64;
                try {
                    Path htmlPath = resourceLoader.getCached("static/html/symmetry.html", tempFilePath + "/html");
                    Map<String, String> variables = new HashMap<>();
                    variables.put("mode", "left");
                    if (!event.getCommandParameters().isEmpty()) {
                        String mode = switch (event.getCommandParameters().getFirst()) {
                            case "右" -> "right";
                            case "上" -> "top";
                            case "下" -> "bottom";
                            default -> "left";
                        };
                        variables.put("mode", mode);
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
                    FileUtil.deleteFileByName(tempFilePath, downloadedFileName);
                }
                String response = MsgUtils.builder().img("base64://" + base64).build();
                bot.sendGroupMsg(groupId, response, false);
                log.info("\t\t\t\t├─[Symmetry] 处理完成 - {}", downloadedFileName);
            }
            // bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[对称] ✅全部处理完成！", false);
        }else
            throw new NullBotLogException("[对称] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Symmetry 命令
                功能: 图片对称
                限权: %d 级
                格式:
                1. [引用] Symmetry
                2. Symmetry [@任何人|QQ号]
                别名: 对称""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ Convert 命令
                功能: 头像图片对称处理
                限权: %d 级
                格式: Symmetry [QQ号]
                示例: Symmetry 2660181154""", getAccess()
        );
    }
}
