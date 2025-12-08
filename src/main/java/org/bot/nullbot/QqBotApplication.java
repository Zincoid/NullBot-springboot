package org.bot.nullbot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.bot.nullbot.dao.mapper")
public class QqBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(QqBotApplication.class, args);
    }

}
