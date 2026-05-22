package com.zincoid.nullbot.core.component.chat.current.plugin;

import com.zincoid.nullbot.core.component.chat.previous.SysMsgManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QQPrompter {

    private final SysMsgManager sysMsgManager;

    public String getPrompt(Long groupId, Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(sysMsgManager.getDefaultMessage(groupId));
        prompt.append("\n");
        prompt.append(sysMsgManager.getCustomMessage(groupId));
        prompt.append("\n");
        prompt.append(sysMsgManager.getUserMessage(userId));
        prompt.append("\n");
        prompt.append(sysMsgManager.getLongTermGroupMemory(groupId));
        prompt.append("\n");
    }
}
