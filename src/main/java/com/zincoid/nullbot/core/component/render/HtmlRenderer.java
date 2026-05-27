package com.zincoid.nullbot.core.component.render;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.util.Base64Util;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlRenderer {

    private static final TemplateEngine ENGINE;
    static {
        StringTemplateResolver resolver = new StringTemplateResolver();
        ENGINE = new TemplateEngine();
        ENGINE.setTemplateResolver(resolver);
    }

    private static final int DRIVER_POOL_SIZE = 2;

    private final ResourceLoader resources;
    private final ChromeDriverFactory driverFactory;
    private BlockingQueue<WebDriver> drivers;

    @PostConstruct
    void init() {
        drivers = new LinkedBlockingQueue<>(DRIVER_POOL_SIZE);
        for (int i = 0; i < DRIVER_POOL_SIZE; i++)
            drivers.offer(driverFactory.createDriver("3840,2160"));
        log.info("▽ [HtmlRenderer] Chrome 驱动已就绪 (PoolSize: {})", DRIVER_POOL_SIZE);
    }

    @PreDestroy
    void destroy() {
        drivers.forEach(WebDriver::quit);
        log.info("▽ [HtmlRenderer] Chrome 驱动已关闭");
    }

    public Template load(String resourcePath) {
        try {
            return new Template(Files.readString(resources.getCache(resourcePath)));
        } catch (Exception e) {
            throw new RuntimeException("模板加载失败: " + resourcePath, e);
        }
    }

    private WebDriver take() {
        try { return drivers.take(); }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待 WebDriver 被中断", e);
        }
    }

    public class Template {

        private final String html;
        private final Map<String, Object> ctx = new HashMap<>();

        private Template(String html) { this.html = html; }

        // ==================================== 链式构建方法 ====================================

        public Template set(String key, String value) { ctx.put(key, value); return this; }
        public Template image(String key, String filePath) {
            File f = new File(filePath);
            if (!f.exists()) throw new RuntimeException("图片文件不存在: " + filePath);
            ctx.put(key, "file://" + f.getAbsolutePath().replace("\\", "/"));
            return this;
        }
        public Template resource(String key, String resourcePath) {
            Path p = resources.getCache(resourcePath);
            return image(key, p.toAbsolutePath().toString());
        }
        public String render() throws Exception { return capture(null); }
        public String render(String cssSelector) throws Exception { return capture(cssSelector); }

        // =================================== 渲染工具方法 ====================================

        private String capture(String cssSelector) throws Exception {
            Context context = new Context();
            context.setVariables(ctx);
            String resolved = ENGINE.process(html, context);
            WebDriver driver = take();
            try {
                Path tmp = Files.createTempFile("render-", ".html");
                try {
                    Files.writeString(tmp, resolved);
                    driver.get("file://" + tmp.toAbsolutePath());
                    new WebDriverWait(driver, Duration.ofSeconds(10))
                            .until(d -> ((JavascriptExecutor) d)
                                    .executeScript("return document.readyState").equals("complete"));

                    AShot ashot = new AShot();
                    ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));

                    BufferedImage img;
                    if (cssSelector != null) {
                        WebElement el = driver.findElement(By.cssSelector(cssSelector));
                        ashot.coordsProvider(new WebDriverCoordsProvider());
                        img = ashot.takeScreenshot(driver, el).getImage();
                    } else {
                        img = ashot.takeScreenshot(driver).getImage();
                    }
                    return Base64Util.from(img);
                } finally {
                    Files.deleteIfExists(tmp);
                }
            } finally {
                drivers.offer(driver);
            }
        }
    }
}
