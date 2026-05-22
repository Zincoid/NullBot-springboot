package com.zincoid.nullbot.develop.ai.client;

import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.develop.ai.plugin.QQMsgExecutor;
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
    private final QQMsgExecutor qqMsgExecutor;

    @Override
    public QQMessage call(String chatId, String prompt, QQMessage message) {
        chatMemory.add(chatId, message);
        List<Message> messages = chatMemory.get(chatId);
        List<Message> _messages = new ArrayList<>();
        _messages.add(QQMessage.system(prompt));
        _messages.addAll(messages);
        String content = model.invoke(_messages, false, 1024);
        return QQMessage.assistant(content)
                .id(message.getMessageId())
                .gc(
                        message.getGroupId(),
                        message.getUserId(),
                        message.getUserName()
                );
    }

    @Override
    public void clear(String chatId) {
        chatMemory.clear(chatId);
    }

    public void chatBasic(String chatId, String prompt, QQMessage message, boolean voice) {
        QQMessage _message = call(chatId, prompt, message);
        List<QQMessage> messages = qqMsgExecutor.basic(_message, voice);
        for (QQMessage msg : messages) {
            chatMemory.add(chatId, msg);
        }
    }

    public void chatEmbedding(String chatId, String prompt, QQMessage message,
                              Event event, boolean voice, boolean auth) {
        QQMessage _message = call(chatId, prompt, message);
        List<QQMessage> messages = qqMsgExecutor.chain(_message, event, voice, auth);
        for (QQMessage msg : messages) {
            chatMemory.add(chatId, msg);
        }
    }
}
