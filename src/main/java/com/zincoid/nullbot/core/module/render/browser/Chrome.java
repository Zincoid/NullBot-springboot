package com.zincoid.nullbot.core.module.render.browser;

import com.zincoid.nullbot.core.exception.CoreException;
import com.zincoid.nullbot.core.properties.render.ChromeProperties;
import com.zincoid.nullbot.core.utils.Base64Util;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Chrome {

    private final ChromeProperties chromeProperties;

    public WebDriver create(String windowSize) {
        if (chromeProperties.getDriverAuto()) {
            WebDriverManager.chromedriver().setup();
        } else {
            System.setProperty("webdriver.chrome.driver", chromeProperties.getDriverPath());
        }
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--window-size=" + windowSize,
                "--hide-scrollbars", "--lang=zh-CN", "--accept-lang=zh-CN,zh");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(chromeProperties.getLoadTimeout()));
        return driver;
    }

    public void ready(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(chromeProperties.getReadyTimeout()))
                .until(d -> Objects.equals(((JavascriptExecutor) d)
                        .executeScript("return document.readyState"), "complete"));
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
                elements.add(driver.findElement((selector.startsWith("//") || selector.startsWith(".//")
                        || selector.startsWith("(")) ? By.xpath(selector) : By.cssSelector(selector)));
            } catch (NoSuchElementException e) {
                throw new CoreException("页面元素未找到: " + selector);
            }
        }
        return Base64Util.from(ashot.takeScreenshot(driver, elements).getImage());
    }
}
