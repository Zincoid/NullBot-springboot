package com.zincoid.nullbot;

// import jdash.client.GDClient;
// import jdash.common.Length;
// import jdash.common.LevelSearchFilter;
// import jdash.common.LevelSearchMode;
// import jdash.common.entity.GDLevel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NullBotApplicationTests {

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
}
