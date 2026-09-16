package com.zincoid.nullbot.core.model.data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryDTO {

    @NotNull(message = "背包条目 ID 不能为空")
    @Min(value = 1, message = "背包条目 ID 应为正整数")
    private Integer id;

    @NotNull(message = "所有者 ID 不能为空")
    @Min(value = 1, message = "所有者 ID 应为正整数")
    private Long ownerId;

    @NotNull(message = "物品 ID 不能为空")
    @Min(value = 1, message = "物品 ID 应为正整数")
    private Integer itemId;

    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能为负数")
    private Integer amount;
}
