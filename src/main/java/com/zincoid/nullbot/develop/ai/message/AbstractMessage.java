package com.zincoid.nullbot.develop.ai.message;

import com.zincoid.nullbot.develop.ai.enums.Role;

public abstract class AbstractMessage implements Message {

    protected final Role role;
    protected final String content;

    protected AbstractMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }
}
