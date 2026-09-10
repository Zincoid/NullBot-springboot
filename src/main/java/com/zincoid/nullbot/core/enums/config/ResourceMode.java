package com.zincoid.nullbot.core.enums.config;

import lombok.Getter;

@Getter
public enum ResourceMode {

    PATH("path"),
    OSS("oss"),
    BASE64("base64");

    private final String value;

    ResourceMode(String value) {
        this.value = value;
    }
}
