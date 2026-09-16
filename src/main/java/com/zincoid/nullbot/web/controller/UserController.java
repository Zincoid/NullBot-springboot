package com.zincoid.nullbot.web.controller;

import com.zincoid.nullbot.core.model.data.dto.UserDTO;
import com.zincoid.nullbot.core.context.WebCtx;
import com.zincoid.nullbot.core.model.data.query.UserQuery;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.service.base.UserService;
import com.zincoid.nullbot.core.utils.CsvUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/nullbot/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public WebResult<List<UserPO>> list() {
        return WebResult.success("查询成功", userService.list());
    }

    @GetMapping("/page")
    public WebResult<PageResult<UserPO>> page(UserQuery query) {
        PageResult<UserPO> userPage = userService.page(query);
        return WebResult.success("查询成功", userPage);
    }

    @DeleteMapping("/{id}")
    public WebResult<Void> delete(@PathVariable Long id) {
        WebCtx.requireAdmin();
        userService.delete(id);
        return WebResult.success("删除成功");
    }

    @PutMapping("/{id}")
    public WebResult<Void> update(@PathVariable Long id,
                                  @RequestBody @Valid UserDTO user) {
        WebCtx.requireAdmin();
        user.setId(id);
        userService.update(user);
        return WebResult.success("更新成功");
    }

    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        WebCtx.requireAdmin();
        List<UserPO> users = userService.list();
        CsvUtil.exportCsv(response, "Users_" + LocalDateTime.now(), users, UserPO.class);
    }

    @PostMapping("/import")
    public void importCsv(@RequestParam("file") MultipartFile csvFile) throws IOException {
        WebCtx.requireAdmin();
        List<UserPO> users = CsvUtil.importCsv(csvFile, UserPO.class);
        userService.saveBatch(users);
    }
}
