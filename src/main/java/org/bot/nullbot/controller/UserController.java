package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.page.UserPage;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.UserService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/nullbot/user")
@RequiredArgsConstructor
@Slf4j
public class UserController
{
    private final UserService userService;

    @GetMapping("/list/{currentPage}/{pageSize}")
    public WebResult getUserByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
        UserPage userPage = userService.getUserByPage(currentPage, pageSize);
        return WebResult.success().addMsg("查询成功").addData("userPage", userPage);
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody UserPO user){
        if(userService.updateUser(user))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新出错");
    }
}
