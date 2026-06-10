package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.query.ItemQuery;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.basic.ItemService;
import com.zincoid.nullbot.core.utils.CsvUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/item")
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/list")
    public WebResult<List<ItemPO>> getList() {
        return WebResult.success("查询成功", itemService.list());
    }

    @GetMapping("/page")
    public WebResult<PageResult<ItemPO>> getPage(ItemQuery query) {
        PageResult<ItemPO> itemPage = itemService.page(query);
        return WebResult.success("查询成功", itemPage);
    }

    @PostMapping("/add")
    public WebResult<Void> add(@RequestBody ItemPO item) {
        if (itemService.save(item)) {
            return WebResult.success("新增成功");
        } else {
            return WebResult.fail("新增失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public WebResult<Void> delete(@PathVariable Integer id) {
        if (itemService.removeById(id)) {
            return WebResult.success("删除成功");
        } else {
            return WebResult.fail("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult<Void> update(@RequestBody ItemPO item) {
        if (itemService.updateById(item)) {
            return WebResult.success("更新成功");
        } else {
            return WebResult.fail("更新失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<ItemPO> items = itemService.list();
        CsvUtil.exportCsv(response, "Items_" + LocalDateTime.now(), items, ItemPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<ItemPO> items = CsvUtil.importCsv(csvFile, ItemPO.class);
        itemService.saveBatch(items);
    }
}
