package org.bot.nullbot.entity.dto;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Data
public class PwdChangeDTO {

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    public boolean verify() {
        if (!StringUtils.hasLength(oldPassword))
            return false;
        if (!StringUtils.hasLength(newPassword))
            return false;
        if (!StringUtils.hasLength(confirmPassword))
            return false;
        return Objects.equals(newPassword, confirmPassword);
    }
}
