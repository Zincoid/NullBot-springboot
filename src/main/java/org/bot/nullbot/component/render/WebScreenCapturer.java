package org.bot.nullbot.component.render;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
public class WebScreenCapturer
{
    @Value("${driver.chrome.auto}")
    private Boolean chromeDriverAuto;
    @Value("${driver.chrome.path}")
    private String chromeDriverPath;

    // =================== 主要方法 ===================

    // 截取多个元素 可忽略元素 可附加点击
    public String capture(String url, int width, int height,
                                  List<String> targetCssSelectors,
                                  List<String> ignoredCssSelectors,
                                  List<String> clickCssSelectors
    ) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            // 设置窗口尺寸
            driver.manage().window().setSize(new Dimension(width, height));
            // 等待页面加载
            Thread.sleep(2000);
            // 定位元素位置
            List<WebElement> targets = targetCssSelectors.stream()
                    .map(selector -> driver.findElement(getBy(selector)))
                    .toList();
            // 点击附加元素
            for(String clickCssSelector : clickCssSelectors) clickElement(driver, clickCssSelector);
            // 隐藏忽略元素
            hideElements(driver, ignoredCssSelectors);
            // 进行元素截图
            AShot ashot = new AShot();
            ashot.shootingStrategy(ShootingStrategies.viewportPasting(1000));
            ashot.coordsProvider(new WebDriverCoordsProvider());
            Screenshot screenshot = ashot.takeScreenshot(driver, targets);
            BufferedImage eleImage = screenshot.getImage();
            // 保存
            // File outputFile = new File(outputPath);
            // ImageIO.write(eleImage, "png", outputFile);
            // BASE64 转换
            return imageToBase64(eleImage);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("网页截图出错");
        } finally {
            driver.quit();
        }
    }

    // =================== 次级方法 ===================

    // 截取完整页面
    public String captureFull(String url, int width, int height) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            // 设置窗口尺寸
            driver.manage().window().setSize(new Dimension(width, height));
            // 等待页面加载
            Thread.sleep(2000);
            // 进行全页截图
            AShot ashot = new AShot();
            ashot.shootingStrategy(ShootingStrategies.viewportPasting(1000));
            Screenshot screenshot = ashot.takeScreenshot(driver);
            BufferedImage fullImage = screenshot.getImage();
            // 保存
            // File outputFile = new File(outputPath);
            // ImageIO.write(fullImage, "png", outputFile);
            // BASE64 转换
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(fullImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("网页截图出错");
        } finally {
            driver.quit();
        }
    }

    // 截取特定元素
    public String captureElement(String url, String cssSelector, int width, int height) {
        WebDriver driver = setupDriver();
        try {
            driver.get(url);
            // 设置窗口尺寸
            driver.manage().window().setSize(new Dimension(width, height));
            // 等待页面加载
            Thread.sleep(2000);
            // 定位元素位置
            WebElement element = driver.findElement(getBy(cssSelector));
            // 进行元素截图
            AShot ashot = new AShot();
            ashot.shootingStrategy(ShootingStrategies.viewportPasting(1000));
            ashot.coordsProvider(new WebDriverCoordsProvider());
            Screenshot screenshot = ashot.takeScreenshot(driver, element);
            BufferedImage eleImage = screenshot.getImage();
            // 保存
            // File outputFile = new File(outputPath);
            // ImageIO.write(eleImage, "png", outputFile);
            // BASE64 转换
            return imageToBase64(eleImage);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("网页截图出错");
        } finally {
            driver.quit();
        }
    }

    // =================== 工具方法 ===================

    private By getBy(String selector) {
        if (selector.startsWith("//") || selector.startsWith(".//") || selector.startsWith("("))
            return By.xpath(selector);
        else
            return By.cssSelector(selector);
    }

    private void hideElements(WebDriver driver, List<String> cssSelectors) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (String selector : cssSelectors) {
            try {
                // 隐藏所有匹配的元素
                js.executeScript(
                        "document.querySelectorAll('" + selector + "').forEach(function(el) {" +
                                "   el.parentNode.removeChild(el);" +
                                "});"
                );
            } catch (Exception e) {
                log.info("[WebScreenCapturer] 隐藏元素未找到: {}", selector);
            }
        }
    }

    private void clickElement(WebDriver driver, String cssSelector) {
        try {
            WebElement element = driver.findElement(getBy(cssSelector));
            // 尝试常规点击
            element.click();
        } catch (NoSuchElementException e) {
            log.info("[WebScreenCapturer] 交互元素未找到: {}", cssSelector);
        } catch (ElementNotInteractableException e) {
            log.info("[WebScreenCapturer] 该元素不可交互: {} 尝试 JavaScript 方法...", cssSelector);
            // 使用 JavaScript 点击
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                WebElement element = driver.findElement(getBy(cssSelector));
                // 先确保元素可见
                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
                Thread.sleep(200);
                // 使用JavaScript点击
                js.executeScript("arguments[0].click();", element);
                log.info("[WebScreenCapturer] JavaScript 成功点击: {}", cssSelector);
            } catch (Exception ex) {
                log.info("[WebScreenCapturer] JavaScript 点击失败: {}", ex.getMessage());
            }
        }
    }

    private static String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("图片转换失败");
        }
    }

    // =================== 驱动加载 ===================

    public WebDriver setupDriver() {
        if (chromeDriverAuto) {
            // 自动下载 ChromeDriver
            WebDriverManager.chromedriver().setup();
        } else {
            // 手动设置 ChromeDriver
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--hide-scrollbars");

        return new ChromeDriver(options);
    }
}