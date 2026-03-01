package org.bot.nullbot.component.tool;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.NullBotApplication;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@Slf4j
public class Restarter
{
    private static final String SCREEN_SESSION_NAME = "nullbot";
    private static final String DEFAULT_JAR_PATH = "/root/Nullbot/jar/NullBot-springboot-0.0.1-SNAPSHOT.jar";
    private static final String JAVA_BIN = "java";

    public void restart() {
        log.info("▽ [Restarter] 正在重启应用...");
        NullBotApplication.restart();
    }

    public void restartViaJar() {
        log.info("▽ [Restarter] 通过默认JAR路径重启应用...");
        restartViaJar(DEFAULT_JAR_PATH);
    }

    public void restartViaJar(String jarPath) {
        log.info("▽ [Restarter] 正在通过JAR文件重启应用...");
        try {
            // 验证 JAR文件
            if (!new File(jarPath).exists()) {
                log.error("▽ [Restarter] JAR文件不存在: {}", jarPath);
                throw new IllegalArgumentException("JAR文件不存在");
            }

            // 构建 启动命令
            String javaCmd = "%s -jar %s".formatted(JAVA_BIN, jarPath);
            String[] runScreenCmd = {"screen", "-dmS", SCREEN_SESSION_NAME, "bash", "-c", javaCmd};

            // 启动 新的进程
            log.info("▽ [Restarter] 将于3秒后重启...");
            Thread.sleep(3000);
            if (new ProcessBuilder(runScreenCmd).start().waitFor() == 0) {
                log.info("▽ [Restarter] 新进程启动成功");
            } else {
                log.info("▽ [Restarter] 新进程启动失败");
                throw new RuntimeException("新进程启动失败");
            }

            // 退出 当前进程
            log.info("▽ [Restarter] 将于3秒后退出...");
            Thread.sleep(3000);
            System.exit(0);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("▽ [Restarter] 重启时捕获未知非运行异常", e);
        }
    }
}
