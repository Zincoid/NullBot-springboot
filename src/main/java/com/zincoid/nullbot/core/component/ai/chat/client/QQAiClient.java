package com.zincoid.nullbot.core.component.ai.chat.client;

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
public class QQAiClient {

    private enum ChatStrategy {
        WITH_COMMANDS,
        WITH_TOOLS
    }

    private final ChatMemory chatMemory;
    private final Model model;

    private final QQAntiInjector qqAntiInjector;
    private final QQPrompter qqPrompter;
    private final QQMsgExecutor qqMsgExecutor;

    private ChatStrategy chatStrategy = ChatStrategy.WITH_COMMANDS;
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
        this.chatStrategy = ChatStrategy.WITH_TOOLS;
        this.maxToolCalls = maxToolCalls;
        this.toolRegistry = toolRegistry;
        log.info("▽ [QQAiClient] 工具调用已配置 - MaxToolCalls: {}", maxToolCalls);
        return this;
    }

    public boolean switchStrategy() {
        if (toolRegistry == null) {
            log.warn("▽ [QQAiClient] 工具调用未配置 无法切换");
            return false;
        }
        chatStrategy = chatStrategy == ChatStrategy.WITH_TOOLS
                ? ChatStrategy.WITH_COMMANDS
                : ChatStrategy.WITH_TOOLS;
        log.info("▽ [QQAiClient] 策略已切换 - {}", chatStrategy);
        return true;
    }

    public void clear(String chatId) {
        chatMemory.clear(chatId);
    }

    public List<Message> history(String chatId) {
        return chatMemory.get(chatId);
    }

    // =========================================== 应用方法 ===========================================

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
        return chatStrategy == ChatStrategy.WITH_TOOLS
                ? chatWithTools(message)
                : chatWithCommands(message);
    }

    // ----------------------------------------- 工具调用方案 -----------------------------------------

    private String chatWithTools(QQMessage message) {
        String prompt = message.isPrivate()
                ? qqPrompter.prompt(message.getUserId(), false)
                : qqPrompter.prompt(message.getGroupId(), false, BotCtxUtil.getSetting().isCustom());
        QQMessage result = callWithTools(prompt, message);
        return result.getContent();
    }

    private QQMessage callWithTools(String prompt, QQMessage message) {
        String chatId = BotCtxUtil.getChatId();
        ModelResponse finalResp = null;
        for (int i = 0; i < maxToolCalls; i++) {
            List<Message> messages = new ArrayList<>();
            messages.add(QQMessage.system(prompt));
            messages.addAll(chatMemory.get(chatId));
            ModelResponse response = model.invoke(messages, toolRegistry.getAll(), false, maxTokens);
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
            finalResp = model.invoke(chatMemory.get(chatId), false, maxTokens);
        }
        QQMessage finalMessage = message.isPrivate()
                ? QQMessage.assistant(finalResp.getContent()).with(message.getUserId(), message.getUserName())
                : QQMessage.assistant(finalResp.getContent()).with(message.getGroupId(), message.getUserId(), message.getUserName());
        List<QQMessage> _messages = qqMsgExecutor.direct(finalMessage, false);
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

    // ----------------------------------------- 指令嵌入方案 -----------------------------------------

    private String chatWithCommands(QQMessage message) {
        SettingPO setting = message.isPrivate() ? null : BotCtxUtil.getSetting();
        boolean embed = setting != null && setting.isEmbedding();
        boolean custom = setting != null && setting.isCustom();
        boolean thinking = setting != null && setting.isThinking();

        String prompt = message.isPrivate()
                ? qqPrompter.prompt(message.getUserId(), true)
                : qqPrompter.prompt(message.getGroupId(), embed, custom);

        QQMessage result = plainCall(prompt, message, thinking);

        List<QQMessage> messages;
        if (embed && !custom) {
            boolean voice = setting.isVoice();
            boolean auth = setting.isEmbeddingAuth();
            messages = qqMsgExecutor.chain(result, BotCtxUtil.getEvent(), voice, auth);
        } else {
            boolean voice = setting != null && setting.isVoice();
            messages = qqMsgExecutor.direct(result, voice);
        }

        String chatId = BotCtxUtil.getChatId();
        for (QQMessage msg : messages) chatMemory.add(chatId, msg);
        return result.getContent();
    }

    private QQMessage plainCall(String prompt, QQMessage message, boolean thinking) {
        String chatId = BotCtxUtil.getChatId();
        List<Message> messages = new ArrayList<>();
        messages.add(QQMessage.system(prompt));
        messages.addAll(chatMemory.get(chatId));
        QQMessage result = QQMessage.assistant(model.invoke(messages, thinking, maxTokens).getContent());
        if (message.isPrivate()) return result.with(message.getUserId(), message.getUserName());
        return result.with(message.getGroupId(), message.getUserId(), message.getUserName());
    }
}
