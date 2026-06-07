package com.zincoid.nullbot.core.configuration;

import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.memory.Memory;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowMemory;
import com.zincoid.nullbot.core.component.ai.chat.model.Model;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQPrompter;
import com.zincoid.nullbot.core.component.ai.chat.repository.Repository;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolRegistry;
import com.zincoid.nullbot.core.properties.ai.AiChatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiConfiguration {

    @Bean
    public MsgWindowMemory msgWindowMemory(
            Repository repository, AiChatProperties properties
    ) {
        MsgWindowMemory msgWindowMemory = MsgWindowMemory.builder(repository)
                .windowSize(properties.getMaxHistoryLength())
                .build();

        log.info("▽ [MsgWindowMemory] 消息存储已初始化 - Repository: {}, WindowSize: {}",
                repository.getClass().getSimpleName(), properties.getMaxHistoryLength());
        return msgWindowMemory;
    }

    @Bean
    public QQAiClient qqAiClient(
            AiChatProperties properties, Memory memory, Model model, ToolRegistry registry,
            QQAntiInjector antiInjector, QQPrompter prompter, QQMsgExecutor executor
    ) {
        QQAiClient qqAiClient = QQAiClient.builder(
                        memory, model,
                        antiInjector.withModel(model), prompter, executor
                )
                .maxTokens(properties.getMaxTokens())
                .toolRegistry(registry, properties.getMaxToolCalls())
                .build();

        log.info("▽ [QQAiClient] 聊天客户端已初始化 - Model: {}, MaxTokens: {}, MaxToolCalls: {}",
                model.getClass().getSimpleName(), properties.getMaxTokens(), properties.getMaxToolCalls());
        return qqAiClient;
    }
}
