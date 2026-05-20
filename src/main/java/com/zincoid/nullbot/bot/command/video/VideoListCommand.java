package com.zincoid.nullbot.bot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.config.prop.FileStorageProperties;
import com.zincoid.nullbot.core.entity.po.FilePO;
import com.zincoid.nullbot.core.mapper.FileMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"VideoList", "视频列表"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoListCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileMapper fileMapper;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String videoPath = fileStorageProperties.getVideoPath();
        List<FilePO> videos = fileMapper.searchFile("", videoPath);
        List<String> fileNames = videos.stream().map(FilePO::getFileName).toList();
        if (videos.size() > 50) {
            log.info("\t\t\t\t├─[VideoList] 视频列表数据过多 - {}", fileNames);
            bot.sendGroupMsg(event.getGroupId(), """
                    [视频列表] ✅过多暂不展示
                    - 共 %s 个视频""".formatted(videos.size()), false);
            return;
        }
        bot.sendGroupMsg(event.getGroupId(), "[视频列表] ✅已获取\n"
                + String.join("\n", fileNames), false);
        log.info("\t\t\t\t├─[VideoList] 已获取视频列表");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoList 命令
                功能: 获取保存视频列表
                限权: %d 级
                格式: VideoList
                别名: 视频列表""", getAccess()
        );
    }
}
