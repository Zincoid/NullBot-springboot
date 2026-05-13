package org.bot.nullbot.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginDTO {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID需为正整数")
    private Long id;

    @NotBlank(message = "密码不能为空")
    private String password;
}
