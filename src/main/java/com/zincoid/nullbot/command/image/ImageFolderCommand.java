package com.zincoid.nullbot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.annotation.CommandMapping;
import com.zincoid.nullbot.command.Command;
import com.zincoid.nullbot.config.prop.FileStorageProperties;
import com.zincoid.nullbot.util.StringUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@CommandMapping({"ImageFolder", "图片目录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageFolderCommand implements Command {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) throws IOException {
        String structure = StringUtil.getFolderTreeString(fileStorageProperties.getImagePath(), 0);
        bot.sendGroupMsg(event.getGroupId(), "[图片目录] \uD83D\uDCC1已获取！\n" + structure, false);
        log.info("\t\t\t\t├─[ImageFolder] 已输出 - 图片目录结构");
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
