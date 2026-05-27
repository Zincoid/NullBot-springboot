package com.zincoid.nullbot.bot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
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
@CommandMapping({"Anime", "anime", "二次元", "色图", "涩图"})
@Component
@RequiredArgsConstructor
public class AnimeCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String animePath = fileStorageProperties.getImagePath() + "/acg/二次元";
        List<FilePO> images = fileService.search("", animePath);
        if (images.isEmpty()) throw new BotInfoException(Emoji.INFO, "暂无图片");
        FilePO image = images.get(ThreadLocalRandom.current().nextInt(images.size()));
        String response = MsgUtils.builder()
                .img(ossUrlBuilder.from(image.getId()))
                .build();
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
