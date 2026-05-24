package com.zincoid.nullbot.core.component.ai.chat.client;

import com.zincoid.nullbot.core.component.ai.chat.memory.ChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.BaseMessage;
import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.model.Model;
import com.zincoid.nullbot.core.component.ai.chat.model.ModelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// @Component
@RequiredArgsConstructor
public class BaseAiClient implements AiClient<BaseMessage> {

    private final ChatMemory chatMemory;
    private final Model model;

    @Override
    public BaseMessage call(String chatId, String prompt, BaseMessage message, boolean thinking, int maxTokens) {
        chatMemory.add(chatId, message);
        List<Message> messages = chatMemory.get(chatId);
        List<Message> _messages = new ArrayList<>();
        _messages.add(BaseMessage.system(prompt));
        _messages.addAll(messages);
        ModelResponse response = model.invoke(_messages, thinking, maxTokens);
        BaseMessage _message = BaseMessage.assistant(response.getContent());
        chatMemory.add(chatId, _message);
        return _message;
    }
}
