package com.zincoid.nullbot.core.module.ai.chat.manage;

import com.zincoid.nullbot.core.properties.ai.OpenAiProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
@Component
@RequiredArgsConstructor
public class AiCostManager {

    private final OpenAiProperties openAiProperties;
    private final Map<String, Boolean> outOfMap = new ConcurrentHashMap<>();

    public boolean isOutOfBalance() {
        String current = openAiProperties.current().getName();
        return outOfMap.getOrDefault(current, false);
    }

    public void markOutOf(OpenAiProperties.Provider provider) {
        outOfMap.put(provider.getName(), true);
        log.warn("▽ [AiCostManager] 欠费状态已标记: {}", provider.getName());
    }

    public void recover() {
        outOfMap.clear();
        log.info("▽ [AiCostManager] 欠费状态已重置");
    }
}
