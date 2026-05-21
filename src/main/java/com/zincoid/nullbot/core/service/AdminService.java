package com.zincoid.nullbot.core.service;

import com.zincoid.nullbot.core.model.dto.AdminUpdateDTO;
import com.zincoid.nullbot.core.model.dto.LoginDTO;
import com.zincoid.nullbot.core.model.dto.PwdChangeDTO;
import com.zincoid.nullbot.core.model.dto.RegistDTO;
import com.zincoid.nullbot.core.model.po.AdminPO;

public interface AdminService {

    boolean regist(RegistDTO registDTO);

    boolean login(LoginDTO loginDTO);

    boolean deleteById(Long id);

    boolean update(AdminUpdateDTO admin);

    boolean changePwd(Long id, PwdChangeDTO pwdChangeDTO);

    AdminPO info(Long id);
}
