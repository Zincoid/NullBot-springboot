package org.bot.nullbot.service;

import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.entity.po.AdminPO;

public interface LoginService
{
    boolean login(LoginDTO loginDTO);

    AdminPO info(Long id);
}
