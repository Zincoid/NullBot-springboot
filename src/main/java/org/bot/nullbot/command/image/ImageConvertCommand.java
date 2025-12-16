package org.bot.nullbot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.bot.nullbot.util.image.ImageConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@CommandMapping({"ImageConvert", "图像处理"})
@Component
@Slf4j
@RequiredArgsConstructor
public class ImageConvertCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (!event.getCommandParameters().isEmpty()) {
                String method = event.getCommandParameters().getFirst();
                if (!List.of("RIP").contains(method)) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图像转换] ❌方法不存在", false);
                    log.info("\t\t\t\t├─[Image.Convert] 方法不存在");
                    return;
                }
                String tempFilePath = fileStorageConfig.getTempPath();
                List<Long> qqNumbers = MessageParseUtil.extractAtQQNumbers(groupMessageEvent.getRawMessage());
                for (Long qqNumber : qqNumbers) {
                    String tempFileName = UUID.randomUUID().toString();
                    String avatarUrl = ShiroUtils.getUserAvatar(qqNumber, 5);
                    String downloadedFileName = DownloadUtil.downloadFile(avatarUrl, tempFilePath, tempFileName);
                    if (downloadedFileName == null) {
                        log.info("\t\t\t\t├─[Image.Convert] 下载头像失败 - {}", qqNumber);
                        continue;
                    }
                    try {
                        String base64 = ImageConverter.rip(tempFilePath + "/" + downloadedFileName);
                        String response = MsgUtils.builder().img("base64://" + base64).build();
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    } catch (Exception e) {
                        log.info("\t\t\t\t├─[Image.Convert] 处理QQ {} 时出错: {}", qqNumber, e.getMessage(), e);
                    } finally {
                        FileUtil.deleteFileByName(tempFilePath, downloadedFileName);
                    }
                }
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图像转换] ✅处理完毕！", false);
            }
            log.info("\t\t\t\t├─[Image.Convert] 已处理");
        }else
            log.info("\t\t\t\t├─[Image.Convert] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ ImageConvert 命令\n" +
                "功能: P图!!!\n" +
                "限权: " + getAccess() + "\n" +
                "格式: ImageConvert [处理方式] [@任何人]\n" +
                "中文命令: 图像处理";
    }
}
