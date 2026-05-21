package com.zincoid.nullbot.bot.command.video;

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
import com.zincoid.nullbot.core.service.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@CommandMapping({"RandomVideo", "Video", "video", "vid", "随机视频", "视频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomVideoCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String videoPath = fileStorageProperties.getVideoPath() + "/collect";
        List<FilePO> videos = fileService.search("", videoPath);
        if (videos.isEmpty())
            throw new NullBotMsgException("[随机视频] ❌暂无视频");
        FilePO video = videos.get(ThreadLocalRandom.current().nextInt(videos.size()));
        String response = MsgUtils.builder()
                .video(ossUrlBuilder.from(video.getId()), "")
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[RandomVideo] 已发送视频 - {}", video.getFileName());
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomVideo 命令
                功能: 发送保存的随机视频
                限权: %d 级
                格式: RandomVideo
                别名: Video/video/vid/随机视频/视频""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ RandomVideo 命令
                功能: 发送保存的随机视频
                格式: RandomVideo""";
    }
}
