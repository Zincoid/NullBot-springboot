package com.zincoid.nullbot.core.component.ai.chat.client;

import com.zincoid.nullbot.core.component.ai.chat.enums.ChatStrategy;
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
import com.zincoid.nullbot.core.util.BotCtxUtil;
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

    private ToolRegistry toolRegistry;
    private int maxToolCalls = 0;

    private int maxTokens = 512;

    // ======================================= 系统方法 ======================================

    public QQAiClient withMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        log.info("▽ [QQAiClient] 词元限制已配置 - MaxTokens: {}", maxTokens);
        return this;
    }

    public QQAiClient withToolCall(ToolRegistry toolRegistry, int maxToolCalls) {
        this.maxToolCalls = maxToolCalls;
        this.toolRegistry = toolRegistry;
        log.info("▽ [QQAiClient] 工具调用已配置 - MaxToolCalls: {}", maxToolCalls);
        return this;
    }

    public void clear(String chatId) {
        chatMemory.clear(chatId);
    }

    public List<Message> history(String chatId) {
        return chatMemory.get(chatId);
    }

    // ======================================= 应用方法 ======================================

    @Override
    @Deprecated
    public QQMessage call(String chatId, String prompt, QQMessage message, boolean thinking, int maxTokens) {
        return plainCall(prompt, message, thinking, maxTokens);
    }

    public String chat(QQMessage message) {
        String chatId = BotCtxUtil.getChatId();
        chatMemory.add(chatId, message);
        if (!message.isPrivate() && BotCtxUtil.getSetting().isAntiInjection()) {
            if (qqAntiInjector.check(message)) {
                chatMemory.add(chatId, QQMessage.assistant("对话被拒绝"));
                return "Refused";
            }
        }
        ChatStrategy strategy = message.isPrivate()
                ? ChatStrategy.EMBEDDING : BotCtxUtil.getSetting().getChatStrategy();
        return switch (strategy) {
            case DIRECT -> chatDirect(message);
            case EMBEDDING -> chatEmbedding(message);
            case TOOLS -> {
                if (toolRegistry == null || maxToolCalls <= 0)
                    throw new RuntimeException("工具调用未配置");
                yield chatTools(message);
            }
        };
    }

    // ------------------------------------- DIRECT 方案 ------------------------------------

    private String chatDirect(QQMessage message) {
        boolean thinking = !message.isPrivate() && BotCtxUtil.getSetting().isThinking();
        boolean voice = !message.isPrivate() && BotCtxUtil.getSetting().isVoice();
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), false)
                : qqPrompter.group(message.getGroupId(), false);
        QQMessage _message = plainCall(prompt, message, thinking, maxTokens);
        chatMemory.add(BotCtxUtil.getChatId(), qqMsgExecutor.direct(_message, voice));
        return _message.getContent();
    }

    // ----------------------------------- EMBEDDING 方案 -----------------------------------

    private String chatEmbedding(QQMessage message) {
        boolean thinking = !message.isPrivate() && BotCtxUtil.getSetting().isThinking();
        boolean voice = !message.isPrivate() && BotCtxUtil.getSetting().isVoice();
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), false)
                : qqPrompter.group(message.getGroupId(), false);
        QQMessage _message = plainCall(prompt, message, thinking, maxTokens);
        List<QQMessage> messages = qqMsgExecutor.chain(_message, voice);
        for (QQMessage msg : messages) chatMemory.add(BotCtxUtil.getChatId(), msg);
        return _message.getContent();
    }

    // ------------------------------------- TOOLS 方案 -------------------------------------

    private String chatTools(QQMessage message) {
        boolean thinking = !message.isPrivate() && BotCtxUtil.getSetting().isThinking();
        boolean voice = !message.isPrivate() && BotCtxUtil.getSetting().isVoice();
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), false)
                : qqPrompter.group(message.getGroupId(), false);
        QQMessage _message = callAndStoreWithTools(prompt, message, thinking, voice);
        return _message.getContent();
    }

    private QQMessage callAndStoreWithTools(String prompt, QQMessage message,
                                            boolean thinking, boolean voice) {
        String chatId = BotCtxUtil.getChatId();
        ModelResponse finalResp = null;
        for (int i = 0; i < maxToolCalls; i++) {
            List<Message> messages = new ArrayList<>();
            messages.add(BaseMessage.system(prompt));
            messages.addAll(chatMemory.get(chatId));
            ModelResponse resp = model
                    .invoke(messages, toolRegistry.getAll(), thinking, maxTokens);
            if (!resp.hasToolCalls()) {
                finalResp = resp;
                break;
            }
            log.info("◎ [ToolCall] 第{}轮: {}个工具调用", i + 1, resp.getToolCalls().size());
            chatMemory.add(chatId, BaseMessage.assistant(resp.getToolCalls())
                    .withReasoning(resp.getReasoningContent()));
            for (ToolCall tc : resp.getToolCalls()) {
                log.info("◎ [ToolCall] 执行工具: {}({})", tc.getName(), tc.getArguments());
                String result = executeTool(tc);
                log.info("◎ [ToolCall] 工具结果: {}", result);
                chatMemory.add(chatId, BaseMessage.tool(tc.getId(), result));
            }
        }
        if (finalResp == null) {
            log.warn("◎ [ToolCall] 最终调用: 达到最大迭代次数{} ", maxToolCalls);
            finalResp = model.invoke(chatMemory.get(chatId), false, maxTokens);
        }
        QQMessage _message = QQMessage.send(message, finalResp.getContent());
        chatMemory.add(chatId, qqMsgExecutor.direct(_message, voice));
        return _message;
    }

    // ====================================== 工具方法 ======================================

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
        messages.add(BaseMessage.system(prompt));
        messages.addAll(chatMemory.get(BotCtxUtil.getChatId()));
        ModelResponse resp = model.invoke(messages, thinking, maxTokens);
        return QQMessage.send(message, resp.getContent());
    }
}
