package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.dto.ItemDTO;
import com.zincoid.nullbot.core.context.WebCtx;
import com.zincoid.nullbot.core.model.data.query.ItemQuery;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.base.ItemService;
import com.zincoid.nullbot.core.utils.CsvUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/items")
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public WebResult<List<ItemPO>> getList() {
        return WebResult.success("查询成功", itemService.list());
    }

    @GetMapping("/page")
    public WebResult<PageResult<ItemPO>> getPage(ItemQuery query) {
        PageResult<ItemPO> itemPage = itemService.page(query);
        return WebResult.success("查询成功", itemPage);
    }

    @PostMapping
    public WebResult<Void> add(@RequestBody @Valid ItemDTO item) {
        WebCtx.requireAdmin();
        itemService.add(item);
        return WebResult.success("新增成功");
    }

    @DeleteMapping("/{id}")
    public WebResult<Void> delete(@PathVariable Integer id) {
        WebCtx.requireAdmin();
        itemService.delete(id);
        return WebResult.success("删除成功");
    }

    @PutMapping("/{id}")
    public WebResult<Void> update(@PathVariable Integer id, @RequestBody @Valid ItemDTO item) {
        WebCtx.requireAdmin();
        item.setId(id);
        itemService.update(item);
        return WebResult.success("更新成功");
    }

    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        WebCtx.requireAdmin();
        List<ItemPO> items = itemService.list();
        CsvUtil.exportCsv(response, "Items_" + LocalDateTime.now(), items, ItemPO.class);
    }

    @PostMapping("/import")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        WebCtx.requireAdmin();
        List<ItemPO> items = CsvUtil.importCsv(csvFile, ItemPO.class);
        itemService.saveBatch(items);
    }
}
