package com.zincoid.nullbot.core.component.chat.current;

import com.zincoid.nullbot.core.component.chat.current.client.AiClient;
import com.zincoid.nullbot.core.component.chat.current.client.BaseAiClient;
import com.zincoid.nullbot.core.component.chat.current.memory.ChatMemory;
import com.zincoid.nullbot.core.component.chat.current.memory.MsgWindowChatMemory;
import com.zincoid.nullbot.core.component.chat.current.message.BaseMessage;
import com.zincoid.nullbot.core.component.chat.current.model.DsModel;
import com.zincoid.nullbot.core.component.chat.current.model.Model;
import com.zincoid.nullbot.core.component.chat.current.repository.ChatRepository;
import com.zincoid.nullbot.core.component.chat.current.repository.InMemoryChatRepository;

import java.util.Scanner;

public class AiTests {

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
            BaseMessage message = aiClient.call(chatId, prompt, BaseMessage.user(msg));
            System.out.println(message.getContent());
        }
    }
}
