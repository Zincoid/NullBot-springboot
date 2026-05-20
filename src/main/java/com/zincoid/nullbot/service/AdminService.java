package com.zincoid.nullbot.service;

import com.zincoid.nullbot.entity.dto.AdminUpdateDTO;
import com.zincoid.nullbot.entity.dto.LoginDTO;
import com.zincoid.nullbot.entity.dto.PwdChangeDTO;
import com.zincoid.nullbot.entity.dto.RegistDTO;
import com.zincoid.nullbot.entity.po.AdminPO;

public interface AdminService {

    boolean regist(RegistDTO registDTO);

    boolean login(LoginDTO loginDTO);

    boolean deleteById(Long id);

    boolean update(AdminUpdateDTO admin);

    boolean changePwd(Long id, PwdChangeDTO pwdChangeDTO);

    AdminPO info(Long id);
}
