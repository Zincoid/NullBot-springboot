package com.zincoid.nullbot.core.module.ai.chat.message;

import com.zincoid.nullbot.core.enums.Role;
import java.util.Map;

public interface Message {

    Role getRole();

    String getContent();

    Map<String, Object> toMap();
}
