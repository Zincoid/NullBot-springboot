package com.zincoid.nullbot.bot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@CmdMapping({"RandomAudio", "audio", "aud", "随机音频", "音频"})
@Component
@RequiredArgsConstructor
public class RandomAudioCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String audioPath = storageProperties.getAudioPath();
        List<FilePO> audios = fileService.list(audioPath);
        if (audios.isEmpty()) throw new BotInfoException(Emoji.INFO, "暂无音频");
        FilePO audio = audios.get(ThreadLocalRandom.current().nextInt(audios.size()));
        String response = MsgUtils.builder().voice(resourceUrlBuilder.from(audio.getId())).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [RandomAudio] 音频已发送: {}", audio.getFileName());
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomAudio 命令
                功能: 发送随机音频
                限权: %d 级
                格式: RandomAudio
                别名: audio/aud/随机音频/音频""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ RandomAudio 命令
                功能: 发送随机音频
                格式: RandomAudio""";
    }
}
