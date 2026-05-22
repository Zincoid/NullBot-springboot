package com.zincoid.nullbot.develop.ai.message;

import com.zincoid.nullbot.develop.ai.enums.Role;

import java.util.Map;

public interface Message {

    Role getRole();

    String getContent();

    Map<String, String> toMap();
}
