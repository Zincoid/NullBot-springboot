package org.bot.nullbot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;

@CommandMapping({"ImageFolder", "图片目录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageFolderStructureCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws IOException {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String structure = FileUtil.getFolderTreeString(fileStorageConfig.getImagePath(), 0);
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片目录] \uD83D\uDCC1已获取！\n" + structure, false);
            log.info("\t\t\t\t├─[Image.Folder] 已输出 - 图片目录结构");
        } else
            log.info("\t\t\t\t├─[Image.Folder] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ ImageFolder 命令\n功能: 获取图片目录树结构\n限权: " + getAccess() + "\n格式: ImageFolder\n中文命令: 图片目录";
    }
}
