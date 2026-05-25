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
import com.zincoid.nullbot.core.model.data.po.SettingPO;
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

    private int maxToolCalls = 0;
    private ToolRegistry toolRegistry;

    private int maxTokens = 512;

    // =========================================== 系统方法 ===========================================

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

    // =========================================== 应用方法 ===========================================

    @Override
    @Deprecated
    public QQMessage call(String chatId, String prompt, QQMessage message, boolean thinking, int maxTokens) {
        return plainCall(prompt, message, thinking, maxTokens);
    }

    public String chat(QQMessage message) {
        String chatId = BotCtxUtil.getChatId();
        chatMemory.add(chatId, message);
        if (!message.isPrivate()) {
            SettingPO setting = BotCtxUtil.getSetting();
            if (setting.isAntiInjection() && qqAntiInjector.check(message)) {
                chatMemory.add(chatId, QQMessage.assistant("对话被拒绝"));
                return "Refused";
            }
        }
        ChatStrategy strategy = message.isPrivate()
                ? ChatStrategy.EMBEDDING : BotCtxUtil.getSetting().getChatStrategy();
        return switch (strategy) {
            case DIRECT -> chatDirect(message);
            case EMBEDDING -> chatWithEmbedding(message);
            case TOOLS -> toolRegistry != null
                    ? chatWithTools(message) : chatWithEmbedding(message);
        };
    }

    // ------------------------------------------ DIRECT 方案 ------------------------------------------

    private String chatDirect(QQMessage message) {
        boolean thinking = !message.isPrivate() && BotCtxUtil.getSetting().isThinking();
        boolean voice = !message.isPrivate() && BotCtxUtil.getSetting().isVoice();
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), false)
                : qqPrompter.group(message.getGroupId(), false);
        QQMessage result = plainCall(prompt, message, thinking, maxTokens);
        List<QQMessage> messages = qqMsgExecutor.direct(result, voice);
        for (QQMessage msg : messages) chatMemory.add(BotCtxUtil.getChatId(), msg);
        return result.getContent();
    }

    // ------------------------------------------ EMBEDDING 方案 ------------------------------------------

    private String chatWithEmbedding(QQMessage message) {
        boolean thinking = !message.isPrivate() && BotCtxUtil.getSetting().isThinking();
        boolean voice = !message.isPrivate() && BotCtxUtil.getSetting().isVoice();
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), false)
                : qqPrompter.group(message.getGroupId(), false);
        QQMessage result = plainCall(prompt, message, thinking, maxTokens);
        List<QQMessage> messages = qqMsgExecutor.chain(result, voice);
        for (QQMessage msg : messages) chatMemory.add(BotCtxUtil.getChatId(), msg);
        return result.getContent();
    }

    // ------------------------------------------ TOOLS 方案 ------------------------------------------

    private String chatWithTools(QQMessage message) {
        String prompt = message.isPrivate()
                ? qqPrompter.user(message.getUserId(), false)
                : qqPrompter.group(message.getGroupId(), false);
        QQMessage result = callAndStoreWithTools(prompt, message, false, false);
        return result.getContent();
    }

    private QQMessage callAndStoreWithTools(String prompt, QQMessage message,
                                            boolean thinking, boolean voice) {
        String chatId = BotCtxUtil.getChatId();
        ModelResponse finalResp = null;
        for (int i = 0; i < maxToolCalls; i++) {
            List<Message> messages = new ArrayList<>();
            messages.add(QQMessage.system(prompt));
            messages.addAll(chatMemory.get(chatId));
            ModelResponse resp = model
                    .invoke(messages, toolRegistry.getAll(), thinking, maxTokens);
            if (!resp.hasToolCalls()) {
                finalResp = resp;
                break;
            }
            log.info("◉ [ToolCall] 第{}轮: {}个工具调用", i + 1, resp.getToolCalls().size());
            chatMemory.add(chatId, BaseMessage.assistant(resp.getToolCalls()));
            for (ToolCall tc : resp.getToolCalls()) {
                log.info("◉ [ToolCall] 执行工具: {}({})", tc.getName(), tc.getArguments());
                String result = executeTool(tc);
                log.info("◉ [ToolCall] 工具结果: {}", result);
                chatMemory.add(chatId, BaseMessage.tool(tc.getId(), result));
            }
        }
        if (finalResp == null) {
            log.warn("◉ [ToolCall] 最终调用: 达到最大迭代次数{} ", maxToolCalls);
            finalResp = model.invoke(chatMemory.get(chatId), false, maxTokens);
        }
        QQMessage finalMessage = message.isPrivate()
                ? QQMessage.assistant(finalResp.getContent())
                .with(message.getUserId(), message.getUserName())
                : QQMessage.assistant(finalResp.getContent())
                .with(message.getGroupId(), message.getUserId(), message.getUserName());
        List<QQMessage> _messages = qqMsgExecutor.direct(finalMessage, voice);
        for (QQMessage msg : _messages) chatMemory.add(chatId, msg);
        return finalMessage;
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

    // =========================================== 工具方法 ===========================================

    private QQMessage plainCall(String prompt, QQMessage message, boolean thinking, int maxTokens) {
        List<Message> messages = new ArrayList<>();
        messages.add(QQMessage.system(prompt));
        messages.addAll(chatMemory.get(BotCtxUtil.getChatId()));
        QQMessage result = QQMessage.assistant(model.invoke(messages, thinking, maxTokens).getContent());
        if (message.isPrivate()) return result.with(message.getUserId(), message.getUserName());
        return result.with(message.getGroupId(), message.getUserId(), message.getUserName());
    }
}
