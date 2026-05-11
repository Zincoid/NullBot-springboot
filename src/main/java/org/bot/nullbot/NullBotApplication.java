package org.bot.nullbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class NullBotApplication {

    private static ConfigurableApplicationContext context;
    private static String[] args;

    public static void main(String[] args) {
        NullBotApplication.args = args;
        NullBotApplication.context = SpringApplication.run(NullBotApplication.class, args);
    }

    public static void restart() {
        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(NullBotApplication.class, args);
        });
        thread.setDaemon(false);
        thread.start();
    }
}
