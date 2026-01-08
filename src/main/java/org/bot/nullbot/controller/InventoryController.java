package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.InventoryService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/nullbot/inventory")
@Slf4j
@RequiredArgsConstructor
public class InventoryController
{
    private final InventoryService inventoryService;

    @GetMapping("/list")
    public WebResult getInventoryList(Long userId){
        return WebResult.success().addMsg("查询成功").addData("inventories", inventoryService.getInventories(userId));
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Integer id){
        if(inventoryService.deleteById(id)){
            return WebResult.success().addMsg("删除成功");
        }else{
            return WebResult.fail().addMsg("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody InventoryPO inventory){
        if(inventoryService.updateInventory(inventory))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新失败");
    }
}
