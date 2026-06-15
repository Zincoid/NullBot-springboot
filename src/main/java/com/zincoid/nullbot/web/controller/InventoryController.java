package com.zincoid.nullbot.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.core.service.base.InventoryService;
import com.zincoid.nullbot.core.utils.CsvUtil;
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
    public WebResult<List<InventoryVO>> getInventoryList(Long userId) {
        List<InventoryVO> inventories = inventoryService.listVO(userId);
        return WebResult.success("查询成功", inventories);
    }

    @PostMapping("/add")
    public WebResult<Void> add(Long userId, Integer itemId) {
        if (inventoryService.add(userId, itemId, 1)) {
            return WebResult.success("增加成功");
        } else {
            return WebResult.fail("增加失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public WebResult<Void> delete(@PathVariable Integer id) {
        if (inventoryService.removeById(id)) {
            return WebResult.success("删除成功");
        } else {
            return WebResult.fail("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult<Void> update(@RequestBody InventoryPO inventory) {
        if (inventoryService.updateById(inventory)) {
            return WebResult.success("更新成功");
        } else {
            return WebResult.fail("更新失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<InventoryPO> inventories = inventoryService.list();
        CsvUtil.exportCsv(response, "Inventories_" + LocalDateTime.now(), inventories, InventoryPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<InventoryPO> inventories = CsvUtil.importCsv(csvFile, InventoryPO.class);
        inventoryService.saveBatch(inventories);
    }
}
