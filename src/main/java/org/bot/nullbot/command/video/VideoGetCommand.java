package org.bot.nullbot.command.video;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"VideoGet", "获取视频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoGetCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if(params.isEmpty()) throw new NullBotMsgException("[获取视频] ❌无文件名参数");
            String videoPath = FileUtil.getFilePathByName(fileStorageProperties.getVideoPath(), params.getFirst());
            if (videoPath == null) throw new NullBotMsgException("[获取视频] ❌未找到该视频");

            String response = MsgUtils.builder()
                    .video(videoPath, "")
                    .build();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
            log.info("\t\t\t\t├─[VideoGet] 已获取视频 - {}", videoPath);
        }else
            throw new NullBotLogException("[获取视频] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoGet 命令
                功能: 获取保存的视频
                限权: %d 级
                格式: VideoGet [文件名]
                别名: 获取视频""", getAccess()
        );
    }
}
