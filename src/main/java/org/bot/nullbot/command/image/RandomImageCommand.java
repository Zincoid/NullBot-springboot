package org.bot.nullbot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.tool.OssUrlBuilder;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@CommandMapping({"RandomImage", "Image", "image", "img", "随机图片", "图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomImageCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String imagePath = fileStorageProperties.getImagePath() + "/collect";
        List<FilePO> images = fileService.search("", imagePath);
        if (images.isEmpty())
            throw new NullBotMsgException("[随机图片] ❌暂无图片");
        FilePO image = images.get(ThreadLocalRandom.current().nextInt(images.size()));
        String response = MsgUtils.builder()
                .img(ossUrlBuilder.from(image.getId()))
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[RandomImage] 已发送图片 - {}", image.getFileName());
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
