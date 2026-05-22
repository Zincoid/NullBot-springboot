package com.zincoid.nullbot.core.configuration;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.develop.ai.client.QQAiClient;
import com.zincoid.nullbot.develop.ai.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.develop.ai.model.Model;
import com.zincoid.nullbot.develop.ai.plugin.QQAntiInjector;
import com.zincoid.nullbot.develop.ai.plugin.QQMsgExecutor;
import com.zincoid.nullbot.develop.ai.repository.InMemoryChatRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfiguration {

    @Bean
    public QQAiClient qqAiClient(Model model, ResourceLoader loader,
                                 QQAntiInjector antiInjector, QQMsgExecutor executor) {
        return new QQAiClient(
                new MsgWindowChatMemory(
                        new InMemoryChatRepository(),
                        10
                ),
                model, loader,
                antiInjector.withModel(model),
                executor
        );
    }
}
