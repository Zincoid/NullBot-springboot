package com.zincoid.nullbot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.annotation.CommandMapping;
import com.zincoid.nullbot.command.Command;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Log", "log", "日志"})
@Component
@RequiredArgsConstructor
@Slf4j
public class LogCommand implements Command {

    @Value("${logging.file.name}")
    private String logPath;  // 通过 yaml 配置时获取日志文件路径

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        // String logPath = getLogFilePath();
        bot.uploadGroupFile(
                event.getGroupId(),
                logPath,
                logPath.substring(logPath.lastIndexOf("/") + 1)
        );
        log.info("\t\t\t\t├─[Log] 日志已发送");
    }

    // public static String getLogFilePath() {  // 通过 xml 配置时获取日志文件路径
    //     LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    //     // 获取名为 "FILE" 的 appender
    //     RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender =
    //             (RollingFileAppender<ILoggingEvent>)
    //                     loggerContext.getLogger("ROOT").getAppender("FILE");
    //     if (fileAppender != null) {
    //         return fileAppender.getFile();
    //     }
    //     throw new RuntimeException("未找到日志文件路径");
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
