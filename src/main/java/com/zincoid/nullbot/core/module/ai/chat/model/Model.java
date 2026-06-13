package com.zincoid.nullbot.core.module.ai.chat.model;

public interface Model {

    ModelRes invoke(ModelReq req);
}
