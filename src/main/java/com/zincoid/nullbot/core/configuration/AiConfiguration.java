package com.zincoid.nullbot.core.configuration;

import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import com.zincoid.nullbot.core.component.ai.chat.memory.ChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.model.Model;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.ai.chat.plugin.QQPrompter;
import com.zincoid.nullbot.core.component.ai.chat.repository.ChatRepository;
import com.zincoid.nullbot.core.properties.AiChatProperties;
import com.zincoid.nullbot.core.service.SettingService;
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
        log.info("▽ [MsgWindowChatMemory] 已初始化");
        return msgWindowChatMemory;
    }

    @Bean
    public QQAiClient qqAiClient(
            ChatMemory memory, Model model, SettingService service, AiChatProperties properties,
            QQAntiInjector antiInjector, QQPrompter prompter, QQMsgExecutor executor
    ) {
        QQAiClient qqAiClient = new QQAiClient(
                memory, model,
                antiInjector.withModel(model),
                prompter, executor,
                service
        ).withMaxTokens(properties.getMaxTokens());
        log.info("▽ [QQAiClient] 已初始化");
        return qqAiClient;
    }
}
