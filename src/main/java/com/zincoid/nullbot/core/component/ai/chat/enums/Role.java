package com.zincoid.nullbot.core.component.ai.chat.enums;

import lombok.Getter;

@Getter
public enum Role {

    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
    TOOL("tool");

    private final String value;

    Role(String value) {
        this.value = value;
    }
}
