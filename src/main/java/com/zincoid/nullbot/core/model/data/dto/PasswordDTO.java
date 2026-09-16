package com.zincoid.nullbot.core.model.data.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6~20位之间")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @AssertTrue(message = "新密码与确认密码不一致")
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    @AssertTrue(message = "新密码不能与旧密码相同")
    public boolean isNewPasswordDifferentFromOld() {
        return newPassword == null || !newPassword.equals(oldPassword);
    }
}
