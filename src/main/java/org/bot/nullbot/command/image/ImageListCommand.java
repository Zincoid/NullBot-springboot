package org.bot.nullbot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@CommandMapping({"ImageList", "图片列表"})
@Component
@RequiredArgsConstructor
public class ImageListCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ImageListCommand.class);
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String imageList = FileUtil.getFileListAsString(fileStorageConfig.getImagePath() + "/collect");
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), imageList, false);
            logger.info("\t\t\t\t├─[Image.List] 已获取 - 图片列表");
        }else
            logger.info("\t\t\t\t├─[Image.List] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return "◉ ImageList 命令\n功能: 获取保存图片的列表\n限权: " + getAccess() + "\n格式: ImageList\n中文命令: 图片列表";
    }
}
