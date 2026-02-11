package org.bot.nullbot.component.tool;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.NullBotApplication;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class Restarter
{
    private static final String SESSION_NAME = "nullbot";
    private static final String JAR_PATH = "/root/Nullbot/jar/NullBot-springboot-0.0.1-SNAPSHOT.jar";
    private static final String LOG_PATH = "/root/Nullbot/output.log";

    public void restart() {
        log.info("▽ [Restarter] 正在重启应用...");
        NullBotApplication.restart();
    }

    public void restartViaJar() {
        restartViaJar(JAR_PATH);
    }

    public void restartViaJar(String jarPath) {
        log.info("▽ [Restarter] 正在通过JAR文件重启应用...");
        try {
            // 验证 JAR文件
            if (!new File(jarPath).exists()) {
                log.error("▽ [Restarter] JAR文件不存在: {}", jarPath);
                throw new IllegalArgumentException("JAR文件不存在");
            }

            // 构建 重启命令
            // String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String javaBin = "java";
            String javaCommand = String.format("%s -jar %s 2>&1 | tee %s", javaBin, jarPath, LOG_PATH);
            String[] screenCommand = {"screen", "-dmS", SESSION_NAME, "bash", "-c", javaCommand};
            String[] killScreenCmd = {"screen", "-S", SESSION_NAME, "-X", "quit"};

            // 执行 重启命令
            log.info("▽ [Restarter] 将于3秒后重启...");
            Thread.sleep(3000);
            new ProcessBuilder(killScreenCmd).start().waitFor();
            Thread.sleep(1000);
            new ProcessBuilder(screenCommand).directory(new File("/root")).start();
            Thread.sleep(1000);
            System.exit(0);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("▽ [Restarter] 重启过程中出错", e);
        }
    }
}
