package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.UserService;
import org.bot.nullbot.util.CsvExportUtil;
import org.bot.nullbot.util.CsvImportUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public WebResult getUserList(){
        return WebResult.success().addMsg("查询成功").addData("users", userService.getAll());
    }

    @GetMapping("/page/{currentPage}/{pageSize}")
    public WebResult getUserByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
        DataPage<UserPO> userPage = userService.getPage(currentPage, pageSize);
        return WebResult.success().addMsg("查询成功").addData("userPage", userPage);
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Integer id){
        if(userService.delete(id)){
            return WebResult.success().addMsg("删除成功");
        }else{
            return WebResult.fail().addMsg("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody UserPO user){
        if(userService.update(user))
            return WebResult.success().addMsg("更新成功");
        else
            return WebResult.fail().addMsg("更新出错");
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException, IllegalAccessException {
        List<UserPO> users = userService.getAll();
        CsvExportUtil.exportToCsv(response, "Users_" + LocalDateTime.now(), users, UserPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<UserPO> users =  CsvImportUtil.importFromCsv(csvFile, UserPO.class);
        userService.adds(users);
    }
}
