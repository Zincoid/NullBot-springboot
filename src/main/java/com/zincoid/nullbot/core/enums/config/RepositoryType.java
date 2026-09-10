package com.zincoid.nullbot.core.enums.config;

import lombok.Getter;

@Getter
public enum RepositoryType {

    MEMORY("memory"),
    DB("db"),
    FILE("file");

    private final String value;

    RepositoryType(String value) {
        this.value = value;
    }
}
