package com.zincoid.nullbot.core.service.system.impl;

import com.zincoid.nullbot.core.module.security.JwtTool;
import com.zincoid.nullbot.web.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.module.security.SecurityCodeScheduler;
import com.zincoid.nullbot.core.converter.AdminConverter;
import com.zincoid.nullbot.core.model.data.po.AdminPO;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.exception.CoreException;
import com.zincoid.nullbot.core.model.data.dto.AdminDTO;
import com.zincoid.nullbot.core.model.data.dto.LoginDTO;
import com.zincoid.nullbot.core.model.data.dto.PasswordDTO;
import com.zincoid.nullbot.core.model.data.dto.RegistDTO;
import com.zincoid.nullbot.core.service.base.UserService;
import com.zincoid.nullbot.core.service.system.AdminService;
import com.zincoid.nullbot.core.service.system.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminService adminService;
    private final SecurityCodeScheduler securityCodeScheduler;
    private final PasswordEncoder passwordEncoder;
    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    @Override
    public void regist(RegistDTO regist) {
        if (!securityCodeScheduler.validate("regist", regist.getActivationCode()))
            throw new CoreException("激活码错误");
        UserPO user = userService.getById(regist.getId());
        if (user == null)
            throw new CoreException("未知用户 (未使用过 NullBot)");
        if (adminService.getById(regist.getId()) != null)
            throw new CoreException("用户已注册");
        AdminPO newAdmin = AdminConverter.INSTANCE.toPO(user);
        newAdmin.setEmail(regist.getEmail());
        newAdmin.setPassword(passwordEncoder.encode(regist.getPassword()));
        if (!adminService.save(newAdmin))
            throw new CoreException("注册失败");
        securityCodeScheduler.refresh("regist", true);
    }

    @Override
    public String login(LoginDTO login) {
        AdminPO admin = adminService.getById(login.getId());
        if (admin == null || !passwordEncoder.matches(login.getPassword(), admin.getPassword()))
            throw new CoreException("登录失败");
        return jwtTool.createJwt(
                login.getId(), 1,
                jwtProperties.getTokenTTL()
        );
    }

    @Override
    public String guest() {
        return jwtTool.createJwt(
                null, 0,
                jwtProperties.getTokenTTL()
        );
    }

    @Override
    public void update(AdminDTO adminDTO) {
        AdminPO admin = AdminConverter.INSTANCE.toPO(adminDTO);
        if (!adminService.updateById(admin))
            throw new CoreException("更新失败");
    }

    @Override
    public void delete(Long id) {
        if (!adminService.removeById(id))
            throw new CoreException("注销失败");
    }

    @Override
    public void changePassword(Long id, PasswordDTO password) {
        AdminPO admin = adminService.getById(id);
        if (admin == null)
            throw new CoreException("用户不存在");
        if (!passwordEncoder.matches(password.getOldPassword(), admin.getPassword()))
            throw new CoreException("旧密码错误");
        admin.setPassword(passwordEncoder.encode(password.getNewPassword()));
        if (!adminService.updateById(admin))
            throw new CoreException("更改失败");
    }

    @Override
    public Map<String, Object> info(Integer type, Long id) {
        AdminPO admin;
        if (type == 0) {
            admin = new AdminPO(null, "Guest", null, null);
        } else if (type == 1) {
            admin = adminService.getById(id);
            if (admin == null)
                throw new CoreException("未知用户");
            admin.setPassword(null);
        } else throw new CoreException("未知类型");
        Map<String, Object> data = new HashMap<>();
        data.put("info", admin);
        data.put("userType", type);
        return data;
    }
}
