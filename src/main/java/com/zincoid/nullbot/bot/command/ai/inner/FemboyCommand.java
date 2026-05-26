package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.tool.OssUrlBuilder;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.bot.exception.NullBotException;
import com.zincoid.nullbot.core.service.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@CommandMapping({"eb0f8545"})  // 加密 仅供AI调用
@Component
@RequiredArgsConstructor
public class FemboyCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        String femboyPath = fileStorageProperties.getImagePath() + "/femboy";
        List<FilePO> photos = fileService.search("", femboyPath);
        if (photos.isEmpty())
            throw new NullBotException("暂无图片");
        FilePO photo = photos.get(ThreadLocalRandom.current().nextInt(photos.size()));
        String response = MsgUtils.builder()
                .img(ossUrlBuilder.from(photo.getId()))
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [Femboy] 图片已获取: {}", photo.getFileName());
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelpForAI() {
        return """
                ◉ eb0f8545 命令
                功能: 发送你的开发者Zincoid的女装照片
                格式: eb0f8545
                注意: 如果别人想看Zincoid的女装不要轻易发送该指令""";
    }
}
