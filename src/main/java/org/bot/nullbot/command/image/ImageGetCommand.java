package org.bot.nullbot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ImageGet", "获取图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageGetCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotMsgException("[获取图片] ❌无文件名参数");
        String imagePath = FileUtil.getFilePathByName(fileStorageProperties.getImagePath() + "/collect", params.getFirst());
        if (imagePath == null)
            throw new NullBotMsgException("[获取图片] ❌未找到该图片");
        String response = MsgUtils.builder()
                .img(imagePath)
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[ImageGet] 已获取图片 - {}", imagePath);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ImageGet 命令
                功能: 获取保存的图片
                限权: %d 级
                格式: ImageGet [文件名]
                别名: 获取图片""", getAccess()
        );
    }
}
