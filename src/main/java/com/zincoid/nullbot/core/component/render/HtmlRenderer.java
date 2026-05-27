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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlRenderer {

    private static final int DRIVER_POOL_SIZE = 2;
    private static final TemplateEngine ENGINE;

    static {
        StringTemplateResolver resolver = new StringTemplateResolver();
        ENGINE = new TemplateEngine();
        ENGINE.setTemplateResolver(resolver);
    }

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

    /** 文件路径 → file:// URL */
    public String toUrl(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) throw new RuntimeException("图片文件不存在: " + filePath);
        return "file://" + file.getAbsolutePath().replace("\\", "/");
    }

    /** 资源路径 → file:// URL (从缓存加载) */
    public String resource(String resourcePath) {
        Path path = resources.getCache(resourcePath);
        return toUrl(path.toAbsolutePath().toString());
    }

    /** 渲染资源模板 → 全页截图 */
    public String render(String resourcePath, Map<String, Object> context) throws Exception {
        String html = Files.readString(resources.getCache(resourcePath));
        return capture(apply(html, context), null);
    }

    /** 渲染资源模板 → 元素截图 */
    public String render(String resourcePath, Map<String, Object> context, String cssSelector) throws Exception {
        String html = Files.readString(resources.getCache(resourcePath));
        return capture(apply(html, context), cssSelector);
    }

    private String apply(String template, Map<String, Object> context) {
        Context ctx = new Context();
        ctx.setVariables(context);
        return ENGINE.process(template, ctx);
    }

    private String capture(String html, String cssSelector) throws Exception {
        WebDriver driver = take();
        try {
            Path tempFile = Files.createTempFile("render-", ".html");
            try {
                Files.writeString(tempFile, html);
                driver.get("file://" + tempFile.toAbsolutePath());

                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(d -> ((JavascriptExecutor) d)
                                .executeScript("return document.readyState")
                                .equals("complete"));

                AShot ashot = new AShot();
                ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));

                BufferedImage image;
                if (cssSelector != null) {
                    WebElement element = driver.findElement(By.cssSelector(cssSelector));
                    ashot.coordsProvider(new WebDriverCoordsProvider());
                    image = ashot.takeScreenshot(driver, element).getImage();
                } else {
                    image = ashot.takeScreenshot(driver).getImage();
                }
                return Base64Util.from(image);
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } finally {
            drivers.offer(driver);
        }
    }

    private WebDriver take() {
        try {
            return drivers.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待 WebDriver 被中断", e);
        }
    }
}
