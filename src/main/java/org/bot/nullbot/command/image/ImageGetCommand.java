package org.bot.nullbot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
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

@CommandMapping({"ImageGet", "获取图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageGetCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(!event.getCommandParameters().isEmpty()){
                String imagePath = FileUtil.getFilePathByName(fileStorageConfig.getImagePath() + "/collect", event.getCommandParameters().getFirst());
                if (imagePath != null) {
                    String response = MsgUtils.builder()
                            .img(imagePath)
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    log.info("\t\t\t\t├─[Image.Get] 已获取图片: {}", imagePath);
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌未找到该图片", false);
                    log.info("\t\t\t\t├─[Image.Get] 未找到该图片");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌无文件名参数", false);
                log.info("\t\t\t\t├─[Image.Get] 无文件名参数");
            }
        }else
            log.info("\t\t\t\t├─[Image.Get] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ImageGet 命令
                功能: 获取保存的图片
                限权: %d 级
                格式: ImageGet [文件名]
                中文命令: 获取图片""", getAccess()
        );
    }
}
