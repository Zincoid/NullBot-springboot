package com.zincoid.nullbot.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.api")
public class ApiProperties {
    private String baseUrl;
}
