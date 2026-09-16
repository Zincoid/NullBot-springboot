package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.system.AuthService;
import com.zincoid.nullbot.core.context.WebCtx;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Validated
@RequestMapping("/nullbot")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/regist")
    public WebResult<Void> regist(@RequestBody @Validated RegistDTO regist) {
        log.info("└─[AuthController] 管理账号注册 - {}", regist.getId());
        authService.regist(regist);
        return WebResult.success("注册成功");
    }

    @PostMapping("/guest")
    public WebResult<String> guest() {
        log.info("└─[AuthController] 访客临时登录");
        String token = authService.guest();
        return WebResult.success("暂访成功", token);
    }

    @PostMapping("/login")
    public WebResult<String> login(@RequestBody @Validated LoginDTO login) {
        log.info("└─[AuthController] 管理账号登录 - {}", login.getId());
        String token = authService.login(login);
        return WebResult.success("登录成功", token);
    }

    @DeleteMapping("/delete")
    public WebResult<Void> delete() {
        Long id = WebCtx.getId();
        log.info("└─[AuthController] 管理账号注销 - ID: {}", id);
        authService.delete(id);
        return WebResult.success("注销成功");
    }

    @PostMapping("/update")
    public WebResult<Void> update(@RequestBody @Validated AdminDTO admin) {
        Long id = WebCtx.getId();
        admin.setId(id);
        log.info("└─[AuthController] 管理信息更新 - ID: {}", id);
        authService.update(admin);
        return WebResult.success("更新成功");
    }

    @PostMapping("/password")
    public WebResult<Void> changePassword(@RequestBody @Validated PasswordDTO password) {
        Long id = WebCtx.getId();
        log.info("└─[AuthController] 管理密码更改 - ID: {}", id);
        authService.changePassword(id, password);
        return WebResult.success("更改成功");
    }

    @GetMapping("/info")
    public WebResult<Map<String, Object>> info() {
        Integer type = WebCtx.getType();
        Long id = WebCtx.getId();
        Map<String, Object> data = authService.info(type, id);
        log.info("└─[AuthController] 获取账号信息 - userType: {}", data.get("userType"));
        return WebResult.success("获取成功", data);
    }
}
