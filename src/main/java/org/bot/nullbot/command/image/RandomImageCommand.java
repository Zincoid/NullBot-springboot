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


@CommandMapping({"RandomImage", "Image", "image", "img", "随机图片", "图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomImageCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String imagePath;
        try {
            imagePath = FileUtil.getRandomFilePath(fileStorageProperties.getImagePath() + "/collect");
        } catch (Exception e) {
            throw new NullBotMsgException("[随机图片] ❌目录异常");
        }
        if (imagePath == null)
            throw new NullBotMsgException("[随机图片] ❌暂无图片");

        String response = MsgUtils.builder()
                .img(imagePath)
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[RandomImage] 已发送图片 - {}", imagePath);
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomImage 命令
                功能: 发送保存的随机图片
                限权: %d 级
                格式: RandomImage
                别名: Image/image/img/随机图片/图片""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ RandomImage 命令
                功能: 发送保存的随机图片
                格式: RandomImage""";
    }
}
