package com.zincoid.nullbot.core.module.ai.chat.client.impl;

import com.zincoid.nullbot.core.enums.ChatStrategy;
import com.zincoid.nullbot.core.module.ai.chat.client.Client;
import com.zincoid.nullbot.core.module.ai.chat.client.ClientReq;
import com.zincoid.nullbot.core.module.ai.chat.client.ClientRes;
import com.zincoid.nullbot.core.module.ai.chat.message.StdMessage;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelReq;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelRes;
import com.zincoid.nullbot.core.module.ai.chat.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.module.ai.chat.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.module.ai.chat.memory.Memory;
import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.module.ai.chat.message.QQMessage;
import com.zincoid.nullbot.core.module.ai.chat.model.Model;
import com.zincoid.nullbot.core.module.ai.chat.plugin.QQPrompter;
import com.zincoid.nullbot.core.module.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolCall;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolRegistry;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)  // CGLIB REQUIRED
public class QQChatClient implements Client<QQMessage> {

    private final Memory memory;
    private final Model model;

    private final QQAntiInjector qqAntiInjector;
    private final QQPrompter qqPrompter;
    private final QQMsgExecutor qqMsgExecutor;

    private final ToolRegistry toolRegistry;
    private final int maxToolCalls;
    private final int maxTokens;

    @RequiredArgsConstructor
    public static class Builder {

        private final Memory memory;
        private final Model model;
        private final QQAntiInjector qqAntiInjector;
        private final QQPrompter qqPrompter;
        private final QQMsgExecutor qqMsgExecutor;
        private ToolRegistry toolRegistry;
        private int maxToolCalls = 0;
        private int maxTokens = 512;

        public Builder maxTokens(int maxTokens) { this.maxTokens = maxTokens; return this; }
        public Builder toolRegistry(ToolRegistry toolRegistry, int maxToolCalls) { this.toolRegistry = toolRegistry; this.maxToolCalls = maxToolCalls; return this; }

        public QQChatClient build() {
            return new QQChatClient(
                    memory, model,
                    qqAntiInjector, qqPrompter, qqMsgExecutor,
                    toolRegistry, maxToolCalls, maxTokens
            );
        }
    }

    public static Builder builder(
            Memory memory, Model model,
            QQAntiInjector qqAntiInjector, QQPrompter qqPrompter, QQMsgExecutor qqMsgExecutor) {
        return new Builder(memory, model, qqAntiInjector, qqPrompter, qqMsgExecutor);
    }

    @RequiredArgsConstructor
    public class Caller {

        private final QQMessage message;

        public ClientRes<QQMessage> call() {
            return QQChatClient.this.call(
                    ClientReq.<QQMessage>builder()
                            .message(message)
                            .build()
            );
        }
    }

    public Caller handle(QQMessage message) {
        return new Caller(message);
    }

    // ===================================== 应用方法 =====================================

    public void clear(String chatId) {
        memory.clear(chatId);
    }

    public List<Message> history(String chatId) {
        return memory.get(chatId);
    }

    @Override
    public ClientRes<QQMessage> call(ClientReq<QQMessage> req) {
        QQMessage message = req.getMessage();
        String chatId = BotCtx.getChatId();
        memory.lock(chatId);
        try {
            memory.add(chatId, message);
            if (!message.isPrivate() && BotCtx.getSetting().isAntiInjection()) {
                if (qqAntiInjector.check(message)) {
                    QQMessage _message = QQMessage.assistant("对话被拒绝");
                    memory.add(chatId, _message);
                    return ClientRes.of(_message);
                }
            }
            ChatStrategy strategy = message.isPrivate() ?
                    ChatStrategy.EMBEDDING : BotCtx.getSetting().getChatStrategy();
            return ClientRes.of(
                    switch (strategy) {
                        case DIRECT -> chatDirect(message);
                        case EMBEDDING -> chatEmbedding(message);
                        case TOOLS -> {
                            if (toolRegistry == null || maxToolCalls <= 0)
                                throw new RuntimeException("工具调用未配置");
                            yield chatTools(message);
                        }
                    });
        } finally {
            memory.unlock(chatId);
        }
    }

    // ===================================== 工具方法 =====================================

