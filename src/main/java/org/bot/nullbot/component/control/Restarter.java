package org.bot.nullbot.component.control;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.NullBotApplication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Restarter
{
    public void restart() {
        NullBotApplication.restart();
    }

    public void restartViaJvm() {
        log.info("▽ [Restarter] JVM重启暂未实现");
    }
}
