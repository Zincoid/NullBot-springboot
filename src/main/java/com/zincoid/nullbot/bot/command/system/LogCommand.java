package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Log", "log", "日志"})
@Component
@RequiredArgsConstructor
public class LogCommand implements Command {

    @Value("${logging.file.name}")
    private String logPath;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        bot.uploadGroupFile(
                event.getGroupId(),
                getLogFilePath(),
                getLogFilePath().substring(logPath.lastIndexOf("/") + 1)
        );
        log.info("☑ [Log] 日志已发送: {}", getLogFilePath());
    }

    // 获取日志路径 (YML 配置)
    public String getLogFilePath() {
        return logPath;
    }

    // 获取日志路径 (XML 配置)
    // public static String getLogFilePath() {
    //     LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    //     RollingFileAppender<ILoggingEvent> fileAppender =  // 获取名为 "FILE" 的 appender
    //             (RollingFileAppender<ILoggingEvent>) loggerContext.getLogger("ROOT").getAppender("FILE");
    //     if (fileAppender != null) return fileAppender.getFile();
    //     throw new BotWarnException("未找到日志文件路径");
    // }

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
