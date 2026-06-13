package com.zincoid.nullbot.core.module.ai.chat.client.impl;

import com.zincoid.nullbot.core.module.ai.chat.client.Client;
import com.zincoid.nullbot.core.module.ai.chat.client.ClientReq;
import com.zincoid.nullbot.core.module.ai.chat.client.ClientRes;
import com.zincoid.nullbot.core.module.ai.chat.message.StdMessage;
import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.module.ai.chat.model.Model;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelReq;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelRes;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(staticName = "of")
public class SingleCallClient implements Client<StdMessage> {

    private final Model model;

    @RequiredArgsConstructor
    public class Caller {

        private final String prompt;
        private boolean thinking = false;
        private int maxTokens = 512;

        public Caller thinking(boolean thinking) { this.thinking = thinking; return this; }
        public Caller maxTokens(int maxTokens) { this.maxTokens = maxTokens; return this; }

        public ClientRes<StdMessage> call() {
            return SingleCallClient.this.call(
                    ClientReq.<StdMessage>builder()
                            .prompt(prompt)
                            .thinking(thinking)
                            .maxTokens(maxTokens)
                            .build()
            );
        }
    }

    public Caller prompt(String prompt) {
        return new Caller(prompt);
    }

    @Override
    public ClientRes<StdMessage> call(ClientReq<StdMessage> req) {
        List<Message> messages = new ArrayList<>();
        if (req.getPrompt() != null)
            messages.add(StdMessage.system(req.getPrompt()));
        if (req.getMessage() != null)
            messages.add(req.getMessage());
        ModelReq _req = ModelReq.of(messages, req.isThinking(), req.getMaxTokens());
        ModelRes _res = model.invoke(_req);
        StdMessage message = StdMessage.assistant(_res.getContent());
        return ClientRes.of(message);
    }
}
