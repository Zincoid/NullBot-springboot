package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.security.SecurityCodeScheduler;
import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.entity.dto.PwdChangeDTO;
import org.bot.nullbot.entity.dto.RegistDTO;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.mapper.AdminMapper;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.service.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService
{
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;

    private final SecurityCodeScheduler securityCodeScheduler;
    private final PasswordEncoder passwordEncoder;

    // =================== WEB功能相关 ===================

    @Override
    public boolean regist(RegistDTO registDTO) {
        if (!StringUtils.hasLength(registDTO.getPassword()))
            throw new IllegalArgumentException("未输入密码");
        if (securityCodeScheduler.validateCode("regist", registDTO.getActivationCode()))
            throw new IllegalArgumentException("激活码错误");

        UserPO user = userMapper.selectById(registDTO.getId());
        if (user == null)
            throw new IllegalArgumentException("用户不可用 (未使用过 NullBot)");
        AdminPO admin = adminMapper.selectById(registDTO.getId());
        if (admin != null)
            throw new IllegalArgumentException("用户已注册");

        AdminPO newAdmin = new AdminPO();
        newAdmin.setId(user.getId());
        newAdmin.setUsername(user.getName());
        newAdmin.setEmail(registDTO.getEmail());
        newAdmin.setPassword(passwordEncoder.encode(registDTO.getPassword()));
        try {
            boolean inserted = adminMapper.insert(newAdmin) == 1;
            if (inserted) securityCodeScheduler.useCode("regist");
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
    public boolean delete(Long id) { return adminMapper.deleteById(id) == 1; }

    @Override
    public boolean update(AdminPO admin) { return adminMapper.updateById(admin) == 1; }

    @Override
    public boolean changePwd(Long id, PwdChangeDTO pwdChangeDTO) {
        if (!pwdChangeDTO.verify())
            throw new IllegalArgumentException("表单验证失败");
        AdminPO admin = adminMapper.selectById(id);
        if (admin == null)
            throw new IllegalArgumentException("用户不存在");
        if (!passwordEncoder.matches(pwdChangeDTO.getOldPassword(), admin.getPassword()))
            throw new IllegalArgumentException("旧密码错误");
        admin.setPassword(passwordEncoder.encode(pwdChangeDTO.getNewPassword()));
        return adminMapper.updateById(admin) == 1;
    }

    @Override
    public AdminPO info(Long id) {
        return adminMapper.selectById(id);
    }
}
