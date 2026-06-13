package com.zincoid.nullbot.core.module.ai.chat.client.impl;

import com.zincoid.nullbot.core.module.ai.chat.client.Client;
import com.zincoid.nullbot.core.module.ai.chat.client.ClientReq;
import com.zincoid.nullbot.core.module.ai.chat.client.ClientRes;
import com.zincoid.nullbot.core.module.ai.chat.memory.Memory;
import com.zincoid.nullbot.core.module.ai.chat.message.StdMessage;
import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelReq;
import com.zincoid.nullbot.core.module.ai.chat.model.Model;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelRes;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(staticName = "of")
public class SimpleChatClient implements Client<StdMessage> {

    private final Memory memory;
    private final Model model;

    @RequiredArgsConstructor
    public class Caller {

        private final String chatId;
        private String prompt;
        private StdMessage message;
        private boolean thinking = false;
        private int maxTokens = 512;

        public Caller prompt(String prompt) { this.prompt = prompt; return this; }
        public Caller message(String message) { this.message = StdMessage.user(message); return this; }
        public Caller thinking(boolean thinking) { this.thinking = thinking; return this; }
        public Caller maxTokens(int maxTokens) { this.maxTokens = maxTokens; return this; }

        public ClientRes<StdMessage> call() {
            if (message == null)
                throw new IllegalArgumentException("消息不能为空");
            return SimpleChatClient.this.call(
                    ClientReq.<StdMessage>builder()
                            .chatId(chatId)
                            .prompt(prompt)
                            .message(message)
                            .thinking(thinking)
                            .maxTokens(maxTokens)
                            .build()
            );
        }
    }

    public Caller chat(String chatId) {
        return new Caller(chatId);
    }

    @Override
    public ClientRes<StdMessage> call(ClientReq<StdMessage> req) {
        String chatId = req.getChatId();
        memory.add(chatId, req.getMessage());
        List<Message> messages = new ArrayList<>();
        if (req.getPrompt() != null)
            messages.add(StdMessage.system(req.getPrompt()));
        messages.addAll(memory.get(chatId));
        ModelReq _req = ModelReq.of(messages, req.isThinking(), req.getMaxTokens());
        ModelRes _res = model.invoke(_req);
        StdMessage message = StdMessage.assistant(_res.getContent());
        memory.add(chatId, message);
        return ClientRes.of(message);
    }
}
