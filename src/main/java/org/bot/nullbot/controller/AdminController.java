package org.bot.nullbot.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.service.AdminService;
import org.bot.nullbot.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/nullbot")
@Slf4j
@RequiredArgsConstructor
public class AdminController
{
    private final AdminService adminService;

    @PostMapping("/login")
        public WebResult login(@RequestBody LoginDTO loginDTO){
        log.info("[管理系统] 用户登录 - {}", loginDTO);

        if(adminService.login(loginDTO)){
            Map<String, Object> claims = new HashMap<>();
            claims.put("id",loginDTO.getId());

            String jwt = JwtUtil.generateJwt(claims);
            return WebResult.success().addMsg("登录成功").addData("token",jwt);
        }
        return WebResult.fail().addMsg("登录失败！");
    }

    @GetMapping("/info")
    public WebResult userInfo(@RequestHeader("token") String token){
        Claims claims = JwtUtil.parseJWT(token);
        Long id = claims.get("id", Long.class);
        log.info("[管理系统] 获取信息 - ID {}", id);
        AdminPO admin = adminService.info(id);
        if(admin != null){
            return WebResult.success().addMsg("获取用户信息成功").addData("info", admin);
        }else{
            return WebResult.fail().addMsg("获取用户信息失败");
        }
    }
}
