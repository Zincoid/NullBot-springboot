package org.bot.nullbot;

import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import org.bot.nullbot.component.game.logic.TicTacToeGameLogic;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.svg.SvgCanvas;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.ResourceLoader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QqBotApplicationTests {
    @Resource
    private CommandProcessor commandProcessor;
    @Resource
    Matcher matcher;
    @Resource
    TicTacToeGameLogic ticTacToeGameLogic;

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
       System.out.println(matcher.joinMatch(0L, 1L, "A", "reversi"));
       // System.out.println(matchService.joinMatch(0L, 1L, "B", "tictactoe"));
       System.out.println(matcher.joinMatch(1L, 2L, "C", "reversi"));
       // System.out.println(matchService.joinMatch(2L, 3L, "D", "punch"));

       // boolean current = true;
       // Scanner scanner = new Scanner(System.in);
       // while (true)
       // {
       //     System.out.println("Enter command: ");
       //     String command = scanner.nextLine();
       //     int i = Integer.parseInt(command.split(" ")[0]);
       //     int j = Integer.parseInt(command.split(" ")[1]);
       //
       //     if(current){
       //         System.out.println(ticTacToeGameLogic.move(0L, i, j).getInfo());
       //         current = false;
       //     }else{
       //         System.out.println(ticTacToeGameLogic.move(1L, i, j).getInfo());
       //         current = true;
       //     }
       // }
   }

    // @Test
    void renderTest() throws Exception {
        Path fontPath = ResourceLoader.getCached("static/fonts/Gilroy-Bold.ttf", "/root/Nullbot/file/temp/fonts");

        System.out.println("字体路径: " + fontPath.toAbsolutePath());
        System.out.println("字体文件存在: " + Files.exists(fontPath));

        // 创建 SVG 画布
        SvgCanvas canvas = SvgCanvas.create(640, 640);

        // 添加用户头像
        canvas.image(
                0, 0, 640, 640,
                Path.of("src/test/testOutput/input.jpg"), true
        );

        // 添加 RIP 文字 - 确保使用正确的字体名称
        canvas.text(160, 550, "R.I.P.")
                .font("Gilroy")
                .size(150)
                .color("#000000")
                .bold()
                .stroke("#FFFFFF", 8);

        // 使用 resvg 渲染为 PNG
        Path outputPath = Path.of("src/test/testOutput/output.png");
        canvas.renderToImg(outputPath, "/root/Nullbot/file/temp/fonts");

        // 先保存SVG文件查看内容
        Path svgPath = Path.of("src/test/testOutput/output.svg");
        canvas.exportSvg(svgPath);

        // 读取并打印SVG内容
        String svgContent = Files.readString(svgPath);
        System.out.println("生成的SVG内容:");
        System.out.println(svgContent.substring(0, Math.min(svgContent.length(), 2000)) + "...");

        System.out.println("图片已生成: " + outputPath.toAbsolutePath());
    }
}
