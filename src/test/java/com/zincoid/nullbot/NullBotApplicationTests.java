package com.zincoid.nullbot;

// import jdash.client.GDClient;
// import jdash.common.Length;
// import jdash.common.LevelSearchFilter;
// import jdash.common.LevelSearchMode;
// import jdash.common.entity.GDLevel;
import com.zincoid.nullbot.core.module.render.resvg.SvgRenderer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NullBotApplicationTests {

    @Resource
    private SvgRenderer svgRenderer;

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
    void ThymeleafTest() throws IOException {
        String file = "Y:\\Projects\\IntelliJ IDEA\\Develop\\NullBot-springboot\\src\\test\\file\\uses.png";
        String base64 = svgRenderer.load("uses").number("uses", 123456).render();
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
        }
    }
}
