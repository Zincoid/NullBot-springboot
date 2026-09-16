package com.zincoid.nullbot.core.module.ai.chat.client.impl;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Scanner;

@Disabled("须 AI 服务, 交互式手动验证")
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SimpleChatClientTests {

    @Resource
    private SimpleChatClient simpleChatClient;

    @Test
    void interactiveChat() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            String input = sc.nextLine();
            String content = simpleChatClient.chat("zincoid_01")
                    .prompt("你是一只猫娘")
                    .message(input)
                    .call()
                    .getContent();
            System.out.println("AI: " + content);
        }
    }
}
