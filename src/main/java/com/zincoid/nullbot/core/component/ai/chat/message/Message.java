package com.zincoid.nullbot.core.component.ai.chat.message;

import com.zincoid.nullbot.core.component.ai.chat.enums.Role;

import java.util.Map;

public interface Message {

    Role getRole();

    String getContent();

    Map<String, String> toMap();
}
