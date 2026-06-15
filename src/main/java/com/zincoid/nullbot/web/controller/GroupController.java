package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.query.GroupQuery;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.base.GroupService;
import com.zincoid.nullbot.core.utils.CsvUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/group")
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/list")
    public WebResult<List<GroupPO>> getList() {
        return WebResult.success("查询成功", groupService.list());
    }

    @GetMapping("/page")
    public WebResult<PageResult<GroupPO>> getPage(GroupQuery query) {
        PageResult<GroupPO> groupPage = groupService.page(query);
        return WebResult.success("查询成功", groupPage);
    }

    @DeleteMapping("/delete/{id}")
    public WebResult<Void> delete(@PathVariable Long id) {
        if (groupService.removeById(id)) {
            return WebResult.success("删除成功");
        } else {
            return WebResult.fail("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult<Void> update(@RequestBody GroupPO group) {
        if (groupService.updateById(group)) {
            return WebResult.success("更新成功");
        } else {
            return WebResult.fail("更新出错");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<GroupPO> groups = groupService.list();
        CsvUtil.exportCsv(response, "Groups_" + LocalDateTime.now(), groups, GroupPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<GroupPO> groups = CsvUtil.importCsv(csvFile, GroupPO.class);
        groupService.saveBatch(groups);
    }
}
