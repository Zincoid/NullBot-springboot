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
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"VideoGet", "获取视频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoGetCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(!event.getCommandParameters().isEmpty()){
                String videoPath = FileUtil.getFilePathByName(fileStorageConfig.getVideoPath(), event.getCommandParameters().getFirst());
                if (videoPath != null) {
                    String response = MsgUtils.builder()
                            .video(videoPath, "")
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    log.info("\t\t\t\t├─[Video.Get] 已获取视频: {}", videoPath);
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌未找到该视频", false);
                    log.info("\t\t\t\t├─[Video.Get] 未找到该视频");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌无文件名参数", false);
                log.info("\t\t\t\t├─[Video.Get] 无文件名参数");
            }
        }else
            log.info("\t\t\t\t├─[Video.Get] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoGet 命令
                功能: 获取保存的视频
                限权: %d
                格式: VideoGet [文件名]
                中文命令: 获取视频""", getAccess()
        );
    }
}
