package com.zincoid.nullbot.core.component.ai.chat.tool;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToolDef {

    private final String name;
    private final String description;
    private final ObjectNode parameters;
}
