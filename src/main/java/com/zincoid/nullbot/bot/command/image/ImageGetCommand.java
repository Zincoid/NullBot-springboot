package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.tool.OssUrlBuilder;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.po.FilePO;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.mapper.FileMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ImageGet", "获取图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageGetCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileMapper fileMapper;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotMsgException("[获取图片] ❌无文件名参数");
        String imagePath = fileStorageProperties.getImagePath() + "/collect";
        List<FilePO> images = fileMapper.searchFile(params.getFirst(), imagePath);
        if (images.isEmpty())
            throw new NullBotMsgException("[获取图片] ❌未找到该图片");
        if (images.size() > 1)
            throw new NullBotMsgException("[获取图片] ❌找到多个图片");
        FilePO image = images.getFirst();
        String response = MsgUtils.builder()
                .img(ossUrlBuilder.from(image.getId()))
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[ImageGet] 已获取图片 - {}", image.getFileName());
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
