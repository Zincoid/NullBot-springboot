package org.bot.nullbot.component.storage;

import lombok.Data;
import org.bot.nullbot.config.DeepSeekConfig;
import org.springframework.stereotype.Component;

@Data
@Component
public class SysMsgStorage
{
    private final String defaultMessage;
    private String customMessage;
    private boolean isCustom;

    public SysMsgStorage(DeepSeekConfig config) {
        this.defaultMessage = config.getDefaultSystemMessage();
        customMessage = "你的名字叫Null，是一个助手。";
        isCustom = false;
    }

    public String getSysMsg() {
        if(isCustom) {
            return customMessage;
        }
        return defaultMessage;
    }

    public String changeMode() {
        isCustom = !isCustom;
        return isCustom ? "自定义" : "默认";
    }
}
