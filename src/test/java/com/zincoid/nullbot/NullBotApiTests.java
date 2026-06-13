package com.zincoid.nullbot;

import com.zincoid.nullbot.core.module.ai.voice.TtsClient;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NullBotApiTests {

    @Resource
    TtsClient ttsClient;

    @Test
    void TtsTest() throws IOException {
        String text = "大家好呀";
        String base64 = ttsClient.synthesize(text);
        byte[] bytes = Base64.getDecoder().decode(base64);
        Path output = Path.of("src/test/file/tts_output.mp3");
        Files.write(output, bytes);
        System.out.println("音频已保存到: " + output.toAbsolutePath());
    }
}
