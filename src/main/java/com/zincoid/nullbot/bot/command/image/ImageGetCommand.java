package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.mapper.FileMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CommandMapping({"ImageGet", "获取图片"})
@Component
@RequiredArgsConstructor
public class ImageGetCommand implements Command {

    private final StorageProperties storageProperties;
    private final FileMapper fileMapper;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String imagePath = storageProperties.getImagePath() + "/collect";
        List<FilePO> images = fileMapper.searchFile(args.nextFullString(), imagePath);
        if (images.isEmpty()) throw new BotInfoException(Emoji.INFO, "图片未找到");
        if (images.size() > 1) throw new BotInfoException(Emoji.INFO, "匹配项过多");
        FilePO image = images.getFirst();
        String response = MsgUtils.builder().img(resourceUrlBuilder.from(image.getId())).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [ImageGet] 图片已获取: {}", image.getFileName());
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
