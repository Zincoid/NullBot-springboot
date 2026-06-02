package com.zincoid.nullbot.core.properties.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "file.resource")
public class ResourceProperties {
    private String ossBaseUrl;
    private String mode = "path";
}
