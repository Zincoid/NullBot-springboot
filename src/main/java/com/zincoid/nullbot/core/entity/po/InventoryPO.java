package com.zincoid.nullbot.core.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`inventory`")
public class InventoryPO {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long ownerId;
    private Integer itemId;
    private Integer amount;
}
