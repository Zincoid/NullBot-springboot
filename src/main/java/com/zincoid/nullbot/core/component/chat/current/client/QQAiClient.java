package com.zincoid.nullbot.core.component.chat.current.client;

import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.chat.current.memory.ChatMemory;
import com.zincoid.nullbot.core.component.chat.current.message.Message;
import com.zincoid.nullbot.core.component.chat.current.message.QQMessage;
import com.zincoid.nullbot.core.component.chat.current.model.Model;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class QQAiClient implements AiClient<QQMessage> {

    private final ChatMemory chatMemory;
    private final Model model;
    private final QQAntiInjector qqAntiInjector;
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

    // =================== 应用方法 ===================

    public void chat(String chatId, String prompt, QQMessage message, boolean check, boolean voice) {
        if (check && qqAntiInjector.check(message)) {
            chatMemory.add(chatId, QQMessage.assistant("对话被拒绝"));
            return;
        }
        QQMessage _message = call(chatId, prompt, message);
        List<QQMessage> messages = qqMsgExecutor.direct(_message, voice);
        for (QQMessage msg : messages) {
            chatMemory.add(chatId, msg);
        }
    }

    public void chat(String chatId, String prompt, QQMessage message, boolean check, boolean voice,
                     Event event, boolean auth) {
        if (check && qqAntiInjector.check(message)) {
            chatMemory.add(chatId, QQMessage.assistant("对话被拒绝"));
            return;
        }
        QQMessage _message = call(chatId, prompt, message);
        List<QQMessage> messages = qqMsgExecutor.chain(_message, event, voice, auth);
        for (QQMessage msg : messages) {
            chatMemory.add(chatId, msg);
        }
    }
}
