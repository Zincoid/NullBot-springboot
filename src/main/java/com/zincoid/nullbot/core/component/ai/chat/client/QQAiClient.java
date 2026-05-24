package com.zincoid.nullbot.core.component.ai.chat.client;

import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.core.component.ai.chat.message.BaseMessage;
import com.zincoid.nullbot.core.component.ai.chat.model.ModelResponse;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.ai.chat.memory.ChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.component.ai.chat.model.Model;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQPrompter;
import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolCall;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolRegistry;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class QQAiClient implements AiClient<QQMessage> {

    private final ChatMemory chatMemory;
    private final Model model;

    private final QQAntiInjector qqAntiInjector;
    private final QQPrompter qqPrompter;
    private final QQMsgExecutor qqMsgExecutor;

    private boolean toolCallEnabled = false;
    private int maxToolCalls = 0;
    private ToolRegistry toolRegistry;

    private int maxTokens = 512;

    // =========================================== 配置方法 ===========================================

    public QQAiClient withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public QQAiClient withToolCall(ToolRegistry toolRegistry, int maxToolCalls) {
        toolCallEnabled = true;
        this.maxToolCalls = maxToolCalls;
        this.toolRegistry = toolRegistry;
        return this;
    }

    // =========================================== 模型方法 ===========================================

    @Override
    public QQMessage call(String chatId, String prompt, QQMessage message, boolean thinking, int maxTokens) {
        List<Message> _messages = new ArrayList<>();
        _messages.add(QQMessage.system(prompt));
        _messages.addAll(chatMemory.get(chatId));
        QQMessage _message = QQMessage.assistant(model.invoke(_messages, thinking, maxTokens).getContent());
        if (message.isPrivate()) return _message.with(message.getUserId(), message.getUserName());
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
        boolean realToolCallEnabled = setting.isEmbedding() && !setting.isCustom() && toolCallEnabled;
        String prompt = qqPrompter.prompt(message.getGroupId(), setting.isEmbedding(), setting.isCustom(), realToolCallEnabled);
        List<QQMessage> messages;
        String responseContent;
        if (realToolCallEnabled) {
            responseContent = chatWithTools(chatId, prompt, message, event, setting);
            messages = List.of();
        } else {
            QQMessage _message = call(chatId, prompt, message, setting.isThinking(), maxTokens);
            responseContent = _message.getContent();
            if (setting.isEmbedding() && !setting.isCustom()) {
                messages = qqMsgExecutor.chain(_message, event, setting.isVoice(), setting.isEmbeddingAuth());
            } else {
                messages = qqMsgExecutor.direct(_message, setting.isVoice());
            }
        }
        for (QQMessage msg : messages) chatMemory.add(chatId, msg);
        return responseContent;
    }

    public String chat(String chatId, QQMessage message, Event event) {
        if (!message.isPrivate())
            throw new IllegalArgumentException("消息类型应为私聊消息");
        chatMemory.add(chatId, message);
        String prompt = qqPrompter.prompt(message.getUserId(), toolCallEnabled);
        List<QQMessage> messages;
        String responseContent;
        if (toolCallEnabled) {
            responseContent = chatWithTools(chatId, prompt, message, event, null);
            messages = List.of();
        } else {
            QQMessage _message = call(chatId, prompt, message, false, maxTokens);
            responseContent = _message.getContent();
            messages = qqMsgExecutor.chain(_message, event, false, false);
        }
        for (QQMessage msg : messages) chatMemory.add(chatId, msg);
        return responseContent;
    }

    public void clear(String chatId) {
        chatMemory.clear(chatId);
    }

    public List<QQMessage> history(String chatId) {
        return chatMemory.get(chatId).stream().filter(msg -> msg instanceof QQMessage)
                .map(msg -> (QQMessage) msg)
                .toList();
    }

    // =========================================== 工具方法 ===========================================

    private String chatWithTools(String chatId, String prompt, QQMessage message, Event event, SettingPO setting) {
        List<Message> toolMessages = new ArrayList<>();
        toolMessages.add(QQMessage.system(prompt));
        toolMessages.addAll(chatMemory.get(chatId));
        boolean thinking = setting != null && setting.isThinking();
        boolean voice = setting != null && setting.isVoice();
        boolean embeddingAuth = setting != null && setting.isEmbeddingAuth();
        String finalContent = runToolCalls(toolMessages, thinking, maxTokens);
        QQMessage finalMessage = QQMessage.assistant(finalContent);
        if (message.isPrivate()) {
            finalMessage = finalMessage.with(message.getUserId(), message.getUserName());
        } else {
            finalMessage = finalMessage.with(message.getGroupId(), message.getUserId(), message.getUserName());
        }
        boolean chain = setting == null || (setting.isEmbedding() && !setting.isCustom());
        List<QQMessage> sent;
        if (chain) {
            sent = qqMsgExecutor.chain(finalMessage, event, voice, embeddingAuth);
        } else {
            sent = qqMsgExecutor.direct(finalMessage, voice);
        }
        for (QQMessage msg : sent) chatMemory.add(chatId, msg);
        return finalContent;
    }

    public String runToolCalls(List<Message> messages, boolean thinking, int maxTokens) {
        for (int i = 0; i < maxToolCalls; i++) {
            ModelResponse response = model.invoke(messages, toolRegistry.getAll(), thinking, maxTokens);
            if (!response.hasToolCalls()) return response.getContent();
            log.info("◉ [ToolCall] 第{}轮: 收到{}个工具调用", i + 1, response.getToolCalls().size());
            messages.add(BaseMessage.assistant(response.getToolCalls()));
            for (ToolCall toolCall : response.getToolCalls()) {
                log.info("◉ [ToolCall] 执行工具: {}({})", toolCall.getName(), toolCall.getArguments());
                String result = executeTool(toolCall);
                log.info("◉ [ToolCall] 工具结果: {}", result);
                messages.add(BaseMessage.tool(toolCall.getId(), result));
            }
        }
        log.warn("◉ [ToolCall] 达到最大迭代次数({})，进行最终调用", maxToolCalls);
        return model.invoke(messages, thinking, maxTokens).getContent();
    }

    private String executeTool(ToolCall toolCall) {
        Tool tool = toolRegistry.get(toolCall.getName());
        if (tool == null)
            return "错误: 工具 " + toolCall.getName() + " 不存在";
        try {
            return tool.execute(toolCall.getArguments());
        } catch (Exception e) {
            log.warn("◉ [ToolCall] 工具执行失败: {}", e.getMessage());
            return "错误: " + e.getMessage();
        }
    }
}
