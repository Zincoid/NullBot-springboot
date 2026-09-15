package com.zincoid.nullbot.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.WebResult;
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

    @GetMapping("/invoke")
    public WebResult<String> invoke(@RequestParam(defaultValue = "") String command) throws Exception {
        String result = systemService.invoke(command);
        return WebResult.success("调用成功", result);
    }

    @GetMapping("/exception")
    public WebResult<Void> exception() throws Exception {
        throw new Exception("测试异常");
    }

    // ── 全局设置 ──────────────

    @GetMapping("/func")
    public WebResult<Map<String, Boolean>> funcList() {
        return WebResult.success("查询成功", systemService.getFuncFlags());
    }

    @PutMapping("/func/set")
    public WebResult<Void> funcSet(@RequestParam String function, @RequestParam(required = false) Boolean enabled) {
        systemService.setFuncFlag(function, enabled);
        return WebResult.success("设置成功");
    }

    // ── 模型设置 ──────────────

    @GetMapping("/model")
    public WebResult<ModelVO> model() {
        return WebResult.success("查询成功", systemService.getModels());
    }

    @PutMapping("/model/set")
    public WebResult<Void> modelSet(@RequestParam String provider) {
        systemService.setModel(provider);
        return WebResult.success("切换成功");
    }
}
