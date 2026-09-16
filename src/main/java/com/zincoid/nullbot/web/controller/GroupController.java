package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.dto.GroupDTO;
import com.zincoid.nullbot.core.model.data.query.GroupQuery;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.context.WebCtx;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.base.GroupService;
import com.zincoid.nullbot.core.utils.CsvUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/groups")
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public WebResult<List<GroupPO>> list() {
        return WebResult.success("查询成功", groupService.list());
    }

    @GetMapping("/page")
    public WebResult<PageResult<GroupPO>> page(GroupQuery query) {
        PageResult<GroupPO> groupPage = groupService.page(query);
        return WebResult.success("查询成功", groupPage);
    }

    @DeleteMapping("/{id}")
    public WebResult<Void> delete(@PathVariable Long id) {
        WebCtx.requireAdmin();
        groupService.delete(id);
        return WebResult.success("删除成功");
    }

    @PutMapping("/{id}")
    public WebResult<Void> update(@PathVariable Long id, @RequestBody @Valid GroupDTO group) {
        WebCtx.requireAdmin();
        group.setId(id);
        groupService.update(group);
        return WebResult.success("更新成功");
    }

    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        WebCtx.requireAdmin();
        List<GroupPO> groups = groupService.list();
        CsvUtil.exportCsv(response, "Groups_" + LocalDateTime.now(), groups, GroupPO.class);
    }

    @PostMapping("/import")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        WebCtx.requireAdmin();
        List<GroupPO> groups = CsvUtil.importCsv(csvFile, GroupPO.class);
        groupService.saveBatch(groups);
    }
}
