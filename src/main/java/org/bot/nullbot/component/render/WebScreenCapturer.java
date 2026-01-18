package org.bot.nullbot.component.render;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WebScreenCapturer
{
    @Value("${driver.chrome-driver-path}")
    private String chromeDriverPath;

    // 截取完整页面
    public String captureFull(String url) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            // 设置窗口尺寸
            driver.manage().window().setSize(new Dimension(1920, 1080));
            // 等待页面加载
            Thread.sleep(2000);
            // 进行全页截图
            AShot ashot = new AShot();
            ashot.shootingStrategy(ShootingStrategies.viewportPasting(1000));
            Screenshot screenshot = ashot.takeScreenshot(driver);
            BufferedImage fullImage = screenshot.getImage();

            // 测试
            // File outputFile = new File("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\capture.png");
            // ImageIO.write(fullImage, "png", outputFile);

            // 保存
            // File outputFile = new File(outputPath);
            // ImageIO.write(fullImage, "png", outputFile);

            // BASE64 转换
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(fullImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("网页截图出错: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    // 截取特定元素
    public String captureElement(String url, String cssSelector) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            // 设置窗口尺寸
            driver.manage().window().setSize(new Dimension(1920, 1080));
            // 等待页面加载
            Thread.sleep(2000);
            // 定位元素位置
            WebElement element = driver.findElement(By.cssSelector(cssSelector));
            // 进行元素截图
            AShot ashot = new AShot();
            ashot.shootingStrategy(ShootingStrategies.viewportPasting(1000));
            ashot.coordsProvider(new WebDriverCoordsProvider());
            Screenshot screenshot = ashot.takeScreenshot(driver, element);
            BufferedImage eleImage = screenshot.getImage();

            // 测试
            // File outputFile = new File("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\capture.png");
            // ImageIO.write(eleImage, "png", outputFile);

            // 保存
            // File outputFile = new File(outputPath);
            // ImageIO.write(eleImage, "png", outputFile);

            // BASE64 转换
            return imageToBase64(eleImage);
        } catch (Exception e) {
            throw new RuntimeException("网页截图出错: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    // 截取多个元素并可忽略
    public String captureElements(String url, List<String> targetCssSelectors, List<String> ignoredCssSelectors) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            // 设置窗口尺寸
            driver.manage().window().setSize(new Dimension(1920, 1080));
            // 等待页面加载
            Thread.sleep(2000);
            // 定位元素位置
            List<WebElement> targets = targetCssSelectors.stream()
                    .map(selector -> driver.findElement(By.cssSelector(selector)))
                    .toList();
            Set<By> ignores = ignoredCssSelectors.stream()
                    .map(By::cssSelector)
                    .collect(Collectors.toSet());
            // 进行元素截图
            AShot ashot = new AShot();
            ashot.shootingStrategy(ShootingStrategies.viewportPasting(1000));
            ashot.coordsProvider(new WebDriverCoordsProvider());
            ashot.ignoredElements(ignores);
            Screenshot screenshot = ashot.takeScreenshot(driver, targets);
            BufferedImage eleImage = screenshot.getImage();

            // 测试
            File outputFile = new File("C:\\Users\\Zincoid\\IdeaProjects\\NullBot-springboot\\src\\test\\testFile\\capture.png");
            ImageIO.write(eleImage, "png", outputFile);

            // 保存
            // File outputFile = new File(outputPath);
            // ImageIO.write(eleImage, "png", outputFile);

            // BASE64 转换
            return imageToBase64(eleImage);
        } catch (Exception e) {
            throw new RuntimeException("网页截图出错: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    // =================== 工具方法 ===================

    private static String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("图片转换失败", e);
        }
    }

    // =================== 驱动加载 ===================

    public WebDriver setupDriver() {
        // 自动下载 ChromeDriver
        // WebDriverManager.chromedriver().setup();
        // 手动设置 ChromeDriver
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--hide-scrollbars");

        return new ChromeDriver(options);
    }
}