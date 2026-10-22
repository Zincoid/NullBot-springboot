package com.zincoid.nullbot.core.model.data.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupDTO {

    @NotNull(message = "群号不能为空")
    private Long id;

    @NotBlank(message = "群名不能为空")
    private String name;

    @NotNull(message = "权限不能为空")
    @Min(value = -2, message = "权限不能小于 -2")
    @Max(value = 2, message = "权限不能大于 2")
    private Integer access;
}
