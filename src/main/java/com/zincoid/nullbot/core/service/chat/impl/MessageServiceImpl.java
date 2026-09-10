package com.zincoid.nullbot.core.service.chat.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.mapper.MessageMapper;
import com.zincoid.nullbot.core.model.data.po.MessagePO;
import com.zincoid.nullbot.core.service.chat.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, MessagePO> implements MessageService {
}
