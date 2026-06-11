package com.zincoid.nullbot.core.module.render.browser;

import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.properties.render.ChromeProperties;
import org.openqa.selenium.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
public class WebCapturer {

    private final Chrome chrome;
    private final int maxRetries;

    public WebCapturer(Chrome chrome, ChromeProperties props) {
        this.chrome = chrome;
        this.maxRetries = props.getMaxRetries();
    }

    // ============================= 截图方法 =============================

    public String capture(String url, int width, int height,
                          List<String> targets, List<String> hides, List<String> clicks) {
        WebDriver driver = chrome.create(width + "," + height);
        try {
            return retry(() -> {
                driver.get(url);
                driver.manage().window().setSize(new Dimension(width, height));
                chrome.ready(driver);
                for (String sel : clicks) click(driver, sel);
                for (String sel : hides) remove(driver, sel);
                return chrome.capture(driver, targets.stream()
                        .map(s -> driver.findElement(by(s)))
                        .toList());
            });
        } finally {
            driver.quit();
        }
    }

    public String capture(String url, int width, int height) {
        WebDriver driver = chrome.create(width + "," + height);
        try {
            return retry(() -> {
                driver.get(url);
                driver.manage().window().setSize(new Dimension(width, height));
                chrome.ready(driver);
                return chrome.capture(driver, (String) null);
            });
        } finally {
            driver.quit();
        }
    }

    // ============================= 工具方法 =============================

    private String retry(Supplier<String> action) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return action.get();
            } catch (TimeoutException e) {
                log.info("▽ [WebCapturer] 页面访问超时: {} Times", i + 1);
            }
        }
        throw new RuntimeException("网页访问失败");
    }

    private void remove(WebDriver driver, String selector) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('" + selector + "').forEach(el => el.remove());");
        } catch (Exception ignored) {}
    }

    private void click(WebDriver driver, String selector) {
        try {
            driver.findElement(by(selector)).click();
        } catch (Exception e) {
            try {
                WebElement el = driver.findElement(by(selector));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            } catch (Exception ignored) {}
        }
    }

    private By by(String selector) {
        return (selector.startsWith("//") || selector.startsWith(".//") || selector.startsWith("("))
                ? By.xpath(selector) : By.cssSelector(selector);
    }
}
