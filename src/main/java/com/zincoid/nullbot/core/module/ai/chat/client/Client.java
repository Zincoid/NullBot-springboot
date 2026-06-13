package com.zincoid.nullbot.core.module.ai.chat.client;

import com.zincoid.nullbot.core.module.ai.chat.message.Message;

public interface Client<M extends Message> {

    ClientRes<M> call(ClientReq<M> req);
}
