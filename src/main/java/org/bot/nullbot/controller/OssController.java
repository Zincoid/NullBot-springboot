package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.service.OssService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/nullbot/oss")
@RestController
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> preview(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {
        return ossService.preview(id, request);
    }
}
