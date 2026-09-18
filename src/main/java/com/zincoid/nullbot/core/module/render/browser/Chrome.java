package com.zincoid.nullbot.core.module.render.browser;

import com.zincoid.nullbot.core.exception.CoreException;
import com.zincoid.nullbot.core.properties.render.ChromeProperties;
import com.zincoid.nullbot.core.utils.Base64Util;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Chrome {

    private final ChromeProperties chromeProperties;
    private final Semaphore semaphore;
    private final Set<WebDriver> active;
    private final ScheduledExecutorService watchdog;

    public Chrome(ChromeProperties chromeProperties) {
        this.chromeProperties = chromeProperties;
        this.semaphore = new Semaphore(Math.max(1, chromeProperties.getInstance().getMaxConcurrent()));
        this.active = ConcurrentHashMap.newKeySet();
        this.watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "chrome-watcher");
            thread.setDaemon(true);
            return thread;
        });
    }

    public WebDriver create(String windowSize) {
        try {
            if (!semaphore.tryAcquire(chromeProperties.getInstance().getQueueTimeout(),
                    TimeUnit.SECONDS))
                throw new CoreException("WebDriver 等待超时");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CoreException("WebDriver 等待中断");
        }
        WebDriver driver;
        try {
            if (chromeProperties.getDriver().getAuto()) {
                WebDriverManager.chromedriver().setup();
            } else {
                System.setProperty("webdriver.chrome.driver",
                        chromeProperties.getDriver().getPath());
            }
            ChromeOptions options = new ChromeOptions();
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addArguments(
                    "--headless",
                    "--disable-gpu",
                    "--disable-dev-shm-usage",
                    "--no-sandbox",
                    "--window-size=" + windowSize,
                    "--hide-scrollbars",
                    "--lang=zh-CN",
                    "--accept-lang=zh-CN,zh"
            );
            driver = new ChromeDriver(options);
        } catch (Exception e) {
            semaphore.release();
            throw new CoreException("WebDriver 创建失败", e);
        }
        driver.manage()
                .timeouts()
                .pageLoadTimeout(Duration.ofSeconds(
                        chromeProperties.getInstance().getLoadTimeout()));
        active.add(driver);
        watchdog.schedule(
                () -> {
                    try {
                        if (active.contains(driver))
                            destroy(driver);
                    } catch (Exception ignored) {}
                },
                Math.max(1, chromeProperties.getInstance().getLiveTimeout()),
                TimeUnit.SECONDS
        );
        return driver;
    }

    public void destroy(WebDriver driver) {
        boolean owned = active.remove(driver);
        try {
            driver.quit();
        } catch (Exception e) {
            throw new CoreException("WebDriver 销毁失败", e);
        } finally {
            if (owned) semaphore.release();
        }
    }

    public String capture(WebDriver driver, String... cssSelectors) {
        AShot ashot = new AShot();
        ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
        if (cssSelectors == null || cssSelectors.length == 0)
            return Base64Util.from(ashot.takeScreenshot(driver).getImage());
        ashot.coordsProvider(new WebDriverCoordsProvider());
        List<WebElement> elements = new ArrayList<>();
        for (String selector : cssSelectors) {
            try {
                elements.add(driver.findElement(
                        (selector.startsWith("//") || selector.startsWith(".//") || selector.startsWith("("))
                                ? By.xpath(selector)
                                : By.cssSelector(selector)
                ));
            } catch (NoSuchElementException e) {
                throw new CoreException("未找到页元素: " + selector);
            }
        }
        return Base64Util.from(ashot.takeScreenshot(driver, elements).getImage());
    }
}
