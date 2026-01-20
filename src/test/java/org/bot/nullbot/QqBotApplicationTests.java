package org.bot.nullbot;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import org.bot.nullbot.component.game.handler.TicTacToeMatchHandler;
import org.bot.nullbot.component.game.logic.TicTacToeGameLogic;
import org.bot.nullbot.component.render.HtmlRenderer;
import org.bot.nullbot.component.render.WebScreenCapturer;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.svg.SvgCanvas;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.HtmlTemplateUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.bot.nullbot.util.ResourceUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QqBotApplicationTests
{
    @Value("${nullbot.bot-id}")
    private Long botId;
    @Resource
    private BotContainer botContainer;
    @Resource
    private CommandProcessor commandProcessor;
    @Resource
    Matcher matcher;
    @Resource
    TicTacToeMatchHandler ticTacToeMatchHandler;
    @Resource
    TicTacToeGameLogic ticTacToeGameLogic;
    @Resource
    WebScreenCapturer webScreenCapturer;
    @Resource
    HtmlRenderer htmlRenderer;

    @Test
    void parseTest() throws IOException {
        Bot bot = botContainer.robots.get(botId);
        System.out.println(bot.getStrangerInfo(2660181154L, true).getData().getNickname());
        System.out.println(MessageParseUtil.parseRawSaying(bot, "[CQ:at,qq=2660181154] 你好！"));
    }

    @Test
    void fileTest() throws IOException {
        String root = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\main";
        System.out.println(FileUtil.getFolderTreeString(root, 0));
    }

   @Test
   void commandTest() throws Exception {
       String commandType = "help";
       List<String> commandParameters =  new ArrayList<>();
       commandProcessor.processTest(new CommandEvent<>(
               commandType, commandParameters, new GroupMessageEvent(),
               false, true)
       );

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
       //         System.out.println(ticTacToeGameLogic.place(0L, i, j).getInfo());
       //         current = false;
       //     }else{
       //         System.out.println(ticTacToeGameLogic.place(1L, i, j).getInfo());
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
    void WebCaptureTest() {
        String operator = "莱伊";

        // String base64 = webScreenCapturer.captureFull(
        //         "https://prts.wiki/w/" + operator, 1920, 1080
        // );

        // String base64 = webScreenCapturer.captureElement(
        //         "https://prts.wiki/w/" + operator,
        //         "#bodyContent",
        //         1000, 5000
        // );

        // String base64 = webScreenCapturer.capture(
        //         "https://prts.wiki/w/" + operator, 1024, 5120,
        //         List.of("#bodyContent"),
        //         List.of(
        //                 ".backToTop", "#toc", "#rightToc",
        //                 ".music-btn", "#calc", "#equip-selector",
        //                 "#干员模型", "#敌人模型", "#spine-root",
        //                 "#注释与链接", "#catlinks"
        //         ),
        //         List.of(
        //                 "input[onchange*='switchDisplay第一天赋算法']",
        //                 "input[onchange*='switchDisplay第一天赋潜能']",
        //                 "input[onchange*='switchDisplay第二天赋算法']",
        //                 "input[onchange*='switchDisplay第二天赋潜能']"
        //         )
        // );

        // String base64 = webScreenCapturer.capture(
        //         "https://prts.wiki/w/" + operator, 1024, 5120,
        //         List.of("#voice-table-root"),
        //         List.of(".backToTop", "#rightToc", ".z-1.float-right.select-none"),
        //         List.of("a[class*='z-1 float-right select-none']")
        // );

        String base64 = webScreenCapturer.capture(
                "https://prts.wiki/w/" + operator, 1024, 5120,
                List.of("//table[.//th//b[contains(text(),'人员档案')]]"),
                List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
                List.of("//table[.//th//b[contains(.,'人员档案')]]//button[contains(@class,'mw-collapsible-toggle')]")
        );

        // String base64 = webScreenCapturer.capture(
        //         "https://prts.wiki/w/" + operator, 1024, 5120,
        //         List.of("//table[.//th//b[contains(text(),'干员密录')]]"),
        //         List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
        //         List.of("//table[.//th//b[contains(.,'干员密录')]]//button[contains(@class,'mw-collapsible-toggle')]")
        // );

        // String base64 = webScreenCapturer.capture(
        //         "https://prts.wiki/w/" + operator, 1024, 5120,
        //         List.of("//table[.//th//b[contains(text(),'悖论模拟')]]"),
        //         List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
        //         List.of("//table[.//th//b[contains(.,'悖论模拟')]]//button[contains(@class,'mw-collapsible-toggle')]")
        // );

        // Base64 解码
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\captured.png")) {
            fos.write(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void HtmlRenderTest() throws Exception {
        // String htmlPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\main\\resources\\static\\html\\symmetry.html";
        // String imagePath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\neuro.png";
        // Map<String, String> variables = new HashMap<>();
        // variables.put("mode", "right");
        // Map<String, String> images = new HashMap<>();
        // images.put("image", imagePath);
        // String html = HtmlTemplateUtil.loadTemplate(htmlPath);
        // html = HtmlTemplateUtil.replaceVariables(html, variables);
        // html = HtmlTemplateUtil.replaceImages(html, images);
        // String base64 = htmlRenderer.renderElement(html, "#mirrorContainer");

        // String htmlPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\main\\resources\\static\\html\\5000choyen.html";
        // Map<String, String> variables = new HashMap<>();
        // variables.put("topText", "我去");
        // variables.put("bottomText", "不早说");
        // String html = HtmlTemplateUtil.loadTemplate(htmlPath);
        // html = HtmlTemplateUtil.replaceVariables(html, variables);
        // String base64 = htmlRenderer.renderElement(html, "#templateContainer");

        String htmlPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\meme_template.html";
        String backgroundPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\input.jpg";
        String imagePath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\neuro.png";
        Map<String, String> variables = new HashMap<>();
        variables.put("text", "我去我去我去我去我去我去我去我去我去我去我去我去我去");
        Map<String, String> images = new HashMap<>();
        images.put("image", imagePath);
        Map<String, String> imagesBase64 = new HashMap<>();
        imagesBase64.put("background", backgroundPath);
        String html = HtmlTemplateUtil.loadTemplate(htmlPath);
        html = HtmlTemplateUtil.replaceVariables(html, variables);
        html = HtmlTemplateUtil.replaceImages(html, images);
        html = HtmlTemplateUtil.replaceImagesBase64(html, imagesBase64);
        String base64 = htmlRenderer.renderElement(html, "#wrap");

        // Base64 解码
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\rendered.png")) {
            fos.write(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
