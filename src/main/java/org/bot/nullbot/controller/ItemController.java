package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/list")
    public WebResult getItemList(){
        return WebResult.success().addMsg("查询成功").addData("items", itemService.getItemList());
    }

    // @GetMapping("/list/{currentPage}/{pageSize}")
    // public WebResult getItemByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
    //     ItemPage itemPage = itemService.getItemByPage(currentPage, pageSize, null);
    //     return WebResult.success().addMsg("查询成功").addData("itemPage", itemPage);
    // }

    @PostMapping("/add")
    public WebResult add(@RequestBody ItemPO item){
        try {
            if(itemService.addItem(item))
                return WebResult.success().addMsg("新增成功");
            else
                return WebResult.fail().addMsg("新增失败");
        } catch (Exception e) {
            return WebResult.fail().addMsg("新增出错");
        }
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Integer id){
        if(itemService.deleteById(id)){
            return WebResult.success().addMsg("删除成功");
        }else{
            return WebResult.fail().addMsg("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody ItemPO item){
        if(itemService.updateItem(item))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新失败");
    }
}
