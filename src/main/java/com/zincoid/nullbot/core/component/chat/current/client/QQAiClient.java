package com.zincoid.nullbot.core.component.chat.current.client;

import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.chat.current.memory.ChatMemory;
import com.zincoid.nullbot.core.component.chat.current.message.Message;
import com.zincoid.nullbot.core.component.chat.current.message.QQMessage;
import com.zincoid.nullbot.core.component.chat.current.model.Model;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQPrompter;
import com.zincoid.nullbot.core.enums.ChatScope;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.util.BotCtxUtil;
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

    @Override
    public QQMessage call(String chatId, String prompt, QQMessage message, boolean thinking, int maxTokens) {
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

    public QQAiClient withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    // =================== 应用方法 ===================

    public String pm(QQMessage message, Event event) {
        String chatId = "Private_" + message.getUserId();
        String prompt = qqPrompter.pm(message.getUserId());
        QQMessage _message = call(chatId, prompt, message, false, maxTokens);
        List<QQMessage> messages = qqMsgExecutor.chain(_message, event, false, false);
        for (QQMessage msg : messages) {
            chatMemory.add(chatId, msg);
        }
        return _message.getContent();
    }

    public String gc(QQMessage message, Event event) {
        SettingPO setting = BotCtxUtil.getSetting();
        String chatId = setting.getChatScope() + "_" +
                (setting.getChatScope() == ChatScope.Personal
                        ? message.getUserId() : message.getGroupId());
        if (setting.isAntiInjection() && qqAntiInjector.check(message)) {
            chatMemory.add(chatId, QQMessage.assistant("对话被拒绝"));
            return "Refused";
        }
        String prompt = qqPrompter.gc(message.getGroupId(), setting.isEmbedding(), setting.isCustom());
        QQMessage _message = call(chatId, prompt, message, setting.isThinking(), maxTokens);
        List<QQMessage> messages = qqMsgExecutor.chain(_message, event,
                setting.isVoice(), setting.isEmbeddingAuth());
        for (QQMessage msg : messages) {
            chatMemory.add(chatId, msg);
        }
        return _message.getContent();
    }

    public void reset(Long userId) {
        chatMemory.clear("Private_" + userId);
    }

    public void reset(Long groupId, Long userId) {
        SettingPO setting = BotCtxUtil.getSetting();
        String chatId = setting.getChatScope() + "_" +
                (setting.getChatScope() == ChatScope.Personal ? userId : groupId);
        chatMemory.clear(chatId);
    }
}
