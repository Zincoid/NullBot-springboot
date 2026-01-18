package org.bot.nullbot.component.render;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
import javax.imageio.ImageIO;
import java.awt.*;
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
        // 获取浏览器实际可视区域高度
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Long viewportHeight = (Long) js.executeScript(
                "return window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;"
        );
        Long viewportWidth = (Long) js.executeScript(
                "return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;"
        );
        int vHeight = viewportHeight.intValue();
        int vWidth = viewportWidth.intValue();
        // 创建完整页图像
        BufferedImage combinedImage = new BufferedImage(vWidth, pageHeight.intValue(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combinedImage.createGraphics();
        int currentPosition = 0;
        // 隐藏滚动条避免干扰
        js.executeScript("document.documentElement.style.overflow = 'hidden';");
        while (currentPosition < pageHeight) {
            // 使用平滑滚动
            js.executeScript("window.scrollTo({top: " + currentPosition + ", behavior: 'smooth'});");
            // 等待滚动停止
            Thread.sleep(1000);
            // 确保滚动正确
            Long currentScroll = (Long) js.executeScript("return window.pageYOffset;");
            if (currentScroll.intValue() != currentPosition) {
                // 如果没滚动到位，调整并重试
                js.executeScript("window.scrollTo(0, " + currentPosition + ");");
                Thread.sleep(800);
            }
            // 截取当前视图
            File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            BufferedImage screenshot = ImageIO.read(tempFile);
            // 计算剩余高度
            int remainingHeight = pageHeight.intValue() - currentPosition;
            int currentViewportHeight = Math.min(vHeight, remainingHeight);
            // 绘制到总图上
            if (currentViewportHeight < vHeight) {
                // 最后一屏，从截图顶部截取相应高度
                BufferedImage croppedScreenshot = screenshot.getSubimage(0, 0, vWidth, currentViewportHeight);
                g2d.drawImage(croppedScreenshot, 0, currentPosition, null);
            } else {
                // 完整截图
                g2d.drawImage(screenshot, 0, currentPosition, null);
            }
            // 更新位置（使用实际滚动高度 避免累积误差）
            currentPosition += currentViewportHeight;
        }
        // 恢复滚动条
        js.executeScript("document.documentElement.style.overflow = 'auto';");
        g2d.dispose();
        return combinedImage;
    }

    // =================== 驱动加载 ===================

    public WebDriver setupDriver() {
        // 自动下载 ChromeDriver
        // WebDriverManager.chromedriver().setup();
        // 手动设置 ChromeDriver
        System.setProperty("webdriver.chrome.driver", "/root/Nullbot/file/driver/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        return new ChromeDriver(options);
    }
}