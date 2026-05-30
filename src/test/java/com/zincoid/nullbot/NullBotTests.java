package com.zincoid.nullbot;

import com.zincoid.nullbot.core.component.ai.chat.tool.impl.BaiduSearchTool;
import com.zincoid.nullbot.core.component.ai.chat.tool.impl.BingSearchTool;
import org.junit.jupiter.api.Test;

public class NullBotTests {

    @Test
    void ToolTest() {
        BingSearchTool tool = new BingSearchTool();
        // BaiduSearchTool tool = new BaiduSearchTool();
        System.out.println(tool.execute("{\"query\": \"张雪峰最近怎么了\"}"));
    }
}
