package com.zincoid.nullbot.core.properties.ai;

import com.zincoid.nullbot.core.enums.config.RepositoryType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.chat")
public class RepositoryProperties {

    private RepositoryType repository;
}
