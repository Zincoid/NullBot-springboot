package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.query.GroupQuery;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.basic.GroupService;
import com.zincoid.nullbot.core.util.CsvUtil;
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
    public WebResult getList() {
        return WebResult.success("查询成功").withData("groups", groupService.list());
    }

    @GetMapping("/page")
    public WebResult getPage(GroupQuery query) {
        PageResult<GroupPO> groupPage = groupService.getPage(query);
        return WebResult.success("查询成功").withData("groupPage", groupPage);
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Long id) {
        if (groupService.removeById(id)) {
            return WebResult.success("删除成功");
        } else {
            return WebResult.fail("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody GroupPO group) {
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
