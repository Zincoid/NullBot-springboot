package org.bot.nullbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class QqBotApplication
{
    private static ConfigurableApplicationContext context;
    private static String[] args;

    public static void main(String[] args) {
        QqBotApplication.args = args;
        QqBotApplication.context = SpringApplication.run(QqBotApplication.class, args);
    }

    public static void restart() {
        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(QqBotApplication.class, args);
        });
        thread.setDaemon(false);
        thread.start();
    }
}
