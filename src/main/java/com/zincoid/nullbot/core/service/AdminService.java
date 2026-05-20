package com.zincoid.nullbot.core.service;

import com.zincoid.nullbot.core.entity.dto.AdminUpdateDTO;
import com.zincoid.nullbot.core.entity.dto.LoginDTO;
import com.zincoid.nullbot.core.entity.dto.PwdChangeDTO;
import com.zincoid.nullbot.core.entity.dto.RegistDTO;
import com.zincoid.nullbot.core.entity.po.AdminPO;

public interface AdminService {

    boolean regist(RegistDTO registDTO);

    boolean login(LoginDTO loginDTO);

    boolean deleteById(Long id);

    boolean update(AdminUpdateDTO admin);

    boolean changePwd(Long id, PwdChangeDTO pwdChangeDTO);

    AdminPO info(Long id);
}
