package org.bot.nullbot.component.render;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class HtmlRenderer
{
    private WebDriver driver;
    private boolean initialized = false;


    public HtmlRenderer()
    {
        if (chromeDriverAuto) {
            // 自动下载 ChromeDriver
            WebDriverManager.chromedriver().setup();
        } else {
            // 手动设置 ChromeDriver
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        }

        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--hide-scrollbars");

        driver = new ChromeDriver(options);
    }

    public void initialize() {
        if (!initialized) {
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--headless",
                    "--disable-gpu",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--window-size=1920,1080",
                    "--hide-scrollbars"
            );

            // 启用更多功能
            options.addArguments("--remote-allow-origins=*");

            // 设置日志级别
            System.setProperty("webdriver.chrome.silentOutput", "true");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            initialized = true;

            System.out.println("Chrome 驱动已初始化");
        }
    }

    /**
     * 从 HTML 字符串渲染为图片
     */
    public void renderFromHtml(String html, String outputPath) throws Exception {
        initialize();

        // 保存 HTML 到临时文件
        File tempFile = File.createTempFile("render-", ".html");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(html);
        }

        // 加载页面
        driver.get("file://" + tempFile.getAbsolutePath());

        // 等待页面完全加载
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
        driver.manage().window().setSize(
                new Dimension(1200, totalHeight.intValue() + 100)
        );

        // 给一点时间让布局稳定
        Thread.sleep(1000);

        // 截图
        File screenshot = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE);

        // 保存到输出路径
        try (InputStream in = new FileInputStream(screenshot);
             OutputStream out = new FileOutputStream(outputPath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }

        // 清理临时文件
        tempFile.delete();

        System.out.println("图片已生成: " + outputPath);
    }

    /**
     * 关闭驱动
     */
    public void close() {
        if (driver != null) {
            driver.quit();
            initialized = false;
            System.out.println("Chrome 驱动已关闭");
        }
    }

    /**
     * 渲染指定元素
     */
    public void renderElement(String html, String cssSelector, String outputPath)
            throws Exception {
        initialize();

        File tempFile = File.createTempFile("render-", ".html");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(html);
        }

        driver.get("file://" + tempFile.getAbsolutePath());
        Thread.sleep(2000);

        WebElement element = driver.findElement(By.cssSelector(cssSelector));
        File screenshot = element.getScreenshotAs(OutputType.FILE);

        try (InputStream in = new FileInputStream(screenshot);
             OutputStream out = new FileOutputStream(outputPath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }

        tempFile.delete();
        System.out.println("元素截图已生成: " + outputPath);
    }
}
