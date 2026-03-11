package org.bot.nullbot.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.service.PreviewService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/nullbot/preview")
public class PreviewController
{
    private final PreviewService previewService;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> preview(@PathVariable Integer id, HttpServletRequest request){
        return previewService.preview(id, request);
    }
}
