package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.tool.OssUrlBuilder;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@CommandMapping({"RandomImage", "Image", "image", "img", "随机图片", "图片"})
@Component
@RequiredArgsConstructor
public class RandomImageCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String imagePath = fileStorageProperties.getImagePath() + "/collect";
        List<FilePO> images = fileService.search("", imagePath);
        if (images.isEmpty())
            throw new NullBotException("暂无图片");
        FilePO image = images.get(ThreadLocalRandom.current().nextInt(images.size()));
        String response = MsgUtils.builder()
                .img(ossUrlBuilder.from(image.getId()))
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [RandomImage] 图片已发送: {}", image.getFileName());
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
