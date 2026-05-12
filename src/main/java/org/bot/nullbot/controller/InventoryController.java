package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.util.CsvExportUtil;
import org.bot.nullbot.util.CsvImportUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/inventory")
@RestController
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/list")
    public WebResult getInventoryList(Long userId){
        return WebResult.success().addMsg("查询成功").addData("inventories", inventoryService.getVOList(userId));
    }

    @PostMapping("/add")
    public WebResult add(Long userId, Integer itemId){
        if(inventoryService.increase(userId, itemId, 1))
            return WebResult.success().addMsg("增加成功");
        else
            return WebResult.fail().addMsg("增加失败");
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
        if(inventoryService.update(inventory))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新失败");
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException, IllegalAccessException {
        List<InventoryPO> inventories = inventoryService.getAll();
        CsvExportUtil.exportToCsv(response, "Inventories_" + LocalDateTime.now(), inventories, InventoryPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<InventoryPO> inventories =  CsvImportUtil.importFromCsv(csvFile, InventoryPO.class);
        inventoryService.add(inventories);
    }
}
