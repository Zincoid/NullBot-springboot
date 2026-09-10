package com.zincoid.nullbot.core.module.ai.chat.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zincoid.nullbot.core.model.data.po.MessagePO;
import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.service.chat.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.chat.repository", havingValue = "db")
public class DbRepository implements Repository {

    private final MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Message> get(String chatId) {
        List<Message> messages = new ArrayList<>();
        List<MessagePO> pos = messageService.lambdaQuery()
                .eq(MessagePO::getChatId, chatId)
                .orderByAsc(MessagePO::getId)
                .list();
        for (MessagePO po : pos) {
            try {
                messages.add(
                        objectMapper
                                .readValue(po.getPayload(), MessageDTO.class)
                                .toMessage()
                );
            } catch (JsonProcessingException e) {
                log.error("▽ [DbRepository] 反序列化失败 - id: {}", po.getId(), e);
            }
        }
        return messages;
    }

    @Override
    @Transactional
    public void update(String chatId, List<Message> messages) {
        List<MessagePO> pos = new ArrayList<>(messages.size());
        try {
            for (Message message : messages)
                pos.add(new MessagePO(chatId, objectMapper.writeValueAsString(MessageDTO.of(message))));
        } catch (JsonProcessingException e) {
            log.error("▽ [DbRepository] 序列化失败 - chatId: {}", chatId, e);
            return;
        }
        clear(chatId);
        if (!messageService.saveBatch(pos))
            log.error("▽ [DbRepository] 写入失败 - chatId: {}", chatId);
    }

    @Override
    public void clear(String chatId) {
        messageService.lambdaUpdate()
                .eq(MessagePO::getChatId, chatId)
                .remove();
    }

    @Override
    public void reset() {
        messageService.remove(Wrappers.emptyWrapper());
    }
}
