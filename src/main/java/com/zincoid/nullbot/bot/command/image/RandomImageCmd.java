package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@CmdMapping({"RandomImage", "image", "img", "随机图片", "图片"})
@Component
@RequiredArgsConstructor
public class RandomImageCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String imagePath = storageProperties.getImagePath() + "/collect";
        List<FilePO> images = fileService.list(imagePath);
        if (images.isEmpty()) throw new BotInfoException(Emoji.INFO, "暂无图片");
        FilePO image = images.get(ThreadLocalRandom.current().nextInt(images.size()));
        String response = MsgUtils.builder().img(resourceUrlBuilder.from(image.getId())).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [RandomImage] 图片已发送: {}", image.getFileName());
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomImage 命令
                功能: 发送随机保存图片
                限权: %d 级
                格式: RandomImage
                别名: image/img/随机图片/图片""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ RandomImage 命令
                功能: 发送随机保存图片
                格式: RandomImage""";
    }
}
