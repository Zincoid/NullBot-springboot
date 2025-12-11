package org.bot.nullbot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.util.FileUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"VideoList", "视频列表"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoListCommand  implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String videoList = FileUtil.getFileListAsString(fileStorageConfig.getVideoPath());
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ✅列表已获取！\n" + videoList, false);
            log.info("\t\t\t\t├─[Video.List] 已获取 - 视频列表");
        }else
            log.info("\t\t\t\t├─[Video.List] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return "◉ VideoList 命令\n功能: 获取保存视频的列表\n限权: " + getAccess() + "\n格式: VideoList\n中文命令: 视频列表";
    }
}
