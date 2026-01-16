package org.bot.nullbot.command.video;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"RandomVideo", "Video", "vid", "随机视频", "视频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomVideoCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String videoPath;
            try {
                videoPath = FileUtil.getRandomFile(fileStorageConfig.getVideoPath());
            } catch (Exception e) {
                throw new NullBotMsgException("[随机视频] ❌目录异常");
            }
            if (videoPath == null)
                throw new NullBotMsgException("[随机视频] ❌暂无视频");

            String response = MsgUtils.builder()
                    .video(videoPath, "")
                    .build();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
            log.info("\t\t\t\t├─[RandomVideo] 已发送视频 - {}", videoPath);
        }else
            throw new NullBotLogException("[随机视频] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomVideo 命令
                功能: 发送保存的随机视频
                限权: %d 级
                格式: RandomVideo 或 Video 或 vid
                中文命令: 随机视频/视频""", getAccess()
        );
    }
}
