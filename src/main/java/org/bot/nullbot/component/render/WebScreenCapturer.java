package org.bot.nullbot.component.render;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Component
public class WebScreenCapturer
{
    // 截取整个页面
    public String capturePage(String url) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            // 等待页面加载
            Thread.sleep(2000);
            // 截取整个页面
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            // File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            // screenshot.renameTo(new File(outputPath));
            // System.out.println("截图保存到: " + outputPath);
        } catch (Exception e) {
            throw new RuntimeException("网页截图出错");
        } finally {
            driver.quit();
        }
    }

    // 截取特定元素
    public String captureElement(String url, String cssSelector) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            // 等待页面加载
            Thread.sleep(2000);
            // 定位元素
            WebElement element = driver.findElement(By.cssSelector(cssSelector));
            // 截取元素
            return element.getScreenshotAs(OutputType.BASE64);
            // File screenshot = element.getScreenshotAs(OutputType.FILE);
            // screenshot.renameTo(new File(outputPath));
            // System.out.println("元素截图保存到: " + outputPath);
        } catch (Exception e) {
            throw new RuntimeException("网页截图出错");
        } finally {
            driver.quit();
        }
    }

    // 截取滚动页面
    public String capturePageWithScroll(String url) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            Thread.sleep(2000);
            // 获取页总高度
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long pageHeight = (Long) js.executeScript("return document.body.scrollHeight");
            // 设置窗口高度
            driver.manage().window().setSize(new Dimension(1920, 1080));
            // 滚动截图逻辑
            BufferedImage combinedImage = capturePageWithHeight(driver, pageHeight);
            // 转换 BASE64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(combinedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
            // File outputFile = new File(outputPath);
            // ImageIO.write(combinedImage, "PNG", outputFile);
        } catch (Exception e) {
            throw new RuntimeException("网页截图出错");
        } finally {
            driver.quit();
        }
    }

    // =================== 工具方法 ===================

    private BufferedImage capturePageWithHeight(WebDriver driver, Long pageHeight) throws IOException, InterruptedException {
        BufferedImage combinedImage = new BufferedImage(1920, pageHeight.intValue(), BufferedImage.TYPE_INT_ARGB);
        int scrollHeight = 1080;
        int currentPosition = 0;
        while (currentPosition < pageHeight) {
            // 滚动到当前位置
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, " + currentPosition + ");");
            // 等待滚动完成
            Thread.sleep(500);
            // 截取当前视图
            File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            BufferedImage screenshot = ImageIO.read(tempFile);
            // 实际复制高度
            int heightToCopy = Math.min(scrollHeight, pageHeight.intValue() - currentPosition);
            // 拼接到完整图片
            combinedImage.createGraphics().drawImage(
                    screenshot,
                    0,
                    currentPosition,
                    1920,
                    currentPosition + heightToCopy,
                    0,
                    scrollHeight - heightToCopy,
                    1920,
                    scrollHeight,
                    null);
            currentPosition += heightToCopy;
        }
        return combinedImage;
    }

    // =================== 驱动加载 ===================

    public WebDriver setupDriver() {
        // 自动下载 ChromeDriver
        WebDriverManager.chromedriver().setup();
        // 手动设置 ChromeDriver
        // System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        return new ChromeDriver(options);
    }
}