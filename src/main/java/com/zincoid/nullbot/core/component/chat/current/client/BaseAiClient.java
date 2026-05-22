package com.zincoid.nullbot.core.component.chat.current.client;

import com.zincoid.nullbot.core.component.chat.current.memory.ChatMemory;
import com.zincoid.nullbot.core.component.chat.current.message.BaseMessage;
import com.zincoid.nullbot.core.component.chat.current.message.Message;
import com.zincoid.nullbot.core.component.chat.current.model.Model;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
        String content = model.invoke(_messages, thinking, maxTokens);
        BaseMessage _message = BaseMessage.assistant(content);
        chatMemory.add(chatId, _message);
        return _message;
    }
}
