package com.zincoid.nullbot.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.component.security.JwtTool;
import com.zincoid.nullbot.core.properties.security.JwtProperties;
import com.zincoid.nullbot.core.model.data.dto.AdminUpdateDTO;
import com.zincoid.nullbot.core.model.data.dto.PwdChangeDTO;
import com.zincoid.nullbot.core.model.data.dto.RegistDTO;
import com.zincoid.nullbot.core.model.data.po.AdminPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.model.data.dto.LoginDTO;
import com.zincoid.nullbot.core.service.system.AdminService;
import com.zincoid.nullbot.core.context.WebCtx;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Validated  // 表单校验 仅方法普通参数  @RequestBody 参数需额外在字段上添加 @Validated 注解
@RequestMapping("/nullbot")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;
    private final AdminService adminService;

    @PostMapping("/regist")
    public WebResult<Void> regist(@RequestBody @Validated RegistDTO registDTO) {
        log.info("└─[LoginController] 管理员注册 - {}", registDTO);
        if (adminService.regist(registDTO)) {
            return WebResult.success("管理员注册成功");
        } else {
            return WebResult.fail("管理员注册失败");
        }
    }

    @PostMapping("/guest")
    public WebResult<String> guest() {
        log.info("└─[LoginController] 访客登录");
        String token = jwtTool.createJwt(
                null, 0,
                jwtProperties.getTokenTTL()
        );
        return WebResult.success("访客登录成功", token);
    }

    @PostMapping("/login")
    public WebResult<String> login(@RequestBody @Validated LoginDTO loginDTO) {
        log.info("└─[LoginController] 管理员登录 - {}", loginDTO);
        if (adminService.login(loginDTO)) {
            String token = jwtTool.createJwt(
                    loginDTO.getId(), 1,
                    jwtProperties.getTokenTTL()
            );
            return WebResult.success("管理员登录成功", token);
        } else {
            return WebResult.fail("用户名或密码错误");
        }
    }

    @DeleteMapping("/delete")
    public WebResult<Void> delete() {
        Long id = WebCtx.getId();
        log.info("└─[LoginController] 管理员注销 - ID: {}", id);
        if (adminService.removeById(id)) {
            return WebResult.success("管理员注销成功");
        } else {
            return WebResult.fail("管理员注销失败");
        }
    }

    @PostMapping("/update")
    public WebResult<Void> update(@RequestBody @Validated AdminUpdateDTO adminUpdateDTO) {
        Long id = WebCtx.getId();
        adminUpdateDTO.setId(id);
        log.info("└─[LoginController] 管理员更新 - ID: {}", id);
        if (adminService.update(adminUpdateDTO)) {
            return WebResult.success("管理员更新成功");
        } else {
            return WebResult.fail("管理员更新失败");
        }
    }

    @PostMapping("/changePwd")
    public WebResult<Void> changePwd(@RequestBody @Validated PwdChangeDTO pwdChangeDTO) {
        Long id = WebCtx.getId();
        log.info("└─[LoginController] 管理员密码更改 - ID: {}", id);
        if (adminService.changePwd(id, pwdChangeDTO)) {
            return WebResult.success("管理员密码更改成功");
        } else {
            return WebResult.fail("管理员密码更改失败");
        }
    }

    @GetMapping("/info")
    public WebResult<Map<String, Object>> info() {
        Integer type = WebCtx.getType();
        if (type == 0) {
            AdminPO admin = new AdminPO(
                    null, "Guest",
                    null, null
            );
            log.info("└─[LoginController] 获取访客信息");
            Map<String, Object> data = new HashMap<>();
            data.put("info", admin);
            data.put("userType", 0);
            return WebResult.success("获取访客信息成功", data);
        } else if (type == 1) {
            Long id = WebCtx.getId();
            log.info("└─[LoginController] 获取管理员信息 - ID: {}", id);
            AdminPO admin = adminService.getById(id);
            if (admin != null) {
                admin.setPassword(null);  // 安全
                Map<String, Object> data = new HashMap<>();
                data.put("info", admin);
                data.put("userType", 1);
                return WebResult.success("获取管理员信息成功", data);
            } else {
                return WebResult.fail("获取管理员信息失败");
            }
        }
        return WebResult.fail("用户类型不存在");
    }
}
