package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.dto.InventoryDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
        inventoryService.increase(userId, itemId);
        return WebResult.success("增加成功");
    }

    @DeleteMapping("/delete/{id}")
    public WebResult<Void> delete(@PathVariable Integer id) {
        inventoryService.delete(id);
        return WebResult.success("删除成功");
    }

    @PutMapping("/update")
    public WebResult<Void> update(@RequestBody @Valid InventoryDTO inventory) {
        inventoryService.update(inventory);
        return WebResult.success("更新成功");
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
