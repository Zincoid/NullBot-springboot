package org.bot.nullbot.entity.dto;

import lombok.Data;

@Data
public class RegistDTO
{
    private String id;
    private String password;
    private String email;
    private String activationCode;
}
