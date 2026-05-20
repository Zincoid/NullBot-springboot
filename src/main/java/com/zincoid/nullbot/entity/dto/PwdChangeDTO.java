package com.zincoid.nullbot.entity.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PwdChangeDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6~20位之间")
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "新密码必须包含大小写字母和数字")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    // 自定义类级别校验：确认密码是否与新密码一致
    @AssertTrue(message = "新密码与确认密码不一致")
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    // 自定义类级别校验：新密码不能与旧密码相同
    @AssertTrue(message = "新密码不能与旧密码相同")
    public boolean isNewPasswordDifferentFromOld() {
        return !newPassword.equals(oldPassword);
    }
}
