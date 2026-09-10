package com.zincoid.nullbot.core.properties.trace;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "trace.saucenao")
public class SauceNaoProperties {

    private String apiKey;
}
