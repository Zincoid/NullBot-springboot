package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.util.StringUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@CommandMapping({"ImageFolder", "图片目录"})
@Component
@RequiredArgsConstructor
public class ImageFolderCommand implements Command {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) throws IOException {
        String structure = StringUtil.getFolderTreeString(fileStorageProperties.getImagePath(), 0);
        bot.sendGroupMsg(event.getGroupId(), "[图片目录] \uD83D\uDCC1已获取\n" + structure, false);
        log.info("☑ [ImageFolder] 图片目录已输出");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ImageFolder 命令
                功能: 获取图片目录树结构
                限权: %d 级
                格式: ImageFolder
                别名: 图片目录""",  getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ ImageFolder 命令
                功能: 获取图片目录树结构
                格式: ImageFolder""";
    }
}
