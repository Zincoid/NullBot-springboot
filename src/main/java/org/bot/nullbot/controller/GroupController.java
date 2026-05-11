package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.GroupPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.GroupService;
import org.bot.nullbot.util.CsvExportUtil;
import org.bot.nullbot.util.CsvImportUtil;
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
    public WebResult getGroupList(){
        return WebResult.success().addMsg("查询成功").addData("groups", groupService.getGroupList());
    }

    @GetMapping("/page/{currentPage}/{pageSize}")
    public WebResult getGroupByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
        DataPage<GroupPO> groupPage = groupService.getGroupByPage(currentPage, pageSize);
        return WebResult.success().addMsg("查询成功").addData("groupPage", groupPage);
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Long id){
        if(groupService.deleteById(id)){
            return WebResult.success().addMsg("删除成功");
        }else{
            return WebResult.fail().addMsg("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody GroupPO group){
        if(groupService.updateGroup(group))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新出错");
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException, IllegalAccessException {
        List<GroupPO> groups = groupService.getGroupList();
        CsvExportUtil.exportToCsv(response, "Groups_" + LocalDateTime.now(), groups, GroupPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<GroupPO> groups =  CsvImportUtil.importFromCsv(csvFile, GroupPO.class);
        groupService.addGroups(groups);
    }
}
