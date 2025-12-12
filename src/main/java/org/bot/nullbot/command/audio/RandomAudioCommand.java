package org.bot.nullbot.command.audio;

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

@CommandMapping({"RandomAudio", "audio", "随机音频", "音频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomAudioCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String audioPath = FileUtil.getRandomFile(fileStorageConfig.getAudioPath());
            if (audioPath != null) {
                String response = MsgUtils.builder()
                        .voice(audioPath)
                        .build();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Audio.Random] 已发送音频: {}", audioPath);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌暂无音频", false);
                log.info("\t\t\t\t├─[Audio.Random] 暂无音频");
            }
        }else
            log.info("\t\t\t\t├─[Audio.Random] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ RandomAudio 或 audio 命令\n功能: 发送保存的随机音频\n限权: " + getAccess() + "\n格式: RandomAudio 或 audio\n中文命令: 随机音频 或 音频";
    }
}
