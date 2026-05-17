package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.tool.OssUrlBuilder;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@CommandMapping({"Anime", "anime", "二次元", "色图", "涩图"})
@Component
@RequiredArgsConstructor
@Slf4j
public class AnimeCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String animePath = fileStorageProperties.getImagePath() + "/acg/二次元";
        List<FilePO> images = fileService.search("", animePath);
        if (images.isEmpty())
            throw new NullBotMsgException("[二次元] ❌暂无图片");
        FilePO image = images.get(ThreadLocalRandom.current().nextInt(images.size()));
        String response = MsgUtils.builder()
                .img(ossUrlBuilder.from(image.getId()))
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[Anime] 获取二次元图片 - {}", image.getFileName());
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
