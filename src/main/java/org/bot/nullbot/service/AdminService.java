package org.bot.nullbot.service;

import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.entity.dto.PwdChangeDTO;
import org.bot.nullbot.entity.dto.RegistDTO;
import org.bot.nullbot.entity.po.AdminPO;

public interface AdminService {

    boolean regist(RegistDTO registDTO);

    boolean login(LoginDTO loginDTO);

    boolean delete(Long id);

    boolean update(AdminPO admin);

    boolean changePwd(Long id, PwdChangeDTO pwdChangeDTO);

    AdminPO info(Long id);
}
