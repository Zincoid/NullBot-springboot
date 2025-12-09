package org.bot.nullbot;

import org.bot.nullbot.plugin.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QqBotApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void fileTest() throws IOException {
        System.out.println(FileUtil.getFolderTreeString("C:\\Users\\Zincoid\\IdeaProjects\\NullBot\\src\\main\\resources", 0));
    }
}
