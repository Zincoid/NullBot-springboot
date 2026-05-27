package com.zincoid.nullbot.core.component.render;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.util.Base64Util;
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

    private final ResourceLoader resources;
    private final ChromeDriverFactory driverFactory;

    public Template load(String resourcePath) {
        try {
            return new Template(Files.readString(resources.getCache(resourcePath)));
        } catch (Exception e) {
            throw new RuntimeException("模板加载失败: " + resourcePath, e);
        }
    }

    public class Template {

        private final String html;
        private final Map<String, Object> ctx = new HashMap<>();

        private Template(String html) { this.html = html; }

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

        private String capture(String cssSelector) throws Exception {
            Context context = new Context();
            context.setVariables(ctx);
            String resolved = ENGINE.process(html, context);

            WebDriver driver = driverFactory.createDriver("3840,2160");
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
                driver.quit();
            }
        }
    }
}
