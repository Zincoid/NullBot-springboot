package com.zincoid.nullbot.core.model.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`bottle`")
public class BottlePO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private LocalDateTime time;
    private Long userId;
    private String userName;
    private String content;  // 图片类型为本地文件路径
    private Boolean isImage;
    private Integer rethrowTimes;

    public void plusRethrowTimes() {
        rethrowTimes++;
    }
}
