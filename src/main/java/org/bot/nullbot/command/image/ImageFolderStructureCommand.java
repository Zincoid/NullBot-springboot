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

import java.io.IOException;

@CommandMapping({"ImageFolderStructure", "图片目录结构"})
@Component
@RequiredArgsConstructor
public class ImageFolderStructureCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ImageFolderStructureCommand.class);
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws IOException {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String structure = FileUtil.getFolderTreeString(fileStorageConfig.getImagePath(), 0);
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片目录结构]\n" + structure, false);
            logger.info("\t\t\t\t├─[Image.FolderStructure] 已输出 - 图片目录结构");
        } else
            logger.info("\t\t\t\t├─[Image.FolderStructure] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ ImageFolderStructure 命令\n功能: 获取图片目录树结构\n限权: " + getAccess() + "\n格式: ImageFolderStructure\n中文命令: 图片目录结构";
    }
}
