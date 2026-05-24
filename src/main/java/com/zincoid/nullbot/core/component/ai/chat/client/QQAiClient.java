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

    private boolean enableToolCall = false;
    private int maxToolCalls = 0;
    private ToolRegistry toolRegistry;

    private int maxTokens = 512;

    // =========================================== 配置方法 ===========================================

    public QQAiClient withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        log.info("▽ [QQAiClient] 词元限制已配置 - MaxTokens: {}", maxTokens);
        return this;
    }

    public QQAiClient withToolCall(ToolRegistry toolRegistry, int maxToolCalls) {
        enableToolCall = true;
        this.maxToolCalls = maxToolCalls;
        this.toolRegistry = toolRegistry;
        log.info("▽ [QQAiClient] 工具调用已配置 - MaxToolCalls: {}", maxToolCalls);
        return this;
    }

    // =========================================== 系统方法 ===========================================

    public void clear(String chatId) {
        chatMemory.clear(chatId);
    }

    public List<QQMessage> history(String chatId) {
        return chatMemory.get(chatId).stream().filter(msg -> msg instanceof QQMessage)
                .map(msg -> (QQMessage) msg)
                .toList();
    }

    public boolean switchToolCall() {
        if (toolRegistry == null || maxToolCalls <= 0) {
            log.warn("▽ [QQAiClient] 工具调用未配置");
            return false;
        }
        log.info("▽ [QQAiClient] 工具调用启用状态 - {}", !enableToolCall ? "ON" : "OFF");
        return enableToolCall = !enableToolCall;
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
        if (enableToolCall) {
            String prompt = qqPrompter.prompt(message.getGroupId(), false, setting.isCustom());
            QQMessage _message = callWithTools(chatId, prompt, message, false, maxTokens);
            return _message.getContent();
        } else {
            String prompt = qqPrompter.prompt(message.getGroupId(), setting.isEmbedding(), setting.isCustom());
            QQMessage _message = call(chatId, prompt, message, setting.isThinking(), maxTokens);
            List<QQMessage> messages = (setting.isEmbedding() && !setting.isCustom())
                    ? qqMsgExecutor.chain(_message, event, setting.isVoice(), setting.isEmbeddingAuth())
                    : qqMsgExecutor.direct(_message, setting.isVoice());
            for (QQMessage msg : messages) chatMemory.add(chatId, msg);
            return _message.getContent();
        }
    }

    public String chat(String chatId, QQMessage message, Event event) {
        if (!message.isPrivate())
            throw new IllegalArgumentException("消息类型应为私聊消息");
        chatMemory.add(chatId, message);
        if (enableToolCall) {
            String prompt = qqPrompter.prompt(message.getUserId(), false);
            QQMessage _message = callWithTools(chatId, prompt, message, false, maxTokens);
            return _message.getContent();
        } else {
            String prompt = qqPrompter.prompt(message.getUserId(), true);
            QQMessage _message = call(chatId, prompt, message, false, maxTokens);
            List<QQMessage> messages = qqMsgExecutor.chain(_message, event, false, false);
            for (QQMessage msg : messages) chatMemory.add(chatId, msg);
            return _message.getContent();
        }
    }

    // =========================================== 工具调用方案 ===========================================

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

    private QQMessage callWithTools(String chatId, String prompt, QQMessage message, boolean thinking, int maxTokens) {
        ModelResponse finalResp = null;
        for (int i = 0; i < maxToolCalls; i++) {
            List<Message> messages = new ArrayList<>();
            messages.add(QQMessage.system(prompt));
            messages.addAll(chatMemory.get(chatId));
            ModelResponse response = model.invoke(messages, toolRegistry.getAll(), thinking, maxTokens);
            if (!response.hasToolCalls()) {
                finalResp = response;
                break;
            }
            log.info("◉ [ToolCall] 第{}轮: 收到{}个工具调用", i + 1, response.getToolCalls().size());
            chatMemory.add(chatId, BaseMessage.assistant(response.getToolCalls()));
            for (ToolCall toolCall : response.getToolCalls()) {
                log.info("◉ [ToolCall] 执行工具: {}({})", toolCall.getName(), toolCall.getArguments());
                String result = executeTool(toolCall);
                log.info("◉ [ToolCall] 工具结果: {}", result);
                chatMemory.add(chatId, BaseMessage.tool(toolCall.getId(), result));
            }
        }
        if (finalResp == null) {
            log.warn("◉ [ToolCall] 达到最大迭代次数({})，进行最终调用", maxToolCalls);
            finalResp = model.invoke(chatMemory.get(chatId), thinking, maxTokens);
        }
        QQMessage finalMessage = message.isPrivate()
                ? QQMessage.assistant(finalResp.getContent()).with(message.getUserId(), message.getUserName())
                : QQMessage.assistant(finalResp.getContent()).with(message.getGroupId(), message.getUserId(), message.getUserName());
        List<QQMessage> _messages = qqMsgExecutor.direct(finalMessage, false);
        for (QQMessage msg : _messages) chatMemory.add(chatId, msg);
        return finalMessage;
    }
}
