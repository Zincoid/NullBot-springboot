package com.zincoid.nullbot.core.component.ai.chat.client;

import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.ai.chat.memory.ChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.component.ai.chat.model.Model;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQPrompter;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class QQAiClient implements AiClient<QQMessage> {

    private final ChatMemory chatMemory;
    private final Model model;

    private final QQAntiInjector qqAntiInjector;
    private final QQPrompter qqPrompter;
    private final QQMsgExecutor qqMsgExecutor;

    private int maxTokens = 512;

    public QQAiClient withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    @Override
    public QQMessage call(String chatId, String prompt, QQMessage message, boolean thinking, int maxTokens) {
        List<Message> _messages = new ArrayList<>();
        _messages.add(QQMessage.system(prompt));
        _messages.addAll(chatMemory.get(chatId));
        QQMessage _message = QQMessage.from(model.invoke(_messages, thinking, maxTokens));
        if (message.isPrivate())
            return _message.with(message.getUserId(), message.getUserName());
        return _message.with(message.getGroupId(), message.getUserId(), message.getUserName());
    }

    // =========================================== 应用方法 ===========================================

    public String chat(String chatId, QQMessage message, Event event, SettingPO setting) {
        if (message.isPrivate())
            throw new IllegalArgumentException("消息类型应为群聊消息");
        chatMemory.add(chatId, message);
        if (setting.isAntiInjection() && qqAntiInjector.check(message)) {
            chatMemory.add(chatId, QQMessage.assistant("对话被拒绝"));
            return "Refused";
        }
        String prompt = qqPrompter.prompt(message.getGroupId(), setting.isEmbedding(), setting.isCustom());
        QQMessage _message = call(chatId, prompt, message, setting.isThinking(), maxTokens);
        List<QQMessage> messages;
        if (setting.isEmbedding() && !setting.isCustom()) {
            messages = qqMsgExecutor.chain(_message, event, setting.isVoice(), setting.isEmbeddingAuth());
        } else {
            messages = qqMsgExecutor.direct(_message, setting.isVoice());
        }
        for (QQMessage msg : messages) chatMemory.add(chatId, msg);
        return _message.getContent();
    }

    public String chat(String chatId, QQMessage message, Event event) {
        if (!message.isPrivate())
            throw new IllegalArgumentException("消息类型应为私聊消息");
        chatMemory.add(chatId, message);
        String prompt = qqPrompter.prompt(message.getUserId());
        QQMessage _message = call(chatId, prompt, message, false, maxTokens);
        List<QQMessage> messages = qqMsgExecutor.chain(_message, event, false, false);
        for (QQMessage msg : messages) chatMemory.add(chatId, msg);
        return _message.getContent();
    }

    public void clear(String chatId) {
        chatMemory.clear(chatId);
    }

    public List<QQMessage> history(String chatId) {
        return chatMemory.get(chatId).stream()
                .filter(msg -> msg instanceof QQMessage)
                .map(msg -> (QQMessage) msg)
                .toList();
    }
}
