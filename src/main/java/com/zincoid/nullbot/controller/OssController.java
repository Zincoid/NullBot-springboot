package com.zincoid.nullbot.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.service.OssService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/nullbot/oss")
@RestController
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getResource(
            HttpServletRequest request,
            @PathVariable Integer id
    ) {
        log.info("◎ [OssController] id: {}", id);
        return ossService.getResourceById(request, id);
    }

    @GetMapping("/to/{*path}")
    public ResponseEntity<?> getResourceByPath(
            HttpServletRequest request,
            @PathVariable String path
    ) {
        log.info("◎ [OssController] path: {}", path);
        return ossService.getResourceByPath(request, path);
    }
}
