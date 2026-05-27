package com.zincoid.nullbot.core.component.render;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.properties.ChromeProperties;
import com.zincoid.nullbot.core.util.Base64Util;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

@Slf4j
@Component
public class WebScreenCapturer {

    private static final int POOL_SIZE = 1;

    private final ChromeDriverFactory driverFactory;
    private final int maxRetries;
    private BlockingQueue<WebDriver> drivers;

    public WebScreenCapturer(ChromeDriverFactory driverFactory, ChromeProperties chromeProperties) {
        this.driverFactory = driverFactory;
        this.maxRetries = chromeProperties.getMaxRetries();
    }

    @PostConstruct
    void init() {
        drivers = new LinkedBlockingQueue<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++)
            drivers.offer(driverFactory.createDriver("1920,1080"));
        log.info("▽ [WebScreenCapturer] Chrome 驱动已就绪 (PoolSize: {})", POOL_SIZE);
    }

    @PreDestroy
    void destroy() {
        drivers.forEach(WebDriver::quit);
        log.info("▽ [WebScreenCapturer] Chrome 驱动已关闭");
    }

    /** 截取多个元素，支持忽略和点击元素 */
    public String capture(String url, int width, int height,
                          List<String> targetSelectors,
                          List<String> ignoreSelectors,
                          List<String> clickSelectors) {
        WebDriver driver = take();
        try {
            return withRetry(driver, () -> {
                navigate(driver, url, width, height);

                List<WebElement> targets = targetSelectors.stream()
                        .map(s -> driver.findElement(by(s)))
                        .toList();
                for (String sel : clickSelectors) clickElement(driver, sel);
                hideElements(driver, ignoreSelectors);

                AShot ashot = new AShot();
                ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
                ashot.coordsProvider(new WebDriverCoordsProvider());
                return ashot.takeScreenshot(driver, targets).getImage();
            });
        } finally {
            drivers.offer(driver);
        }
    }

    /** 截取完整页面 */
    public String captureFull(String url, int width, int height) {
        WebDriver driver = take();
        try {
            return withRetry(driver, () -> {
                navigate(driver, url, width, height);

                AShot ashot = new AShot();
                ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
                return ashot.takeScreenshot(driver).getImage();
            });
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

    private void navigate(WebDriver driver, String url, int width, int height) {
        driver.get(url);
        driver.manage().window().setSize(new Dimension(width, height));
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState")
                        .equals("complete"));
    }

    private String withRetry(WebDriver driver, Supplier<BufferedImage> action) {
        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                return Base64Util.from(action.get());
            } catch (TimeoutException e) {
                log.info("▽ [WebScreenCapturer] 页面访问超时: {} Times", retry + 1);
            }
        }
        throw new RuntimeException("网页访问失败");
    }

    private By by(String selector) {
        return (selector.startsWith("//") || selector.startsWith(".//") || selector.startsWith("("))
                ? By.xpath(selector) : By.cssSelector(selector);
    }

    private void hideElements(WebDriver driver, List<String> selectors) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (String sel : selectors) {
            try {
                js.executeScript("document.querySelectorAll('" + sel + "').forEach(el => el.remove());");
            } catch (Exception e) {
                log.info("▽ [WebScreenCapturer] 隐藏元素未找到: {}", sel);
            }
        }
    }

    private void clickElement(WebDriver driver, String selector) {
        try {
            driver.findElement(by(selector)).click();
        } catch (NoSuchElementException e) {
            log.info("▽ [WebScreenCapturer] 交互元素未找到: {}", selector);
        } catch (ElementNotInteractableException e) {
            log.info("▽ [WebScreenCapturer] 该元素不可交互: {} 尝试 JavaScript 点击...", selector);
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                WebElement el = driver.findElement(by(selector));
                js.executeScript("arguments[0].click();", el);
                log.info("▽ [WebScreenCapturer] JavaScript 成功点击: {}", selector);
            } catch (Exception ex) {
                log.info("▽ [WebScreenCapturer] JavaScript 点击失败: {}", ex.getMessage());
            }
        }
    }
}
