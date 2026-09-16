package com.zincoid.nullbot.core.module.ai.chat.tool.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("须联网, 手动验证")
public class BingSearchToolTests {

    @Test
    void searchQuery() {
        BingSearchTool tool = new BingSearchTool();
        System.out.println(tool.execute("{\"query\": \"张雪峰最近怎么了\"}"));
    }
}
