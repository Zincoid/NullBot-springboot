package org.bot.nullbot.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.service.LoginService;
import org.bot.nullbot.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/nullbot")
@Slf4j
@RequiredArgsConstructor
public class LoginController
{
    private final LoginService adminService;

    @PostMapping("/guest")
    public WebResult guest(){
        log.info("[管理系统] 访客登录");

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", 0);

        String jwt = JwtUtil.generateJwt(claims);
        return WebResult.success().addMsg("访客登录成功").addData("token",jwt);
    }

    @PostMapping("/login")
        public WebResult login(@RequestBody LoginDTO loginDTO){
        log.info("[管理系统] 管理员登录 - {}", loginDTO);

        if(adminService.login(loginDTO)){
            Map<String, Object> claims = new HashMap<>();
            claims.put("id",loginDTO.getId());
            claims.put("type", 1);

            String jwt = JwtUtil.generateJwt(claims);
            return WebResult.success().addMsg("管理员登录成功").addData("token",jwt);
        }
        return WebResult.fail().addMsg("管理员登录失败");
    }

    @GetMapping("/info")
    public WebResult info(@RequestHeader("token") String token){
        Claims claims = JwtUtil.parseJWT(token);
        if(claims.get("type", Integer.class) == 0){
            AdminPO admin = new AdminPO(null, "Guest", "看访客密码？", "访客无此信息");
            log.info("[管理系统] 获取访客信息");
            return WebResult.success().addMsg("获取访客信息成功").addData("info", admin);
        }else if(claims.get("type", Integer.class) == 1){
            Long id = claims.get("id", Long.class);
            log.info("[管理系统] 获取管理员信息 - ID {}", id);
            AdminPO admin = adminService.info(id);
            if(admin != null){
                admin.setPassword("不能给你看");  // 安全
                return WebResult.success().addMsg("获取管理员信息成功").addData("info", admin);
            }else{
                return WebResult.fail().addMsg("获取管理员信息失败");
            }
        }
        return WebResult.fail().addMsg("用户类型不存在");
    }
}
