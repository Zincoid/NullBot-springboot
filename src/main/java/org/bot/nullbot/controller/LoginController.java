package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.component.security.JwtTool;
import org.bot.nullbot.config.prop.JwtProperties;
import org.bot.nullbot.entity.dto.PwdChangeDTO;
import org.bot.nullbot.entity.dto.RegistDTO;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.service.AdminService;
import org.bot.nullbot.util.WebUtil;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/regist")
    public WebResult regist(@RequestBody RegistDTO registDTO){
        log.info("└─[LoginController] 管理员注册 - {}", registDTO);
        try {
            if (adminService.regist(registDTO))
                return WebResult.success().addMsg("管理员注册成功");
            return WebResult.fail().addMsg("管理员注册失败");
        } catch (Exception e) {
            return WebResult.fail().addMsg("管理员注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/guest")
    public WebResult guest(){
        log.info("└─[LoginController] 访客登录");
        String jwt = jwtTool.createJwt(null, 0, jwtProperties.getTokenTTL());
        return WebResult.success().addMsg("访客登录成功").addData("token", jwt);
    }

    @PostMapping("/login")
        public WebResult login(@RequestBody LoginDTO loginDTO){
        log.info("└─[LoginController] 管理员登录 - {}", loginDTO);
        if(adminService.login(loginDTO)){
            String jwt = jwtTool.createJwt(loginDTO.getId(), 1, jwtProperties.getTokenTTL());
            return WebResult.success().addMsg("管理员登录成功").addData("token", jwt);
        }
        return WebResult.fail().addMsg("管理员登录失败");
    }

    @DeleteMapping("/delete")
    public WebResult delete(){
        Long id = jwtTool.getLoginId(WebUtil.getToken());
        log.info("└─[LoginController] 管理员注销 - ID: {}", id);
        if(adminService.delete(id))
            return WebResult.success().addMsg("管理员注销成功");
        return WebResult.fail().addMsg("管理员注销失败");
    }

    @PostMapping("/update")
    public WebResult update(@RequestBody AdminPO admin){
        Long id = jwtTool.getLoginId(WebUtil.getToken());
        admin.setId(id);  // 从 Token 获取 ID
        log.info("└─[LoginController] 管理员更新 - ID: {}", id);
        if(adminService.update(admin))
            return WebResult.success().addMsg("管理员更新成功");
        return WebResult.fail().addMsg("管理员更新失败");
    }

    @PostMapping("/changePwd")
    public WebResult changePwd(@RequestBody PwdChangeDTO pwdChangeDTO) {
        try {
            Long id = jwtTool.getLoginId(WebUtil.getToken());
            log.info("└─[LoginController] 管理员密码更改 - ID: {}", id);
            if(adminService.changePwd(id, pwdChangeDTO))
                return WebResult.success().addMsg("管理员密码更改成功");
            return WebResult.fail().addMsg("管理员密码更改失败");
        } catch (Exception e) {
            return WebResult.fail().addMsg("管理员密码更改失败: " + e.getMessage());
        }
    }

    @GetMapping("/info")
    public WebResult info(){
        Integer type = jwtTool.getLoginType(WebUtil.getToken());
        if(type == 0){
            AdminPO admin = new AdminPO(null, "Guest", null, null);
            log.info("└─[LoginController] 获取访客信息");
            return WebResult.success().addMsg("获取访客信息成功").addData("info", admin).addData("userType", 0);
        }else if(type == 1){
            Long id = jwtTool.getLoginId(WebUtil.getToken());
            log.info("└─[LoginController] 获取管理员信息 - ID {}", id);
            AdminPO admin = adminService.info(id);
            if(admin != null){
                admin.setPassword(null);  // 安全
                return WebResult.success().addMsg("获取管理员信息成功").addData("info", admin).addData("userType", 1);
            }else{
                return WebResult.fail().addMsg("获取管理员信息失败");
            }
        }
        return WebResult.fail().addMsg("用户类型不存在");
    }
}
