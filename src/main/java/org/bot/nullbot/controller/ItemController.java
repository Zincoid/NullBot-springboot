package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.page.ItemPage;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.ItemService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/nullbot/item")
@Slf4j
@RequiredArgsConstructor
public class ItemController
{
    private final ItemService itemService;

    @GetMapping("/list/{currentPage}/{pageSize}")
    public WebResult getItemByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
        ItemPage itemPage = itemService.getItemByPage(currentPage, pageSize, null);
        return WebResult.success().addMsg("查询成功").addData("itemPage", itemPage);
    }

    @PutMapping("/updateItem")
    public WebResult updateItem(@RequestBody ItemPO item){
        if(itemService.updateItem(item))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新出错");
    }
}
