package com.zincoid.nullbot.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.data.DataPage;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.ItemService;
import com.zincoid.nullbot.core.util.CsvUtil;
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
    public WebResult getItemList() {
        return WebResult.success("查询成功").withData("items", itemService.getList());
    }

    @GetMapping("/page/{currentPage}/{pageSize}")
    public WebResult getItemByPage(
            @PathVariable Integer currentPage,
            @PathVariable Integer pageSize
    ) {
        DataPage<ItemPO> itemPage = itemService.getPage(currentPage, pageSize);
        return WebResult.success("查询成功").withData("itemPage", itemPage);
    }

    @PostMapping("/add")
    public WebResult add(@RequestBody ItemPO item) {
        if (itemService.add(item)) {
            return WebResult.success("新增成功");
        } else {
            return WebResult.fail("新增失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Integer id) {
        if (itemService.deleteById(id)) {
            return WebResult.success("删除成功");
        } else {
            return WebResult.fail("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody ItemPO item) {
        if (itemService.update(item)) {
            return WebResult.success("更新成功");
        } else {
            return WebResult.fail("更新失败");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<ItemPO> items = itemService.getList();
        CsvUtil.exportCsv(response, "Items_" + LocalDateTime.now(), items, ItemPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<ItemPO> items = CsvUtil.importCsv(csvFile, ItemPO.class);
        itemService.adds(items);
    }
}