    private String executeTool(ToolCall toolCall) {
        Tool tool = toolRegistry.get(toolCall.getName());
        if (tool == null)
            return "错误: 工具 " + toolCall.getName() + " 不存在";
        try {
            return tool.execute(toolCall.getArguments());
        } catch (Exception e) {
            log.warn("◎ [ToolCall] 工具执行失败: {}", e.getMessage());
            return "错误: " + e.getMessage();
        }
    }

    private QQMessage plainCall(String prompt, QQMessage message, boolean thinking, int maxTokens) {
        List<Message> messages = new ArrayList<>();
        messages.add(StdMessage.system(prompt));
        messages.addAll(memory.get(BotCtx.getChatId()));
        ModelRes _res = model.invoke(ModelReq.of(messages, thinking, maxTokens));
        return QQMessage.send(message, _res.getContent());
    }

    // ---------------------------------- DIRECT 方案 ----------------------------------

    private QQMessage chatDirect(QQMessage message) {
        boolean thinking = !message.isPrivate() && BotCtx.getSetting().isThinking();
        boolean voice = !message.isPrivate() && BotCtx.getSetting().isVoice();
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), false)
                : qqPrompter.group(message.getGroupId(), false);
        QQMessage _message = plainCall(prompt, message, thinking, maxTokens);
        memory.add(BotCtx.getChatId(), qqMsgExecutor.direct(_message, voice));
        return _message;
    }

    // --------------------------------- EMBEDDING 方案 --------------------------------

    private QQMessage chatEmbedding(QQMessage message) {
        boolean thinking = !message.isPrivate() && BotCtx.getSetting().isThinking();
        boolean voice = !message.isPrivate() && BotCtx.getSetting().isVoice();
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), true)
                : qqPrompter.group(message.getGroupId(), true);
        QQMessage _message = plainCall(prompt, message, thinking, maxTokens);
        List<QQMessage> messages = qqMsgExecutor.chain(_message, voice);
        for (QQMessage msg : messages) memory.add(BotCtx.getChatId(), msg);
        return _message;
    }

    // ----------------------------------- TOOLS 方案 -----------------------------------

    private QQMessage chatTools(QQMessage message) {
        boolean thinking = !message.isPrivate() && BotCtx.getSetting().isThinking();
        boolean voice = !message.isPrivate() && BotCtx.getSetting().isVoice();
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), false)
                : qqPrompter.group(message.getGroupId(), false);
        return callAndStoreWithTools(prompt, message, thinking, voice);
    }

    private QQMessage callAndStoreWithTools(String prompt, QQMessage message,
                                            boolean thinking, boolean voice) {
        String chatId = BotCtx.getChatId();
        ModelRes _res = null;
        for (int i = 0; i < maxToolCalls; i++) {
            List<Message> messages = new ArrayList<>();
            messages.add(StdMessage.system(prompt));
            messages.addAll(memory.get(chatId));
            ModelRes __res = model
                    .invoke(ModelReq.of(messages, toolRegistry.getAll(), thinking, maxTokens));
            if (!__res.hasToolCalls()) {
                _res = __res;
                break;
            }
            log.info("◎ [ToolCall] 第{}轮: {}个工具调用", i + 1, __res.getToolCalls().size());
            memory.add(chatId, StdMessage.assistant(__res.getToolCalls())
                    .withReasoning(__res.getReasoningContent()));
            for (ToolCall tc : __res.getToolCalls()) {
                log.info("◎ [ToolCall] 执行工具: {}({})", tc.getName(), tc.getArguments());
                String result = executeTool(tc);
                log.info("◎ [ToolCall] 工具结果: {}", result);
                memory.add(chatId, StdMessage.tool(tc.getId(), result));
            }
        }
        if (_res == null) {
            log.warn("◎ [ToolCall] 达到最大迭代次数: {} ", maxToolCalls);
            memory.add(chatId, StdMessage.user("达到最大工具调用轮数，请根据已有信息给出最终回答，不要再调用工具。"));
            _res = model.invoke(ModelReq.of(memory.get(chatId), false, maxTokens));
        }
        QQMessage _message = QQMessage.send(message, _res.getContent());
        memory.add(chatId, qqMsgExecutor.direct(_message, voice));
        return _message;
    }
}
