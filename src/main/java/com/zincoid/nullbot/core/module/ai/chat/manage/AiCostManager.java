package com.zincoid.nullbot.core.module.ai.chat.manage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Getter
@Slf4j
@Component
public class AiCostManager {

    private volatile boolean outOfBalance = false;

    public void markOutOf() {
        outOfBalance = true;
        log.warn("▽ [AiCostManager] 已标记欠费状态 (余额不足)");
    }

    public void recover() {
        outOfBalance = false;
        log.info("▽ [AiCostManager] 欠费状态已重置");
    }
}
