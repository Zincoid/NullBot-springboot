package com.zincoid.nullbot.core.module.ai.chat.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToolCall {

    private String id;
    private String name;
    private String arguments;
}
