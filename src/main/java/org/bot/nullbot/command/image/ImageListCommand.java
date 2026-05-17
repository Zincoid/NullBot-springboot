package org.bot.nullbot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.mapper.FileMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ImageList", "图片列表"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageListCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileMapper fileMapper;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String imagePath = fileStorageProperties.getImagePath() + "/collect";
        List<FilePO> images = fileMapper.searchFile("", imagePath);
        List<String> fileNames = images.stream().map(FilePO::getFileName).toList();
        if (images.size() > 50) {
            log.info("\t\t\t\t├─[ImageList] 图片列表数据过多 - {}", fileNames);
            bot.sendGroupMsg(event.getGroupId(), """
                    [图片列表] ✅过多暂不展示
                    - 共 %s 张图片""".formatted(images.size()), false);
            return;
        }
        bot.sendGroupMsg(event.getGroupId(), "[图片列表] ✅已获取\n"
                + String.join("\n", fileNames), false);
        log.info("\t\t\t\t├─[ImageList] 已获取图片列表");
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
