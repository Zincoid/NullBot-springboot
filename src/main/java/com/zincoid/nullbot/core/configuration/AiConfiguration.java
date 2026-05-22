package com.zincoid.nullbot.core.configuration;

import com.zincoid.nullbot.core.component.chat.current.client.QQAiClient;
import com.zincoid.nullbot.core.component.chat.current.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.chat.current.model.Model;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQAntiInjector;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQMsgExecutor;
import com.zincoid.nullbot.core.component.chat.current.plugin.QQPrompter;
import com.zincoid.nullbot.core.component.chat.current.repository.ChatRepository;
import com.zincoid.nullbot.core.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiConfiguration {

    @Bean
    public MsgWindowChatMemory msgWindowChatMemory(ChatRepository repository) {
        MsgWindowChatMemory msgWindowChatMemory = new MsgWindowChatMemory(repository, 100);
        log.info("MsgWindowChatMemory 已初始化");
        return msgWindowChatMemory;
    }

    @Bean
    public QQAiClient qqAiClient(MsgWindowChatMemory memory, Model model,
                                 QQAntiInjector antiInjector, QQPrompter prompter, QQMsgExecutor executor,
                                 SettingService service
    ) {
        QQAiClient qqAiClient = new QQAiClient(
                memory, model,
                antiInjector.withModel(model),
                prompter, executor,
                service
        ).withMaxTokens(512);
        log.info("QQAiClient 已初始化");
        return qqAiClient;
    }
}
