package org.bot.nullbot.component.render;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.prop.ChromeProperties;
import org.bot.nullbot.util.Base64Util;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class WebScreenCapturer
{
    private final Boolean driverAuto;  // 自动获取驱动
    private final String driverPath;  // 手动指定驱动路径
    private final int maxRetries;  // 最大重试次数
    private final long loadTimeout;  // 页面加载超时 (Sec)

    public WebScreenCapturer(ChromeProperties props) {
        driverAuto = props.getDriverAuto();
        driverPath = props.getDriverPath();
        maxRetries = props.getMaxRetries();
        loadTimeout = props.getLoadTimeout();
    }

    // =================== 主要方法 ===================

    // 截取多个元素 可忽略元素 可附加点击
    public String capture(String url, int width, int height,
                          List<String> targetCssSelectors,
                          List<String> ignoredCssSelectors,
                          List<String> clickCssSelectors
    ) {
        int retryCount = 0;

        while (retryCount < maxRetries) {
            WebDriver driver = setupDriver();
            try {
                driver.get(url);
                // 设置窗口尺寸
                driver.manage().window().setSize(new Dimension(width, height));
                // 等待页面加载
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")
                        .equals("complete")
                );
                Thread.sleep(200);  // 某些网页加载慢再延迟一会
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
                ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
                ashot.coordsProvider(new WebDriverCoordsProvider());
                Screenshot screenshot = ashot.takeScreenshot(driver, targets);
                BufferedImage eleImage = screenshot.getImage();
                // 保存本地文件
                // File outputFile = new File(outputPath);
                // ImageIO.write(eleImage, "png", outputFile);
                // BASE64 转换
                return Base64Util.imageToBase64(eleImage);

            } catch (TimeoutException e) {
                retryCount++;
                log.info("▽ [WebScreenCapturer] 页面访问超时: {} Times", retryCount);
            } catch (NoSuchElementException e) {
                throw new RuntimeException("未找到页元素");
            } catch (Exception e) {
                throw new RuntimeException("未知截图错误", e);
            } finally {
                driver.quit();
            }
        }

        throw new RuntimeException("网页访问失败");
    }

    // =================== 次级方法 ===================

    // 截取完整页面
    public String captureFull(String url, int width, int height) {
        int retryCount = 0;

        while (retryCount < maxRetries) {
            WebDriver driver = setupDriver();
            try {
                driver.get(url);
                // 设置窗口尺寸
                driver.manage().window().setSize(new Dimension(width, height));
                // 等待页面加载
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")
                        .equals("complete")
                );
                Thread.sleep(200);  // 某些网页加载慢再延迟一会
                // 进行全页截图
                AShot ashot = new AShot();
                ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
                Screenshot screenshot = ashot.takeScreenshot(driver);
                BufferedImage fullImage = screenshot.getImage();
                // 保存本地文件
                // File outputFile = new File(outputPath);
                // ImageIO.write(fullImage, "png", outputFile);
                // BASE64 转换
                return Base64Util.imageToBase64(fullImage);

            } catch (TimeoutException e) {
                retryCount++;
                log.info("▽ [WebScreenCapturer] 页面访问超时: {} Times", retryCount);
            } catch (Exception e) {
                throw new RuntimeException("未知截图错误", e);
            } finally {
                driver.quit();
            }
        }

        throw new RuntimeException("网页访问失败");
    }

    // 截取特定元素
    public String captureElement(String url, String cssSelector, int width, int height) {
        int retryCount = 0;

        while (retryCount < maxRetries) {
            WebDriver driver = setupDriver();
            try {
                driver.get(url);
                // 设置窗口尺寸
                driver.manage().window().setSize(new Dimension(width, height));
                // 等待页面加载
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")
                        .equals("complete")
                );
                Thread.sleep(200);  // 某些网页加载慢再延迟一会
                // 定位元素位置
                WebElement element = driver.findElement(getBy(cssSelector));
                // 进行元素截图
                AShot ashot = new AShot();
                ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
                ashot.coordsProvider(new WebDriverCoordsProvider());
                Screenshot screenshot = ashot.takeScreenshot(driver, element);
                BufferedImage eleImage = screenshot.getImage();
                // 保存本地文件
                // File outputFile = new File(outputPath);
                // ImageIO.write(eleImage, "png", outputFile);
                // BASE64 转换
                return Base64Util.imageToBase64(eleImage);

            } catch (TimeoutException e) {
                retryCount++;
                log.info("▽ [WebScreenCapturer] 页面访问超时: {} Times", retryCount);
            } catch (NoSuchElementException e) {
                throw new RuntimeException("未找到页元素");
            } catch (Exception e) {
                throw new RuntimeException("未知截图错误", e);
            } finally {
                driver.quit();
            }
        }

        throw new RuntimeException("网页访问失败");
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
                log.info("▽ [WebScreenCapturer] 隐藏元素未找到: {}", selector);
            }
        }
    }

    private void clickElement(WebDriver driver, String cssSelector) {
        try {
            WebElement element = driver.findElement(getBy(cssSelector));
            // 尝试常规点击
            element.click();
        } catch (NoSuchElementException e) {
            log.info("▽ [WebScreenCapturer] 交互元素未找到: {}", cssSelector);
        } catch (ElementNotInteractableException e) {
            log.info("▽ [WebScreenCapturer] 该元素不可交互: {} 尝试 JavaScript 方法...", cssSelector);
            // 使用 JavaScript 点击
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                WebElement element = driver.findElement(getBy(cssSelector));
                // 先确保元素可见
                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
                Thread.sleep(200);
                // 使用JavaScript点击
                js.executeScript("arguments[0].click();", element);
                log.info("▽ [WebScreenCapturer] JavaScript 成功点击: {}", cssSelector);
            } catch (Exception ex) {
                log.info("▽ [WebScreenCapturer] JavaScript 点击失败: {}", ex.getMessage());
            }
        }
    }

    // =================== 驱动加载 ===================

    public WebDriver setupDriver() {
        if (driverAuto) {
            // 自动下载 ChromeDriver
            WebDriverManager.chromedriver().setup();
        } else {
            // 手动设置 ChromeDriver
            System.setProperty("webdriver.chrome.driver", driverPath);
        }

        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--hide-scrollbars");
        options.addArguments("--lang=zh-CN");
        options.addArguments("--accept-lang=zh-CN,zh");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(loadTimeout));

        return driver;
    }
}