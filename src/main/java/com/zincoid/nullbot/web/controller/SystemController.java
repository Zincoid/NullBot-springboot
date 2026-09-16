package com.zincoid.nullbot.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.context.WebCtx;
import com.zincoid.nullbot.core.model.data.vo.ModelVO;
import com.zincoid.nullbot.core.service.system.SystemService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RequestMapping("/nullbot/system")
@RestController
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    // ── 系统调用 ──────────────

    @PostMapping("/invoke")
    public WebResult<String> invoke(@RequestParam(defaultValue = "") String command) throws Exception {
        WebCtx.requireAdmin();
        String result = systemService.invoke(command);
        return WebResult.success("调用成功", result);
    }

    @PostMapping("/exception")
    public WebResult<Void> exception() throws Exception {
        WebCtx.requireAdmin();
        throw new Exception("测试异常");
    }

    // ── 全局设置 ──────────────

    @GetMapping("/func")
    public WebResult<Map<String, Boolean>> funcList() {
        Map<String, Boolean> flags = systemService.getFuncFlags();
        return WebResult.success("查询成功", flags);
    }

    @PutMapping("/func")
    public WebResult<Void> funcSet(
            @RequestParam String function,
            @RequestParam(required = false) Boolean enabled
    ) {
        WebCtx.requireAdmin();
        systemService.setFuncFlag(function, enabled);
        return WebResult.success("设置成功");
    }

    // ── 模型设置 ──────────────

    @GetMapping("/model")
    public WebResult<ModelVO> model() {
        ModelVO models = systemService.getModels();
        return WebResult.success("查询成功", models);
    }

    @PutMapping("/model")
    public WebResult<Void> modelSet(@RequestParam String provider) {
        WebCtx.requireAdmin();
        systemService.setModel(provider);
        return WebResult.success("切换成功");
    }
}
