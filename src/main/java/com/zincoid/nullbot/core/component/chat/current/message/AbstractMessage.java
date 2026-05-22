package com.zincoid.nullbot.core.component.chat.current.message;

import com.zincoid.nullbot.core.component.chat.current.enums.Role;

public abstract class AbstractMessage implements Message {

    protected final Role role;
    protected final String content;

    protected AbstractMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }
}
