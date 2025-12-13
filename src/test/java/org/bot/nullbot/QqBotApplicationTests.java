package org.bot.nullbot;

import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.service.game.TicTacToeService;
import org.bot.nullbot.util.FileUtil;
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
    @Resource
    Matcher matcher;
    @Resource
    TicTacToeService ticTacToeService;

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

   // @Test
   void matchTest() {
       System.out.println(matcher.joinMatch(0L, 1L, "A", "tictactoe"));
       // System.out.println(matchService.joinMatch(0L, 1L, "B", "tictactoe"));
       System.out.println(matcher.joinMatch(1L, 2L, "C", "tictactoe"));
       // System.out.println(matchService.joinMatch(2L, 3L, "D", "punch"));


       boolean current = true;
       Scanner scanner = new Scanner(System.in);
       while (true)
       {
           System.out.println("Enter command: ");
           String command = scanner.nextLine();
           int i = Integer.parseInt(command.split(" ")[0]);
           int j = Integer.parseInt(command.split(" ")[1]);

           if(current){
               System.out.println(ticTacToeService.move(0L, i, j).getInfo());
               current = false;
           }else{
               System.out.println(ticTacToeService.move(1L, i, j).getInfo());
               current = true;
           }
       }

       // System.out.println(ticTacToeService.move(0L, 1, 3));
       // System.out.println(ticTacToeService.move(1L, 2, 2));
       // System.out.println(ticTacToeService.move(0L, 1, 1));
       // System.out.println(ticTacToeService.move(0L, 1, 1));
   }
}
