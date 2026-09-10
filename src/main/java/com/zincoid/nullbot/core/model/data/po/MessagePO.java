package com.zincoid.nullbot.core.model.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("message")
public class MessagePO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String chatId;
    private String payload;

    public MessagePO(String chatId, String payload) {
        this.chatId = chatId;
        this.payload = payload;
    }
}
