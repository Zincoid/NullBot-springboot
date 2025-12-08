package org.bot.nullbot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
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

@CommandMapping({"ImageGet"})
@Component
@RequiredArgsConstructor
public class ImageGetCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ImageGetCommand.class);
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(!event.getCommandParameters().isEmpty()){
                String imagePath = FileUtil.getFilePathByName(fileStorageConfig.getImagePath() + "/collect", event.getCommandParameters().get(0));
                if (imagePath != null) {
                    String response = MsgUtils.builder()
                            // .text("Info")
                            .img(imagePath)
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    logger.info("\t\t\t\t├─[Image.Get] 已获取图片: {}", imagePath);
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Image.Get] 未找到图片", false);
                    logger.info("\t\t\t\t├─[Image.Get] 未找到图片");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Image.Get] 无参数", false);
                logger.info("\t\t\t\t├─[Image.Get] 无参数");
            }
        }else
            logger.info("\t\t\t\t├─[Image.Get] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "/ImageGet 命令\n功能: 获取保存的图片\n格式: /ImageGet [文件名]";
    }
}
