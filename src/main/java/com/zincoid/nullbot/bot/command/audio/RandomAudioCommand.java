package com.zincoid.nullbot.bot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.tool.OssUrlBuilder;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@CommandMapping({"RandomAudio", "Audio", "audio", "aud", "随机音频", "音频"})
@Component
@RequiredArgsConstructor
public class RandomAudioCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String audioPath = fileStorageProperties.getAudioPath();
        List<FilePO> audios = fileService.search("", audioPath);
        if (audios.isEmpty())
            throw new NullBotException("暂无音频");
        FilePO audio = audios.get(ThreadLocalRandom.current().nextInt(audios.size()));
        String response = MsgUtils.builder()
                .voice(ossUrlBuilder.from(audio.getId()))
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [RandomAudio] 音频已发送: {}", audio.getFileName());
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
