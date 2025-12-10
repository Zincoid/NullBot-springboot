package org.bot.nullbot.command.video;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.command.image.RandomImageCommand;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"RandomVideo", "video", "随机视频", "视频"})
@Component
@RequiredArgsConstructor
public class RandomVideoCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(RandomVideoCommand.class);
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String videoPath = FileUtil.getRandomFile(fileStorageConfig.getVideoPath());
            if (videoPath != null) {
                String response = MsgUtils.builder()
                        .video(videoPath, null)
                        .build();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                logger.info("\t\t\t\t├─[Video.Random] 已发送视频: {}", videoPath);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌暂无视频", false);
                logger.info("\t\t\t\t├─[Video.Random] 暂无视频");
            }
        }else
            logger.info("\t\t\t\t├─[Video.Random] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ RandomVideo 或 video 命令\n功能: 发送保存的随机视频\n限权: " + getAccess() + "\n格式: RandomVideo 或 video\n中文命令: 随机视频 或 视频";
    }
}
