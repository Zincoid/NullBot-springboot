package org.bot.nullbot.component.render;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.prop.ChromeProperties;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.stereotype.Component;

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
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--hide-scrollbars");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(chromeProperties.getLoadTimeout()));

        this.driver = driver;
        this.initialized = true;
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
    public String renderFromHtml(String html, String outputPath) throws Exception {
        // 保存临时文件
        File tempFile = File.createTempFile("render-", ".html");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(html);
        }
        // 等待加载页面
        driver.get("file://" + tempFile.getAbsolutePath());
        Thread.sleep(2000);
        // 执行 JavaScript 确保所有内容已加载
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("return document.readyState").equals("complete");
        // 计算页面高度
        Long totalHeight = (Long) js.executeScript(
                "return Math.max(" +
                        "document.body.scrollHeight, " +
                        "document.documentElement.scrollHeight, " +
                        "document.body.offsetHeight, " +
                        "document.documentElement.offsetHeight" +
                        ");"
        );
        // 调整窗口大小
        driver.manage().window().setSize(new Dimension(1200, totalHeight.intValue() + 100));
        // 等待布局稳定
        Thread.sleep(1000);
        // BASE64 截图
        String base64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        // 清理临时文件
        tempFile.delete();

        return base64;
    }

    // HTML 页元素渲染
    public String renderElement(String html, String cssSelector, String outputPath) throws Exception {
        // 保存临时文件
        File tempFile = File.createTempFile("render-", ".html");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(html);
        }
        // 等待加载页面
        driver.get("file://" + tempFile.getAbsolutePath());
        Thread.sleep(2000);
        // 查找目标元素
        WebElement element = driver.findElement(By.cssSelector(cssSelector));
        // BASE64 截图
        String base64 = element.getScreenshotAs(OutputType.BASE64);
        // 清理临时文件
        tempFile.delete();

        return base64;
    }
}
