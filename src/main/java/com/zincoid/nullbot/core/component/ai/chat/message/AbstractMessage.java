package com.zincoid.nullbot.core.component.ai.chat.message;

import com.zincoid.nullbot.core.component.ai.chat.enums.Role;

public abstract class AbstractMessage implements Message {

    protected final Role role;
    protected final String content;

    protected AbstractMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public String getContent() {
        return content;
    }
}
