package com.zincoid.nullbot.core.model.data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {

    @NotNull(message = "用户 ID 不能为空")
    @Min(value = 1, message = "用户 ID 应为正整数")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String name;

    @NotNull(message = "权限不能为空")
    @Min(value = 0, message = "权限不能为负数")
    private Integer access;

    @NotNull(message = "等级不能为空")
    @Min(value = 0, message = "等级不能为负数")
    private Integer level;

    @Min(value = 0, message = "经验不能为负数")
    private Integer experience;

    @Min(value = 0, message = "余额不能为负数")
    private Integer cash;

    @Min(value = 0, message = "容量不能为负数")
    private Integer capacity;

    @Min(value = 0, message = "抽取次数不能为负数")
    private Integer drawTimes;
}
