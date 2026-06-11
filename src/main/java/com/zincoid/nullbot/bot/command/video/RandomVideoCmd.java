package com.zincoid.nullbot.bot.command.video;

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
@CmdMapping({"RandomVideo", "Video", "video", "vid", "随机视频", "视频"})
@Component
@RequiredArgsConstructor
public class RandomVideoCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String videoPath = storageProperties.getVideoPath() + "/collect";
        List<FilePO> videos = fileService.list(videoPath);
        if (videos.isEmpty()) throw new BotInfoException(Emoji.INFO, "暂无视频");
        FilePO video = videos.get(ThreadLocalRandom.current().nextInt(videos.size()));
        String response = MsgUtils.builder().video(resourceUrlBuilder.from(video.getId()), "").build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [RandomVideo] 视频已发送: {}", video.getFileName());
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
