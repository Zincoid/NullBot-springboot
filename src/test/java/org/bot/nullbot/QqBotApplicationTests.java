package org.bot.nullbot;

import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import org.bot.nullbot.component.game.logic.TicTacToeGameLogic;
import org.bot.nullbot.component.render.WebScreenCapturer;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.svg.SvgCanvas;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.ResourceUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QqBotApplicationTests {
    // @Value("${nullbot.bot-id}")
    // private Long botId;
    // @Resource
    // private BotContainer botContainer;
    @Resource
    private CommandProcessor commandProcessor;
    @Resource
    Matcher matcher;
    @Resource
    TicTacToeGameLogic ticTacToeGameLogic;
    @Resource
    WebScreenCapturer webScreenCapturer;

    // @Test
    // void parseTest() throws IOException {
    //     Bot bot = botContainer.robots.get(botId);
    //     System.out.println(bot.getStrangerInfo(2660181154L, true).getData().getNickname());
    //     System.out.println(MessageParseUtil.parseRawSaying(bot, "[CQ:at,qq=2660181154] 你好！"));
    // }

    @Test
    void fileTest() throws IOException {
        System.out.println(FileUtil.getFolderTreeString("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\main", 0));
    }

   @Test
   void commandTest() throws Exception {
       String commandType = "help";
       List<String> commandParameters =  new ArrayList<>();
       commandProcessor.processTest(new CommandEvent<>(commandType, commandParameters, new GroupMessageEvent(), false, true));

       // while (true)
       // {
       //     Scanner scanner = new Scanner(System.in);
       //     System.out.println("Enter command: ");
       //     String command = scanner.nextLine();
       //     commandProcessor.processTest(new CommandEvent<>(command));
       // }
   }

   @Test
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

    @Test
    void renderTest() throws Exception {
        // Path fontPath = ResourceUtil.getCached("static/fonts/Bernard MT Condensed.ttf", "/root/Nullbot/file/temp/fonts");
        // // 创建 SVG 画布
        // SvgCanvas canvas = SvgCanvas.create(640, 640);
        //
        // // 添加用户头像
        // canvas.image(
        //         0, 0, 640, 640,
        //         Path.of("src/test/testFile/input.jpg"), true
        // );
        //
        // // 添加 RIP 文字 - 确保使用正确的字体名称
        // canvas.text(175, 550, "R.I.P.")
        //         .font("Bernard MT Condensed")
        //         .size(150)
        //         .color("#000000")
        //         // .bold()
        //         .stroke("#FFFFFF", 6);

        Path prts = ResourceUtil.getCached("static/image/inversePRTS.png", "/root/Nullbot/file/temp/fonts");
        // 创建 SVG 画布
        SvgCanvas canvas = SvgCanvas.create(640, 640);
        // 添加用户头像
        canvas.image(
                0, 0, 640, 640, 1,
                Path.of("src/test/testFile/input.jpg"), false
        );
        // 添加 PRTS
        canvas.image(
                0, 0, 640, 640, 1,
                prts, false
        );

        // 使用 resvg 渲染为 PNG
        Path outputPath = Path.of("src/test/testFile/output.png");
        canvas.render(outputPath, "/root/Nullbot/file/temp/fonts");

        // 先保存SVG文件查看内容
        Path svgPath = Path.of("src/test/testFile/output.svg");
        canvas.exportSvg(svgPath);

        // 读取并打印SVG内容
        String svgContent = Files.readString(svgPath);
        System.out.println("生成的SVG内容: ");
        System.out.println(svgContent.substring(0, Math.min(svgContent.length(), 2000)) + "...");

        System.out.println("图片已生成: " + outputPath.toAbsolutePath());
    }

    @Test
    void WebCaptureTest() throws FileNotFoundException {
        // webScreenCapturer.captureFull("https://prts.wiki/w/%E8%8E%B1%E4%BC%8A");

        // webScreenCapturer.captureElement(
        //         "https://prts.wiki/w/%E8%8E%B1%E4%BC%8A",
        //         "#bodyContent",
        //         1000, 5000
        // );

        String base64 = webScreenCapturer.captureElements(
                "https://prts.wiki/w/%E8%8E%B1%E4%BC%8A", 1040, 5000,
                List.of("#bodyContent"),
                List.of(
                        ".backToTop", "#toc", "#rightToc",
                        ".music-btn", "#calc", "#equip-selector",
                        "#干员模型", "#spine-root",
                        "#注释与链接", "#catlinks"
                ),
                List.of(
                        "input[onchange*='switchDisplay第一天赋算法']",
                        "input[onchange*='switchDisplay第一天赋潜能']",
                        "input[onchange*='switchDisplay第二天赋算法']",
                        "input[onchange*='switchDisplay第二天赋潜能']"
                )
        );

        // 解码Base64字符串
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\capture.png")) {
            fos.write(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
