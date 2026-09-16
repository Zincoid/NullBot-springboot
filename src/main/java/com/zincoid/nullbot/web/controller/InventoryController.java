package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.dto.InventoryDTO;
import com.zincoid.nullbot.core.context.WebCtx;
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
@RequestMapping("/nullbot/inventories")
@RestController
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public WebResult<List<InventoryVO>> getInventoryList(@RequestParam Long userId) {
        List<InventoryVO> inventories = inventoryService.listVO(userId);
        return WebResult.success("查询成功", inventories);
    }

    @PostMapping
    public WebResult<Void> add(@RequestParam Long userId, @RequestParam Integer itemId) {
        WebCtx.requireAdmin();
        inventoryService.increase(userId, itemId);
        return WebResult.success("增加成功");
    }

    @DeleteMapping("/{id}")
    public WebResult<Void> delete(@PathVariable Integer id) {
        WebCtx.requireAdmin();
        inventoryService.delete(id);
        return WebResult.success("删除成功");
    }

    @PutMapping("/{id}")
    public WebResult<Void> update(@PathVariable Integer id, @RequestBody @Valid InventoryDTO inventory) {
        WebCtx.requireAdmin();
        inventory.setId(id);
        inventoryService.update(inventory);
        return WebResult.success("更新成功");
    }

    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        WebCtx.requireAdmin();
        List<InventoryPO> inventories = inventoryService.list();
        CsvUtil.exportCsv(response, "Inventories_" + LocalDateTime.now(), inventories, InventoryPO.class);
    }

    @PostMapping("/import")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        WebCtx.requireAdmin();
        List<InventoryPO> inventories = CsvUtil.importCsv(csvFile, InventoryPO.class);
        inventoryService.saveBatch(inventories);
    }
}
