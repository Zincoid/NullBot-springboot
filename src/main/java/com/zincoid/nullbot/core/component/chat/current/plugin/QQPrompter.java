package com.zincoid.nullbot.core.component.chat.current.plugin;

import com.zincoid.nullbot.core.component.chat.previous.SysMsgManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QQPrompter {

    private final SysMsgManager sysMsgManager;

    public String getPrompt() {
        return null;
    }
}
