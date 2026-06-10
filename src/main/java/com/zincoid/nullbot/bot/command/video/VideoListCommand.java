package com.zincoid.nullbot.bot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.model.bot.args.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import com.zincoid.nullbot.core.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CommandMapping({"VideoList", "视频列表"})
@Component
@RequiredArgsConstructor
public class VideoListCommand implements Command {

    private final StorageProperties storageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String videoPath = storageProperties.getVideoPath();
        List<FilePO> videos = fileService.list(videoPath);
        List<String> fileNames = videos.stream().map(FilePO::getFileName).toList();
        if (videos.size() > 50) throw new BotInfoException(Emoji.INFO, "过多暂不展示: 共%s个".formatted(videos.size()));
        bot.sendGroupMsg(event.getGroupId(), """
                [视频列表] ✅已获取
                %s""".formatted(String.join("\n", fileNames)), false);
        log.info("☑ [VideoList] 视频列表已获取");
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
