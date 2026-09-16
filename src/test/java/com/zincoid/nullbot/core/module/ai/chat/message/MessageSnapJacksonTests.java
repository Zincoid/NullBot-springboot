package com.zincoid.nullbot.core.module.ai.chat.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class MessageSnapJacksonTests {

    @Test
    void deserializeToolCallsSnap() {
        String json = "{\"type\":\"STD\",\"role\":\"ASSISTANT\",\"toolCalls\":[{\"id\":\"c1\",\"name\":\"baidu_search\",\"arguments\":\"{}\"}]}";
        ObjectMapper om = new ObjectMapper();
        try {
            MessageSnap snap = om.readValue(json, MessageSnap.class);
            assertTrue(snap.getToolCalls() != null && !snap.getToolCalls().isEmpty());
            assertEquals("c1", snap.getToolCalls().getFirst().getId());
        } catch (Exception e) {
            fail("ToolCall 行反序列化失败: " + e);
        }
    }
}
