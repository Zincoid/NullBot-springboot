package org.bot.nullbot.command.reply;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.util.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;

@CommandMapping({"Help", "help", "帮助"})
@Component
@Slf4j
@RequiredArgsConstructor
public class HelpCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            try {
                String helpPath = ResourceLoader.getCached("static/help/help.jpg", fileStorageConfig.getTempPath()).toAbsolutePath().toString();
                String response = MsgUtils.builder().img(helpPath).build();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Help] 已获取帮助");
            } catch (IOException e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Help] ❌帮助资源缺失", false);
                log.info("\t\t\t\t├─[Help] 帮助资源缺失");
            }
        }else
            log.info("\t\t\t\t├─[Help] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Help 或 help 命令\n功能: 发送帮助菜单\n限权: " + getAccess() + "\n格式: Help 或 help\n中文命令: 帮助";
    }
}
