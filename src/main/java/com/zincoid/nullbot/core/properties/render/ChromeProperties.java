package com.zincoid.nullbot.core.properties.render;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "chrome")
public class ChromeProperties {

    private Driver driver;
    private Instance instance;
    private Capture capture;

    @Data
    public static class Driver {
        private Boolean auto;
        private String path;
    }

    @Data
    public static class Instance {
        private int maxConcurrent;
        private int queueTimeout;
        private int liveTimeout;
        private int loadTimeout;
    }

    @Data
    public static class Capture {
        private int maxRetries;
    }
}