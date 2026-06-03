package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.query.UserQuery;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.basic.UserService;
import com.zincoid.nullbot.core.util.CsvUtil;
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
    public WebResult getList() {
        return WebResult.success("查询成功").withData("users", userService.getList());
    }

    @GetMapping("/page")
    public WebResult getPage(UserQuery query) {
        PageResult<UserPO> userPage = userService.getPage(query);
        return WebResult.success("查询成功").withData("userPage", userPage);
    }

    @DeleteMapping("/delete/{id}")
    public WebResult delete(@PathVariable Long id) {
        if (userService.delete(id)) {
            return WebResult.success("删除成功");
        } else {
            return WebResult.fail("删除失败");
        }
    }

    @PutMapping("/update")
    public WebResult update(@RequestBody UserPO user) {
        if (userService.update(user)) {
            return WebResult.success("更新成功");
        } else {
            return WebResult.fail("更新出错");
        }
    }

    @GetMapping("/exportCsv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        List<UserPO> users = userService.getList();
        CsvUtil.exportCsv(response, "Users_" + LocalDateTime.now(), users, UserPO.class);
    }

    @PostMapping("/importCsv")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        List<UserPO> users = CsvUtil.importCsv(csvFile, UserPO.class);
        userService.adds(users);
    }
}
