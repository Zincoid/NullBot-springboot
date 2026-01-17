package org.bot.nullbot.command.reply;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.resource.ResourceLoader;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@CommandMapping({"Help", "help", "帮助"})
@Component
@Slf4j
@RequiredArgsConstructor
public class HelpCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;
    private final ResourceLoader resourceLoader;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            try {
                String helpPath = resourceLoader.getCached("static/help/help.jpg", fileStorageConfig.getTempPath()).toAbsolutePath().toString();
                String response = MsgUtils.builder().img(helpPath).build();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Help] 已获取帮助");
            } catch (IOException e) {
                throw new NullBotMsgException("[帮助] ❌资源缺失");
            }
        }else
            throw new NullBotLogException("[帮助] ❌未设计 - 非群消息事件响应方式");
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
}
