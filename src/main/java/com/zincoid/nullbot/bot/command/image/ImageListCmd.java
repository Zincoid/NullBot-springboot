package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import com.zincoid.nullbot.core.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CmdMapping({"ImageList", "图片列表"})
@Component
@RequiredArgsConstructor
public class ImageListCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String imagePath = storageProperties.getImagePath() + "/collect";
        List<FilePO> images = fileService.list(imagePath);
        List<String> fileNames = images.stream().map(FilePO::getFileName).toList();
        if (images.size() > 50) throw new BotInfoException(Emoji.INFO, "过多暂不展示: 共%s张".formatted(images.size()));
        bot.sendGroupMsg(event.getGroupId(), """
                [图片列表] ✅已获取
                %s""".formatted(String.join("\n", fileNames)), false);
        log.info("☑ [ImageList] 图片列表已获取");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ImageList 命令
                功能: 获取保存图片列表
                限权: %d 级
                格式: ImageList
                别名: 图片列表""", getAccess()
        );
    }
}
