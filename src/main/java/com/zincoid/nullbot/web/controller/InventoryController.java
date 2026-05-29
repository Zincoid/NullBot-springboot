package com.zincoid.nullbot.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import com.zincoid.nullbot.core.util.CsvUtil;
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
    public WebResult getInventoryList(Long userId) {
        List<InventoryVO> inventories = inventoryService.getVOList(userId);
        return WebResult.success("查询成功").withData("inventories", inventories);
    }

    @PostMapping("/add")
    public WebResult add(Long userId, Integer itemId) {
        if (inventoryService.increase(userId, itemId, 1)) {
            return WebResult.success("增加成功");
        } else {
            return WebResult.fail("增加失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Integer id) {
        if (inventoryService.deleteById(id)) {
            return WebResult.success("删除成功");
        } else {
            return WebResult.fail("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody InventoryPO inventory) {
        if (inventoryService.update(inventory)) {
            return WebResult.success("更新成功");
        } else {
            return WebResult.fail("更新失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<InventoryPO> inventories = inventoryService.getList();
        CsvUtil.exportCsv(response, "Inventories_" + LocalDateTime.now(), inventories, InventoryPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<InventoryPO> inventories = CsvUtil.importCsv(csvFile, InventoryPO.class);
        inventoryService.adds(inventories);
    }
}
