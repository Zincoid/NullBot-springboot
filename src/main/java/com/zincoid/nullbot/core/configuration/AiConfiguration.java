package com.zincoid.nullbot.core.configuration;

import com.zincoid.nullbot.core.module.ai.chat.client.impl.QQChatClient;
import com.zincoid.nullbot.core.module.ai.chat.client.impl.SimpleChatClient;
import com.zincoid.nullbot.core.module.ai.chat.client.impl.SingleCallClient;
import com.zincoid.nullbot.core.module.ai.chat.memory.Memory;
import com.zincoid.nullbot.core.module.ai.chat.memory.MsgWindowMemory;
import com.zincoid.nullbot.core.module.ai.chat.model.Model;
import com.zincoid.nullbot.core.module.ai.chat.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.module.ai.chat.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.module.ai.chat.plugin.QQPrompter;
import com.zincoid.nullbot.core.module.ai.chat.repository.Repository;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolRegistry;
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
    public QQChatClient qqChatClient(
            AiChatProperties properties, Memory memory, Model model, ToolRegistry registry,
            QQAntiInjector antiInjector, QQPrompter prompter, QQMsgExecutor executor
    ) {
        QQChatClient qqChatClient = QQChatClient.builder(
                        memory, model,
                        antiInjector.withModel(model), prompter, executor
                )
                .maxTokens(properties.getMaxTokens())
                .toolRegistry(registry, properties.getMaxToolCalls())
                .build();

        log.info("▽ [QQChatClient] QQ 聊天客户端已初始化 - Model: {}, MaxTokens: {}, MaxToolCalls: {}",
                model.getClass().getSimpleName(), properties.getMaxTokens(), properties.getMaxToolCalls());
        return qqChatClient;
    }

    @Bean
    public SingleCallClient singleCallClient(
            Model model
    ) {
        SingleCallClient singleCallClient = SingleCallClient.of(model);
        log.info("▽ [SingleCallClient] 单次调用客户端已初始化 - Model: {}", model.getClass().getSimpleName());
        return singleCallClient;
    }

    @Bean
    public SimpleChatClient simpleChatClient(
            Memory memory, Model model
    ) {
        SimpleChatClient simpleChatClient = SimpleChatClient.of(memory, model);
        log.info("▽ [SimpleChatClient] 简单聊天客户端已初始化 - Model: {}", model.getClass().getSimpleName());
        return simpleChatClient;
    }
}
