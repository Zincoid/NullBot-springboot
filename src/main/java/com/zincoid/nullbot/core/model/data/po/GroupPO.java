package com.zincoid.nullbot.core.model.data.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`group`")
public class GroupPO {
    private Long id;
    private String name;
    private Integer access;
}
