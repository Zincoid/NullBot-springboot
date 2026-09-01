package com.zincoid.nullbot.core.module.render.browser;

import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.properties.render.ChromeProperties;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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

    // ============================= 链式入口 =============================

    public Capture load(String url) {
        return new Capture(url);
    }

    public class Capture {

        private final String url;
        private int width = 1280;
        private int height = 800;
        private final List<Consumer<WebDriver>> steps = new ArrayList<>();
        private final List<String> targets = new ArrayList<>();

        private Capture(String url) { this.url = url; }

        public Capture size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Capture target(String... selectors) {
            targets.addAll(List.of(selectors));
            return this;
        }

        public Capture hide(String... selectors) {
            for (String sel : selectors) steps.add(d -> doRemove(d, sel));
            return this;
        }

        public Capture click(String... selectors) {
            for (String sel : selectors) steps.add(d -> doClick(d, sel));
            return this;
        }

        public Capture waitFor(String selector) {
            steps.add(d -> doWaitFor(d, selector));
            return this;
        }

        public Capture scrollTo(String selector) {
            steps.add(d -> doScrollTo(d, selector));
            return this;
        }

        public Capture pause(long millis) {
            steps.add(d -> {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return this;
        }

        public String capture() {
            WebDriver driver = chrome.create(width + "," + height);
            try {
                return withRetry(() -> {
                    driver.get(url);
                    driver.manage().window().setSize(new Dimension(width, height));
                    chrome.ready(driver);
                    for (Consumer<WebDriver> step : steps) step.accept(driver);
                    if (targets.isEmpty())
                        return chrome.capture(driver);
                    return chrome.capture(driver, targets.toArray(String[]::new));
                });
            } finally {
                driver.quit();
            }
        }
    }

    // ============================= 工具方法 =============================

    private String withRetry(Supplier<String> action) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return action.get();
            } catch (TimeoutException e) {
                log.info("▽ [WebCapturer] 页面访问超时: {} Times", i + 1);
            }
        }
        throw new RuntimeException("网页访问失败");
    }

    private void doWaitFor(WebDriver driver, String css) {
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
    }

    private void doScrollTo(WebDriver driver, String css) {
        try {
            WebElement el = driver.findElement(By.cssSelector(css));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
    }

    private void doRemove(WebDriver driver, String selector) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('" + selector + "').forEach(el => el.remove());");
        } catch (Exception ignored) {}
    }

    private void doClick(WebDriver driver, String selector) {
        By by = (selector.startsWith("//") || selector.startsWith(".//") || selector.startsWith("("))
                ? By.xpath(selector) : By.cssSelector(selector);
        try {
            driver.findElement(by).click();
        } catch (Exception e) {
            try {
                WebElement el = driver.findElement(by);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            } catch (Exception ignored) {}
        }
    }
}
