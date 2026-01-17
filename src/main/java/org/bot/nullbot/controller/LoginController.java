package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.component.security.JwtTool;
import org.bot.nullbot.config.prop.JwtProperties;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.service.AdminService;
import org.bot.nullbot.util.JwtUtil;
import org.bot.nullbot.util.WebUtil;
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
    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;
    private final AdminService adminService;

    @PostMapping("/guest")
    public WebResult guest(){
        log.info("[管理系统] 访客登录");
        String jwt = jwtTool.createJwt(0L, 0, jwtProperties.getTokenTTL());
        return WebResult.success().addMsg("访客登录成功").addData("token", jwt);
    }

    @PostMapping("/login")
        public WebResult login(@RequestBody LoginDTO loginDTO){
        log.info("[管理系统] 管理员登录 - {}", loginDTO);
        if(adminService.login(loginDTO)){
            String jwt = jwtTool.createJwt(loginDTO.getId(), 1, jwtProperties.getTokenTTL());
            return WebResult.success().addMsg("管理员登录成功").addData("token", jwt);
        }
        return WebResult.fail().addMsg("管理员登录失败");
    }

    @GetMapping("/info")
    public WebResult info(){
        Integer type = jwtTool.getLoginType(WebUtil.getToken());
        if(type == 0){
            AdminPO admin = new AdminPO(null, "Guest", "看访客密码？", "访客无此信息");
            log.info("[管理系统] 获取访客信息");
            return WebResult.success().addMsg("获取访客信息成功").addData("info", admin).addData("userType", 0);
        }else if(type == 1){
            Long id = jwtTool.getLoginId(WebUtil.getToken());
            log.info("[管理系统] 获取管理员信息 - ID {}", id);
            AdminPO admin = adminService.info(id);
            if(admin != null){
                admin.setPassword("不能给你看");  // 安全
                return WebResult.success().addMsg("获取管理员信息成功").addData("info", admin).addData("userType", 1);
            }else{
                return WebResult.fail().addMsg("获取管理员信息失败");
            }
        }
        return WebResult.fail().addMsg("用户类型不存在");
    }
}
