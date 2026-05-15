package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.component.security.JwtTool;
import org.bot.nullbot.config.prop.JwtProperties;
import org.bot.nullbot.entity.dto.AdminUpdateDTO;
import org.bot.nullbot.entity.dto.PwdChangeDTO;
import org.bot.nullbot.entity.dto.RegistDTO;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.service.AdminService;
import org.bot.nullbot.util.UserCtxUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public WebResult regist(@RequestBody @Validated RegistDTO registDTO) {
        log.info("└─[LoginController] 管理员注册 - {}", registDTO);
        if (adminService.regist(registDTO)) {
            return WebResult.success("管理员注册成功");
        } else {
            return WebResult.fail("管理员注册失败");
        }
    }

    @PostMapping("/guest")
    public WebResult guest() {
        log.info("└─[LoginController] 访客登录");
        String token = jwtTool.createJwt(
                null, 0,
                jwtProperties.getTokenTTL()
        );
        return WebResult.success("访客登录成功").withData("token", token);
    }

    @PostMapping("/login")
    public WebResult login(@RequestBody @Validated LoginDTO loginDTO) {
        log.info("└─[LoginController] 管理员登录 - {}", loginDTO);
        if (adminService.login(loginDTO)) {
            String token = jwtTool.createJwt(
                    loginDTO.getId(), 1,
                    jwtProperties.getTokenTTL()
            );
            return WebResult.success("管理员登录成功").withData("token", token);
        } else {
            return WebResult.fail("用户名或密码错误");
        }
    }

    @DeleteMapping("/delete")
    public WebResult delete() {
        // Long id = jwtTool.getLoginId(WebUtil.getToken());  // 弃用
        Long id = UserCtxUtil.getId();
        log.info("└─[LoginController] 管理员注销 - ID: {}", id);
        if (adminService.deleteById(id)) {
            return WebResult.success("管理员注销成功");
        } else {
            return WebResult.fail("管理员注销失败");
        }
    }

    @PostMapping("/update")
    public WebResult update(@RequestBody @Validated AdminUpdateDTO adminUpdateDTO) {
        // Long id = jwtTool.getLoginId(WebUtil.getToken());  // 弃用
        Long id = UserCtxUtil.getId();
        adminUpdateDTO.setId(id);
        log.info("└─[LoginController] 管理员更新 - ID: {}", id);
        if (adminService.update(adminUpdateDTO)) {
            return WebResult.success("管理员更新成功");
        } else {
            return WebResult.fail("管理员更新失败");
        }
    }

    @PostMapping("/changePwd")
    public WebResult changePwd(@RequestBody @Validated PwdChangeDTO pwdChangeDTO) {
        // Long id = jwtTool.getLoginId(WebUtil.getToken());  // 弃用
        Long id = UserCtxUtil.getId();
        log.info("└─[LoginController] 管理员密码更改 - ID: {}", id);
        if (adminService.changePwd(id, pwdChangeDTO)) {
            return WebResult.success("管理员密码更改成功");
        } else {
            return WebResult.fail("管理员密码更改失败");
        }
    }

    @GetMapping("/info")
    public WebResult info() {
        // Integer type = jwtTool.getLoginType(WebUtil.getToken());  // 弃用
        Integer type = UserCtxUtil.getType();
        if (type == 0) {
            AdminPO admin = new AdminPO(
                    null, "Guest",
                    null, null
            );
            log.info("└─[LoginController] 获取访客信息");
            return WebResult
                    .success("获取访客信息成功")
                    .withData("info", admin)
                    .withData("userType", 0);
        } else if (type == 1) {
            // Long id = jwtTool.getLoginId(WebUtil.getToken());  // 弃用
            Long id = UserCtxUtil.getId();
            log.info("└─[LoginController] 获取管理员信息 - ID {}", id);
            AdminPO admin = adminService.info(id);
            if (admin != null) {
                admin.setPassword(null);  // 安全
                return WebResult
                        .success("获取管理员信息成功")
                        .withData("info", admin)
                        .withData("userType", 1);
            } else {
                return WebResult.fail("获取管理员信息失败");
            }
        }
        return WebResult.fail("用户类型不存在");
    }
}
