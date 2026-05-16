package org.bot.nullbot;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
// import jdash.client.GDClient;
// import jdash.common.Length;
// import jdash.common.LevelSearchFilter;
// import jdash.common.LevelSearchMode;
// import jdash.common.entity.GDLevel;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.component.game.handler.TicTacToeMatchHandler;
import org.bot.nullbot.component.game.logic.TicTacToeGameLogic;
import org.bot.nullbot.component.render.HtmlRenderer;
import org.bot.nullbot.component.render.WebScreenCapturer;
import org.bot.nullbot.dispatcher.CommandProcessor;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.info.DuelInfo;
import org.bot.nullbot.entity.svg.SvgCanvas;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NullBotApplicationTests {

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
    @Resource
    private DeepSeekClient deepSeekClient;
    @Resource
    private BotNextInputer botNextInputer;

    @Test
    void regexTest() {
        // String message = "[123][文件[]名称(456)]: 这是一些描述内容";
        // String regex = "\\[\\d+]\\[.+?\\(\\d+\\)]:";
        // Pattern pattern = Pattern.compile(regex);
        // java.util.regex.Matcher matcher = pattern.matcher(message);
        // System.out.println(matcher.find());

        String message = "120";
        String regex = "[1-9]\\d*";
        Pattern pattern = Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        System.out.println(matcher.matches());
    }

    @Test
    void parseTest() {
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
               new GroupMessageEvent(), commandType, commandParameters,
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
        // Path fontPath = ResourceUtil.getCached("static/font/Bernard MT Condensed.ttf", "/root/Nullbot/file/temp/font");
        // // 创建 SVG 画布
        // SvgCanvas canvas = SvgCanvas.create(640, 640);
        //
        // // 添加用户头像
        // canvas.image(
        //         0, 0, 640, 640,
        //         Path.of("src/test/file/input.jpg"), true
        // );
        //
        // // 添加 RIP 文字 - 确保使用正确的字体名称
        // canvas.text(175, 550, "R.I.P.")
        //         .font("Bernard MT Condensed")
        //         .size(150)
        //         .color("#000000")
        //         // .bold()
        //         .stroke("#FFFFFF", 6);

        Path prts = ResourceUtil.getCached("static/image/invsPRTS.png", "/root/Nullbot/file/temp/font");
        // 创建 SVG 画布
        SvgCanvas canvas = SvgCanvas.create(640, 640);
        // 添加用户头像
        canvas.image(
                0, 0, 640, 640, 1,
                Path.of("src/test/file/input.jpg"), false
        );
        // 添加 PRTS
        canvas.image(
                0, 0, 640, 640, 1,
                prts, false
        );

        // 使用 resvg 渲染为 PNG
        Path outputPath = Path.of("src/test/file/output.png");
        canvas.render(outputPath, "/root/Nullbot/file/temp/font");

        // 先保存SVG文件查看内容
        Path svgPath = Path.of("src/test/file/output.svg");
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

        // String base64 = webScreenCapturer.capture(
        //         "https://prts.wiki/w/" + operator, 1024, 5120,
        //         List.of("//table[.//th//b[contains(text(),'人员档案')]]"),
        //         List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
        //         List.of("//table[.//th//b[contains(.,'人员档案')]]//button[contains(@class,'mw-collapsible-toggle')]")
        // );

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

        // String weapon = "领航者";
        // String base64 = webScreenCapturer.capture(
        //         "https://end.canmoe.com/", 1536, 5120,
        //         List.of("//section[contains(@class,'panel')][.//h2[contains(text(),'方案推荐列表')]]"),
        //         List.of(".ghost-button"),
        //         List.of(
        //                 "#app > div > div > div.notice-footer > div.about-actions > button",
        //                 String.format(
        //                         "//div[@class='weapon-name']" +
        //                                 "/div[@class='weapon-title' and text()='%s']" +
        //                                 "/ancestor::div[contains(@class,'weapon-item')]",
        //                         weapon
        //                 ),
        //                 "//button[contains(.,'收起其他方案')]"
        //         )
        // );

        String base64 = webScreenCapturer.captureFull("https://www.baidu.com/", 1920, 1080);

        // Base64 解码
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\file\\captured.png")) {
            fos.write(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void HtmlRenderTest() throws Exception {
        // String htmlPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\main\\resources\\static\\html\\symmetry.html";
        // String imagePath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\file\\neuro.png";
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

        // String htmlPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\file\\meme_template.html";
        // String backgroundPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\file\\input.jpg";
        // String imagePath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\file\\neuro.png";
        // Map<String, String> variables = new HashMap<>();
        // variables.put("text", "我去我去我去我去我去我去我去我去我去我去我去我去我去");
        // Map<String, String> images = new HashMap<>();
        // images.put("background", backgroundPath);
        // images.put("image", imagePath);
        // String html = HtmlTemplateUtil.loadTemplate(htmlPath);
        // html = HtmlTemplateUtil.replaceVariables(html, variables);
        // html = HtmlTemplateUtil.replaceImages(html, images);
        // String base64 = htmlRenderer.renderElement(html, "#wrap");

        String htmlPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\main\\resources\\static\\html\\pucci.html";
        String backgroundPath = "C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\main\\resources\\static\\image\\pucci.png";
        Map<String, String> variables = new HashMap<>();
        variables.put("text1", "普奇！！回答我！");
        variables.put("text2", "为什么你要加速时间！！");
        variables.put("text3", "我想玩《GTA6》");
        Map<String, String> images = new HashMap<>();
        images.put("background", backgroundPath);
        String html = HtmlTemplateUtil.loadTemplate(htmlPath);
        html = HtmlTemplateUtil.replaceVariables(html, variables);
        html = HtmlTemplateUtil.replaceImages(html, images);
        String base64 = htmlRenderer.renderElement(html, "#wrap");

        // Base64 解码
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\file\\rendered.png")) {
            fos.write(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void GDTest() {
        // Scanner scanner = new Scanner(System.in);
        // while (true)
        // {
        //     System.out.println("输入查询关键字: ");
        //     List<GDLevel> levels;
        //     try {
        //         levels = GDClient.create()
        //                 .searchLevels(
        //                         LevelSearchMode.SEARCH,
        //                         scanner.nextLine(),
        //                         LevelSearchFilter.create()
        //                                 .withToggles(EnumSet.of(LevelSearchFilter.Toggle.STAR)),
        //                         1
        //                 )
        //                 .collectList().block();
        //     } catch (Exception e) {
        //         System.out.println("ERROR.");
        //         continue;
        //     }
        //
        //     for (GDLevel level : levels) {
        //         System.out.println("[%s] %s by %s".formatted(level.difficulty(), level.name(), level.creatorName().isPresent() ? level.creatorName().get() : "-"));
        //     }
        //
        //     System.out.println("输入查询序号: ");
        //     System.out.println(levels.get(scanner.nextInt() - 1));
        //     scanner.nextLine();
        // }
    }

    @Test
    void ChatTest() throws Exception {
        String response = deepSeekClient.chatSingle("出一道单选题并给出题目和答案,问题主题:%s,生成种子:%s (注:将答案用{}包围放在开头,例如{正确选项字母},无需答案解析)"
                .formatted("二次元", UUID.randomUUID()), true, 2500);
        System.out.println(response);
    }

    @Test
    void NextInputerTest() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                botNextInputer.response(0L, 1L, "test");
                botNextInputer.response(0L, 0L, "A");
                botNextInputer.response(0L, 2L, "11");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        List<Pair<Long, String>> inputs = botNextInputer.request(BniMode.PS, 0L, "[1-9]\\d*", 5);
        System.out.println("已响应: " + inputs);
    }

    @Test
    void RandomDuelTest() throws InterruptedException {
        while (true) {
            DuelInfo duelInfo = null;
            try {
                duelInfo = DuelUtil.getRandom("Y:\\Materials\\BOT\\bet\\usable\\duel\\test.csv");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            System.out.println(duelInfo);
            Thread.sleep(1000);
        }
    }
}
