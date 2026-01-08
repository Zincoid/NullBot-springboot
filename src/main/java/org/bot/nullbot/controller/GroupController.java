package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.page.GroupPage;
import org.bot.nullbot.entity.po.GroupPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.GroupService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/nullbot/group")
@RequiredArgsConstructor
@Slf4j
public class GroupController
{
    private final GroupService groupService;

    @GetMapping("/list/{currentPage}/{pageSize}")
    public WebResult getGroupByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
        GroupPage groupPage = groupService.getGroupByPage(currentPage, pageSize);
        return WebResult.success().addMsg("查询成功").addData("groupPage", groupPage);
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody GroupPO group){
        if(groupService.updateGroup(group))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新出错");
    }
}
