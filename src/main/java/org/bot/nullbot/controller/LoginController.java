package org.bot.nullbot.controller;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.WebResult;
import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/nullbot")
@Slf4j
public class LoginController
{
    @PostMapping("/login")
        public WebResult login(@RequestBody LoginDTO loginDTO){
        log.info("[管理系统] 用户登录 - {}", loginDTO);

        if(loginDTO.getId() == 2660181154L && "Zincoid".equals(loginDTO.getPassword())){
            Map<String, Object> claims = new HashMap<>();
            claims.put("id",loginDTO.getId());

            String jwt = JwtUtil.generateJwt(claims);
            return WebResult.success().addMsg("登录成功").addData("token",jwt);
        }
        return WebResult.fail().addMsg("你不是Zincoid！");
    }
}
