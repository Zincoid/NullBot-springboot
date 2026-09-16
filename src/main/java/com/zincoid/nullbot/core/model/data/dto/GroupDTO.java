package com.zincoid.nullbot.core.model.data.dto;

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
    @Min(value = 0, message = "权限不能为负数")
    private Integer access;
}
