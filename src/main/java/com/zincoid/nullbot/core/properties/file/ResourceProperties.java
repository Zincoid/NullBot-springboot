package com.zincoid.nullbot.core.properties.file;

import com.zincoid.nullbot.core.enums.config.ResourceMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "file.resource")
public class ResourceProperties {

    private String ossBaseUrl;
    private ResourceMode mode = ResourceMode.PATH;
}
