package com.zincoid.nullbot.core.service.system.impl;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.component.security.SecurityCodeScheduler;
import com.zincoid.nullbot.core.converter.AdminConverter;
import com.zincoid.nullbot.core.model.data.dto.AdminUpdateDTO;
import com.zincoid.nullbot.core.model.data.dto.LoginDTO;
import com.zincoid.nullbot.core.model.data.dto.PwdChangeDTO;
import com.zincoid.nullbot.core.model.data.dto.RegistDTO;
import com.zincoid.nullbot.core.model.data.po.AdminPO;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.web.exception.CommonException;
import com.zincoid.nullbot.core.mapper.AdminMapper;
import com.zincoid.nullbot.core.mapper.UserMapper;
import com.zincoid.nullbot.core.service.system.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;

    private final SecurityCodeScheduler securityCodeScheduler;
    private final PasswordEncoder passwordEncoder;

    // =================== WEB功能相关 ===================

    @Override
    public boolean regist(RegistDTO registDTO) {
        if (!securityCodeScheduler.validateCode("regist", registDTO.getActivationCode()))
            throw new CommonException("激活码错误");

        UserPO user = userMapper.selectById(registDTO.getId());
        if (user == null)
            throw new CommonException("用户不可用 (未使用过 NullBot)");
        AdminPO admin = adminMapper.selectById(registDTO.getId());
        if (admin != null)
            throw new CommonException("用户已注册");

        AdminPO newAdmin = AdminConverter.INSTANCE.toPO(user);
        newAdmin.setEmail(registDTO.getEmail());
        newAdmin.setPassword(passwordEncoder.encode(registDTO.getPassword()));

        try {
            boolean inserted = adminMapper.insert(newAdmin) == 1;
            if (inserted) securityCodeScheduler.refreshCode("regist", true);
            return inserted;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean login(LoginDTO loginDTO) {
        AdminPO admin = adminMapper.selectById(loginDTO.getId());
        return admin != null && passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword());
    }

    @Override
    public boolean deleteById(Long id) { return adminMapper.deleteById(id) == 1; }

    @Override
    public boolean update(AdminUpdateDTO adminUpdateDTO) {
        AdminPO admin = AdminConverter.INSTANCE.toPO(adminUpdateDTO);
        return adminMapper.updateById(admin) == 1;
    }

    @Override
    public boolean changePwd(Long id, PwdChangeDTO pwdChangeDTO) {
        AdminPO admin = adminMapper.selectById(id);
        if (admin == null)
            throw new CommonException("用户不存在");
        if (!passwordEncoder.matches(pwdChangeDTO.getOldPassword(), admin.getPassword()))
            throw new CommonException("旧密码错误");
        admin.setPassword(passwordEncoder.encode(pwdChangeDTO.getNewPassword()));
        return adminMapper.updateById(admin) == 1;
    }

    @Override
    public AdminPO info(Long id) {
        return adminMapper.selectById(id);
    }
}
