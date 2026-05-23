package com.zincoid.nullbot.core.component.ai.chat.client;

import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.ai.chat.memory.ChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.component.ai.chat.model.Model;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQPrompter;
import com.zincoid.nullbot.core.enums.ChatScope;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.service.SettingService;
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

    private final SettingService settingService;

    private int maxTokens = 512;

    public QQAiClient withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    @Override
    public QQMessage call(String chatId, String prompt, QQMessage message, boolean thinking, int maxTokens) {
        chatMemory.add(chatId, message);
        List<Message> _messages = new ArrayList<>();
        _messages.add(QQMessage.system(prompt));
        _messages.addAll(chatMemory.get(chatId));
        String res = model.invoke(_messages, thinking, maxTokens);
        if (message.isPrivate())
            return QQMessage.assistant(res).pm(message.getUserId(), message.getUserName());
        return QQMessage.assistant(res).gc(message.getGroupId(), message.getUserId(), message.getUserName());
    }

    // =================== 应用方法 (BotCtx) ===================

    void clear() {
        chatMemory.clear(BotCtxUtil.getChatId());
    }

    public List<QQMessage> history() {
        return chatMemory.get(BotCtxUtil.getChatId()).stream()
                .filter(msg -> msg instanceof QQMessage)
                .map(msg -> (QQMessage) msg)
                .toList();
    }

    // =================== 应用方法 (通用) ===================

    public String chat(String chatId, QQMessage message, Event event) {
        if (message.isPrivate()) {
            String prompt = qqPrompter.prompt(message.getUserId());
            QQMessage _message = call(chatId, prompt, message, false, maxTokens);
            List<QQMessage> messages = qqMsgExecutor.chain(_message, event, false, false);
            for (QQMessage msg : messages) chatMemory.add(chatId, msg);
            return _message.getContent();
        }
        SettingPO setting = settingService.get(message.getGroupId());
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

    public void clear(Long userId) {
        chatMemory.clear("Private_" + userId);
    }

    public ChatScope clear(Long groupId, Long userId) {
        ChatScope scope = settingService.get(groupId).getChatScope();
        switch (scope) {
            case Group, Monitor -> chatMemory.clear(scope + "_" + groupId);
            case Personal -> chatMemory.clear(scope + "_" + userId);
        }
        return scope;
    }

    public List<QQMessage> history(Long userId) {
        return chatMemory.get("Private_" + userId).stream()
                .filter(msg -> msg instanceof QQMessage)
                .map(msg -> (QQMessage) msg)
                .toList();
    }

    public List<QQMessage> history(Long groupId, Long userId) {
        ChatScope scope = settingService.get(groupId).getChatScope();
        return (switch (scope) {
            case Group, Monitor -> chatMemory.get(scope + "_" + groupId);
            case Personal -> chatMemory.get(scope + "_" + userId);
        }).stream()
                .filter(msg -> msg instanceof QQMessage)
                .map(msg -> (QQMessage) msg)
                .toList();
    }
}
