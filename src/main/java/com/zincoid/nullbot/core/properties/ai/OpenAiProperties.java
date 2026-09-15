package com.zincoid.nullbot.core.properties.ai;

import com.zincoid.nullbot.core.exception.CoreException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.ai.chat.openai")
public class OpenAiProperties {

    private String active;
    private List<Provider> providers;

    @Data
    public static class Provider {
        private String name;
        private String apiUrl;
        private String apiKey;
        private String model;
    }

    public Provider find(String name) {
        if (providers == null || name == null) return null;
        return providers.stream()
                .filter(p -> name.equalsIgnoreCase(p.getName()))
                .findFirst().orElse(null);
    }

    public Provider current() {
        Provider p = find(active);
        if (p == null && providers != null && !providers.isEmpty()) p = providers.getFirst();
        if (p == null) throw new CoreException("OpenAI 供应商未配置");
        return p;
    }

    public Provider switchTo(String name) {
        Provider p = find(name);
        if (p == null) throw new CoreException("未知 OpenAI 供应商: " + name);
        active = p.getName();
        return p;
    }
}
