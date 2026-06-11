package com.zincoid.nullbot.core.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.service.basic.UserService;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.module.security.SecurityCodeScheduler;
import com.zincoid.nullbot.core.converter.AdminConverter;
import com.zincoid.nullbot.core.model.data.dto.AdminUpdateDTO;
import com.zincoid.nullbot.core.model.data.dto.LoginDTO;
import com.zincoid.nullbot.core.model.data.dto.PwdChangeDTO;
import com.zincoid.nullbot.core.model.data.dto.RegistDTO;
import com.zincoid.nullbot.core.model.data.po.AdminPO;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.web.exception.CommonException;
import com.zincoid.nullbot.core.mapper.AdminMapper;
import com.zincoid.nullbot.core.service.system.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, AdminPO> implements AdminService {

    private final SecurityCodeScheduler securityCodeScheduler;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public boolean regist(RegistDTO registDTO) {
        if (!securityCodeScheduler.validateCode("regist", registDTO.getActivationCode()))
            throw new CommonException("激活码错误");
        UserPO user = userService.getById(registDTO.getId());
        if (user == null)
            throw new CommonException("用户不可用 (未使用过 NullBot)");
        AdminPO admin = getById(registDTO.getId());
        if (admin != null)
            throw new CommonException("用户已注册");
        AdminPO newAdmin = AdminConverter.INSTANCE.toPO(user);
        newAdmin.setEmail(registDTO.getEmail());
        newAdmin.setPassword(passwordEncoder.encode(registDTO.getPassword()));
        try {
            boolean inserted = save(newAdmin);
            if (inserted) securityCodeScheduler.refreshCode("regist", true);
            return inserted;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean login(LoginDTO loginDTO) {
        AdminPO admin = getById(loginDTO.getId());
        return admin != null && passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword());
    }

    @Override
    public boolean update(AdminUpdateDTO adminUpdateDTO) {
        AdminPO admin = AdminConverter.INSTANCE.toPO(adminUpdateDTO);
        return updateById(admin);
    }

    @Override
    public boolean changePwd(Long id, PwdChangeDTO pwdChangeDTO) {
        AdminPO admin = getById(id);
        if (admin == null)
            throw new CommonException("用户不存在");
        if (!passwordEncoder.matches(pwdChangeDTO.getOldPassword(), admin.getPassword()))
            throw new CommonException("旧密码错误");
        admin.setPassword(passwordEncoder.encode(pwdChangeDTO.getNewPassword()));
        return updateById(admin);
    }
}
