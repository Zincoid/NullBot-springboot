package com.zincoid.nullbot.core.module.ai.chat.tool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {

    private String id;
    private String name;
    private String arguments;
}
