package org.bot.nullbot.command.assist;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.resource.ResourceLoader;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@CommandMapping({"Help", "help", "帮助"})
@Component
@Slf4j
@RequiredArgsConstructor
public class HelpCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final ResourceLoader resourceLoader;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        try {
            String helpPath = resourceLoader
                    .getCached("static/help/help.jpg", fileStorageProperties.getTempPath())
                    .toAbsolutePath().toString();
            String response = MsgUtils.builder().img(helpPath).build();
            bot.sendGroupMsg(event.getGroupId(), response, false);
            log.info("\t\t\t\t├─[Help] 已获取帮助");
        } catch (IOException e) {
            throw new NullBotMsgException("[帮助] ❌资源缺失");
        }
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Help 命令
                功能: 发送帮助菜单
                限权: %d 级
                格式: Help
                别名: help/帮助""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Help 命令
                功能: 发送帮助菜单
                格式: Help""";
    }
}
