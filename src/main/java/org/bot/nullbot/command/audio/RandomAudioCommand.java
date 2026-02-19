package org.bot.nullbot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"RandomAudio", "Audio", "audio", "aud", "随机音频", "音频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomAudioCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String audioPath;
        try {
            audioPath = FileUtil.getRandomFilePath(fileStorageProperties.getAudioPath());
        } catch (Exception e) {
            throw new NullBotMsgException("[随机音频] ❌目录异常");
        }
        if (audioPath == null)
            throw new NullBotMsgException("[随机音频] ❌暂无音频");

        String response = MsgUtils.builder()
                .voice(audioPath)
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[RandomAudio] 已发送音频 - {}", audioPath);
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomAudio 命令
                功能: 发送保存的随机音频
                限权: %d 级
                格式: RandomAudio
                别名: Audio/audio/aud/随机音频/音频""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ RandomAudio 命令
                功能: 发送保存的随机音频
                格式: RandomAudio""";
    }
}
