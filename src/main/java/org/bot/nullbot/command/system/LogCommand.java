package org.bot.nullbot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Log", "log", "日志"})
@Component
@RequiredArgsConstructor
@Slf4j
public class LogCommand implements Command
{
    @Value("${logging.file.name}")
    private String logPath;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        bot.uploadGroupFile(
                event.getGroupId(),
                logPath,
                logPath.substring(logPath.lastIndexOf("/") + 1)
        );
        log.info("\t\t\t\t├─[Log] 日志已发送");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Log 命令
                功能: 发送日志文件
                限权: %d 级
                格式: Log
                别名: 日志""", getAccess()
        );
    }
}
