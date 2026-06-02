package com.zincoid.nullbot.core.properties.render;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "chrome")
public class ChromeProperties {
    private Boolean driverAuto;
    private String driverPath;
    private int maxRetries;
    private long loadTimeout;
}