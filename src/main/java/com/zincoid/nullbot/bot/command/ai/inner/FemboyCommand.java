package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.model.bot.args.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@CommandMapping({"eb0f8545"})
@Component
@RequiredArgsConstructor
public class FemboyCommand implements Command {

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        String femboyPath = storageProperties.getImagePath() + "/femboy";
        List<FilePO> photos = fileService.list(femboyPath);
        if (photos.isEmpty()) throw new BotInfoException(Emoji.INFO, "暂无图片");
        FilePO photo = photos.get(ThreadLocalRandom.current().nextInt(photos.size()));
        String response = MsgUtils.builder().img(resourceUrlBuilder.from(photo.getId())).build();
        bot.sendGroupMsg(groupId, response, false);
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
