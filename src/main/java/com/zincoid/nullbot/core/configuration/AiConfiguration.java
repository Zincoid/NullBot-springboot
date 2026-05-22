package com.zincoid.nullbot.core.configuration;

import com.zincoid.nullbot.core.component.chat.current.client.QQAiClient;
import com.zincoid.nullbot.core.component.chat.current.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.chat.current.model.Model;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQPrompter;
import com.zincoid.nullbot.core.component.chat.current.repository.ChatRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfiguration {

    @Bean
    public QQAiClient qqAiClient(ChatRepository repository, Model model,
                                 QQAntiInjector antiInjector, QQPrompter prompter, QQMsgExecutor executor) {
        return new QQAiClient(
                new MsgWindowChatMemory(repository, 10),
                model,
                antiInjector.withModel(model),
                prompter,
                executor
        ).withMaxTokens(512);
    }
}
