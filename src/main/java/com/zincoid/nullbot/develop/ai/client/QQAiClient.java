package com.zincoid.nullbot.develop.ai.client;

import com.zincoid.nullbot.develop.ai.memory.ChatMemory;
import com.zincoid.nullbot.develop.ai.message.Message;
import com.zincoid.nullbot.develop.ai.message.QQMessage;
import com.zincoid.nullbot.develop.ai.model.Model;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class QQAiClient implements AiClient<QQMessage> {

    private final ChatMemory chatMemory;
    private final Model model;

    @Override
    public QQMessage call(String chatId, String prompt, QQMessage message) {
        chatMemory.add(chatId, message);
        List<Message> messages = chatMemory.get(chatId);
        List<Message> _messages = new ArrayList<>();
        _messages.add(QQMessage.system(prompt));
        _messages.addAll(messages);
        String content = model.invoke(_messages, false, 1024);
        QQMessage _message = QQMessage.assistant(content);
        chatMemory.add(chatId, _message);
        return _message;
    }

    @Override
    public void clear(String chatId) {
        chatMemory.clear(chatId);
    }

    public void chatBasic() {

    }

    public void chatEmbedding() {

    }
}
