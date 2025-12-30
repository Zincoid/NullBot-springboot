package org.bot.nullbot.command.image;

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


@CommandMapping({"RandomImage", "img", "随机图片", "图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomImageCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String imagePath = FileUtil.getRandomFile(fileStorageConfig.getImagePath() + "/collect");
            if (imagePath != null) {
                String response = MsgUtils.builder()
                        .img(imagePath)
                        .build();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Image.Random] 已发送图片: {}", imagePath);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌暂无图片", false);
                log.info("\t\t\t\t├─[Image.Random] 暂无图片");
            }
        }else
            log.info("\t\t\t\t├─[Image.Random] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomImage 命令
                功能: 发送保存的随机图片
                限权: %d
                格式: RandomImage 或 img
                中文命令: 随机图片/图片""", getAccess()
        );
    }
}
