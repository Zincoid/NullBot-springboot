package com.zincoid.nullbot.core.component.render;

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
import java.util.function.Supplier;

@Slf4j
@Component
public class WebScreenCapturer {

    private final ChromeDriverFactory driverFactory;
    private final int maxRetries;

    public WebScreenCapturer(ChromeDriverFactory driverFactory, ChromeProperties chromeProperties) {
        this.driverFactory = driverFactory;
        this.maxRetries = chromeProperties.getMaxRetries();
    }

    public String capture(String url, int width, int height,
                          List<String> targetSelectors,
                          List<String> ignoreSelectors,
                          List<String> clickSelectors) {
        WebDriver driver = driverFactory.createDriver(width + "," + height);
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
            driver.quit();
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
            } catch (Exception ignored) {}
        }
    }

    private void clickElement(WebDriver driver, String selector) {
        try {
            driver.findElement(by(selector)).click();
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                WebElement el = driver.findElement(by(selector));
                js.executeScript("arguments[0].click();", el);
            } catch (Exception ex) {
                log.info("▽ [WebScreenCapturer] 点击失败: {}", selector);
            }
        }
    }
}
