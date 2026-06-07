package com.zincoid.nullbot.core.model.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`stats`")
public class StatsPO {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long groupId;
    private Long userId;
    private String command;
    private Long visits;
}
