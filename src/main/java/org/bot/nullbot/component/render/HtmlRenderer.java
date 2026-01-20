package org.bot.nullbot.component.render;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.prop.ChromeProperties;
import org.bot.nullbot.util.Base64Util;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Duration;

@Component
@Slf4j
public class HtmlRenderer
{
    private final ChromeProperties chromeProperties;

    private WebDriver driver;
    private boolean initialized = false;

    public HtmlRenderer(ChromeProperties chromeProperties) {
        this.chromeProperties = chromeProperties;
        initialize();
    }

    // =================== 驱动加载 ===================

    public void initialize() {
        if (initialized) {
            log.info("[HtmlRenderer] Chrome 驱动已初始化过");
            return;
        }

        if (chromeProperties.getDriverAuto()) {
            // 自动下载 ChromeDriver
            WebDriverManager.chromedriver().setup();
        } else {
            // 手动设置 ChromeDriver
            System.setProperty("webdriver.chrome.driver", chromeProperties.getDriverPath());
        }

        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=3840,2160");
        options.addArguments("--hide-scrollbars");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(chromeProperties.getLoadTimeout()));

        this.driver = driver;
        initialized = true;
        log.info("[HtmlRenderer] Chrome 驱动已初始化");
    }

    public void close() {
        if (initialized) {
            driver.quit();
            initialized = false;
            log.info("[HtmlRenderer] Chrome 驱动已关闭");
        } else
            log.info("[HtmlRenderer] Chrome 驱动未初始化");
    }

    // =================== 渲染方法 ===================

    // HTML 字符串渲染
    public String renderFromHtml(String html) throws Exception {
        // 保存临时文件
        File tempFile = File.createTempFile("render-", ".html");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(html);
        }
        // 加载页面文件
        driver.get("file://" + tempFile.getAbsolutePath());
        // 确保内容加载
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")
                .equals("complete")
        );
        // 进行全页截图
        AShot ashot = new AShot();
        ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
        Screenshot screenshot = ashot.takeScreenshot(driver);
        BufferedImage fullImage = screenshot.getImage();
        // 清理临时文件
        tempFile.delete();

        return Base64Util.imageToBase64(fullImage);
    }

    // HTML 页元素渲染
    public String renderElement(String html, String cssSelector) throws Exception {
        // 保存临时文件
        File tempFile = File.createTempFile("render-", ".html");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(html);
        }
        // 加载页面文件
        driver.get("file://" + tempFile.getAbsolutePath());
        // 确保内容加载
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")
                .equals("complete")
        );
        // 查找目标元素
        WebElement element = driver.findElement(By.cssSelector(cssSelector));
        // 进行元素截图
        AShot ashot = new AShot();
        ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
        ashot.coordsProvider(new WebDriverCoordsProvider());
        Screenshot screenshot = ashot.takeScreenshot(driver, element);
        BufferedImage eleImage = screenshot.getImage();
        // 清理临时文件
        tempFile.delete();

        return Base64Util.imageToBase64(eleImage);
    }
}
