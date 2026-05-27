package com.zincoid.nullbot.core.component.render;

import com.zincoid.nullbot.core.properties.ChromeProperties;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ChromeDriverFactory {

    private final ChromeProperties chromeProperties;

    public ChromeOptions createOptions(String windowSize, String... extraArgs) {
        if (chromeProperties.getDriverAuto()) {
            WebDriverManager.chromedriver().setup();
        } else {
            System.setProperty("webdriver.chrome.driver", chromeProperties.getDriverPath());
        }

        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=" + windowSize);
        options.addArguments("--hide-scrollbars");
        options.addArguments("--lang=zh-CN");
        options.addArguments("--accept-lang=zh-CN,zh");
        for (String arg : extraArgs) {
            options.addArguments(arg);
        }
        return options;
    }

    public WebDriver createDriver(ChromeOptions options) {
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(chromeProperties.getLoadTimeout()));
        return driver;
    }

    public WebDriver createDriver(String windowSize, String... extraArgs) {
        return createDriver(createOptions(windowSize, extraArgs));
    }
}
