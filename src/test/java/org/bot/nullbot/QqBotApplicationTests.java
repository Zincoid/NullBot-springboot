package org.bot.nullbot;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QqBotApplicationTests {
    @Resource
    private CommandProcessor commandProcessor;

    // @Test
    void fileTest() throws IOException {
        System.out.println(FileUtil.getFolderTreeString("C:\\Users\\Zincoid\\IdeaProjects\\NullBot\\src\\main\\resources", 0));
    }

   // @Test
   void commandTest() throws Exception {
       String commandType = "help";
       List<String> commandParameters =  new ArrayList<>();
       commandProcessor.processTest(new CommandEvent<>(commandType, commandParameters, new GroupMessageEvent(), false));

       // while (true)
       // {
       //     Scanner scanner = new Scanner(System.in);
       //     System.out.println("Enter command: ");
       //     String command = scanner.nextLine();
       //     commandProcessor.processTest(new CommandEvent<>(command));
       // }
   }
}
