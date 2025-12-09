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

@CommandMapping({"ImageDelete"})
@Component
@RequiredArgsConstructor
public class ImageDeleteCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ImageDeleteCommand.class);
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String fileName = event.getCommandParameters().get(0);
            String response = FileUtil.deleteFileByName(fileStorageConfig.getImagePath() + "/collect", fileName);
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Image.Delete] " + response, false);
            logger.info("\t\t\t\t├─[Image.Delete] {}", response);
        }else
            logger.info("\t\t\t\t├─[Image.Delete] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 2;
    }

    @Override
    public String getHelp() {
        return "◉ ImageDelete 命令\n功能: 删除保存的图片\n限权: " + getAccess() + "\n格式: ImageDelete [文件名]";
    }
}
