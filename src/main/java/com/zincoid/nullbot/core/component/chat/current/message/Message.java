package com.zincoid.nullbot.core.component.chat.current.message;

import com.zincoid.nullbot.core.component.chat.current.enums.Role;

import java.util.Map;

public interface Message {

    Role getRole();

    String getContent();

    Map<String, String> toMap();
}
