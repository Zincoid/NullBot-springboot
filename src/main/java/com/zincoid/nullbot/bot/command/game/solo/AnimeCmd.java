package com.zincoid.nullbot.bot.command.game.solo;

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
@CmdMapping({"Anime", "anime", "二次元", "色图", "涩图"})
@Component
@RequiredArgsConstructor
public class AnimeCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String animePath = storageProperties.getImagePath() + "/acg/二次元";
        List<FilePO> images = fileService.list(animePath);
        if (images.isEmpty()) throw new BotInfoException(Emoji.INFO, "暂无图片");
        FilePO image = images.get(ThreadLocalRandom.current().nextInt(images.size()));
        String response = MsgUtils.builder().img(resourceUrlBuilder.from(image.getId())).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [Anime] 图片已获取: {}", image.getFileName());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Anime 命令
                功能: 随机二/三次元图
                限权: %d 级
                格式: Anime
                别名: anime/二次元/色图/涩图""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Anime 命令
                功能: 随机二/三次元图
                格式: Anime""";
    }
}
