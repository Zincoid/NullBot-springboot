package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`tts_template`")
public class TtsTemplatePO
{
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private String path;
    private String text;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdTime;
}
