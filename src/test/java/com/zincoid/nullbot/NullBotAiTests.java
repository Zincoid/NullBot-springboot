package com.zincoid.nullbot;

import com.zincoid.nullbot.core.component.ai.chat.client.AiClient;
import com.zincoid.nullbot.core.component.ai.chat.client.BaseAiClient;
import com.zincoid.nullbot.core.component.ai.chat.memory.ChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.ai.chat.message.BaseMessage;
import com.zincoid.nullbot.core.component.ai.chat.model.DsModel;
import com.zincoid.nullbot.core.component.ai.chat.model.Model;
import com.zincoid.nullbot.core.component.ai.chat.repository.ChatRepository;
import com.zincoid.nullbot.core.component.ai.chat.repository.InMemoryChatRepository;

import java.util.Scanner;

public class NullBotAiTests {

    private static final ChatRepository repository = new InMemoryChatRepository();
    private static final ChatMemory memory = new MsgWindowChatMemory(repository, 5);
    private static final Model model = new DsModel();
    private static final AiClient<BaseMessage> aiClient = new BaseAiClient(memory, model);

    public static void main(String[] args) {

        String prompt = "你是一个猫娘，名字叫Null。";
        String chatId = "1";

        Scanner sc = new Scanner(System.in);

        while (sc.hasNext()) {
            String msg = sc.next();
            BaseMessage message = aiClient.call(chatId, prompt, BaseMessage.user(msg), false, 1024);
            System.out.println(message.getContent());
        }
    }
}
