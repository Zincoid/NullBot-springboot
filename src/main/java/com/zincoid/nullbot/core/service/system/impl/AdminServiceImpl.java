package com.zincoid.nullbot.core.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.model.data.dto.*;
import com.zincoid.nullbot.core.service.base.UserService;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.module.security.SecurityCodeScheduler;
import com.zincoid.nullbot.core.converter.AdminConverter;
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
    public boolean regist(RegistDTO regist) {
        if (!securityCodeScheduler.validate("regist", regist.getActivationCode()))
            throw new CommonException("激活码错误");
        UserPO user = userService.getById(regist.getId());
        if (user == null)
            throw new CommonException("用户不可用 (未使用过 NullBot)");
        AdminPO admin = getById(regist.getId());
        if (admin != null)
            throw new CommonException("用户已注册");
        AdminPO newAdmin = AdminConverter.INSTANCE.toPO(user);
        newAdmin.setEmail(regist.getEmail());
        newAdmin.setPassword(passwordEncoder.encode(regist.getPassword()));
        try {
            boolean inserted = save(newAdmin);
            if (inserted) securityCodeScheduler.refresh("regist", true);
            return inserted;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean login(LoginDTO login) {
        AdminPO admin = getById(login.getId());
        return admin != null && passwordEncoder.matches(login.getPassword(), admin.getPassword());
    }

    @Override
    public boolean update(AdminDTO adminDTO) {
        AdminPO admin = AdminConverter.INSTANCE.toPO(adminDTO);
        return updateById(admin);
    }

    @Override
    public boolean changePwd(Long id, PasswordDTO password) {
        AdminPO admin = getById(id);
        if (admin == null)
            throw new CommonException("用户不存在");
        if (!passwordEncoder.matches(password.getOldPassword(), admin.getPassword()))
            throw new CommonException("旧密码错误");
        admin.setPassword(passwordEncoder.encode(password.getNewPassword()));
        return updateById(admin);
    }
}
