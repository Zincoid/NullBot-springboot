package com.zincoid.nullbot.core.configuration;

import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.memory.ChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.model.Model;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQPrompter;
import com.zincoid.nullbot.core.component.ai.chat.repository.ChatRepository;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolRegistry;
import com.zincoid.nullbot.core.component.ai.chat.tool.impl.BaiduSearchTool;
import com.zincoid.nullbot.core.component.ai.chat.tool.impl.QQGroupCmdTool;
import com.zincoid.nullbot.core.component.ai.chat.tool.impl.QQGroupInfoTool;
import com.zincoid.nullbot.core.component.ai.chat.tool.impl.QQPrivateCmdTool;
import com.zincoid.nullbot.core.component.ai.chat.tool.impl.QQUserInfoTool;
import com.zincoid.nullbot.core.properties.AiChatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiConfiguration {

    @Bean
    public MsgWindowChatMemory msgWindowChatMemory(
            ChatRepository repository, AiChatProperties properties
    ) {
        MsgWindowChatMemory msgWindowChatMemory = new MsgWindowChatMemory(
                repository, properties.getMaxHistoryLength()
        );
        log.info("▽ [MsgWindowChatMemory] 聊天存储已初始化 - Window Size: {}", properties.getMaxHistoryLength());
        return msgWindowChatMemory;
    }

    @Bean
    public QQAiClient qqAiClient(
            ChatMemory memory, Model model, AiChatProperties properties,
            QQGroupCmdTool qqGroupCmdTool, QQPrivateCmdTool qqPrivateCmdTool,
            QQAntiInjector antiInjector, QQPrompter prompter, QQMsgExecutor executor
    ) {
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(qqGroupCmdTool);
        toolRegistry.register(qqPrivateCmdTool);
        toolRegistry.register(new QQGroupInfoTool());
        toolRegistry.register(new QQUserInfoTool());
        toolRegistry.register(new BaiduSearchTool());

        QQAiClient qqAiClient = new QQAiClient(
                memory, model,
                antiInjector.withModel(model),
                prompter, executor
        )
                .withMaxTokens(properties.getMaxTokens())
                .withToolCall(toolRegistry, 5);

        log.info("▽ [QQAiClient] 聊天客户端已初始化 - Model: {}", model.getClass().getSimpleName());
        return qqAiClient;
    }
}
