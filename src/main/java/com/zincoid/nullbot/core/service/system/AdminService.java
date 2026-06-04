package com.zincoid.nullbot.core.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.data.dto.AdminUpdateDTO;
import com.zincoid.nullbot.core.model.data.dto.LoginDTO;
import com.zincoid.nullbot.core.model.data.dto.PwdChangeDTO;
import com.zincoid.nullbot.core.model.data.dto.RegistDTO;
import com.zincoid.nullbot.core.model.data.po.AdminPO;

public interface AdminService extends IService<AdminPO> {

    boolean regist(RegistDTO registDTO);

    boolean login(LoginDTO loginDTO);

    boolean update(AdminUpdateDTO admin);

    boolean changePwd(Long id, PwdChangeDTO pwdChangeDTO);
}
