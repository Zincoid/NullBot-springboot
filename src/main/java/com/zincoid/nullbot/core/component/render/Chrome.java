package com.zincoid.nullbot.core.component.render;

import com.zincoid.nullbot.core.properties.ChromeProperties;
import com.zincoid.nullbot.core.util.Base64Util;
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
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> Objects.equals(((JavascriptExecutor) d)
                        .executeScript("return document.readyState"), "complete"));
    }

    public String capture(WebDriver driver, String cssSelector) {
        AShot ashot = new AShot();
        ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
        if (cssSelector == null)
            return Base64Util.from(ashot.takeScreenshot(driver).getImage());
        WebElement el = driver.findElement(By.cssSelector(cssSelector));
        ashot.coordsProvider(new WebDriverCoordsProvider());
        return Base64Util.from(ashot.takeScreenshot(driver, el).getImage());
    }

    public String capture(WebDriver driver, List<WebElement> elements) {
        AShot ashot = new AShot();
        ashot.shootingStrategy(ShootingStrategies.viewportPasting(500));
        ashot.coordsProvider(new WebDriverCoordsProvider());
        return Base64Util.from(ashot.takeScreenshot(driver, elements).getImage());
    }
}
