package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface PreviewService {

    ResponseEntity<Resource> preview(Integer id, HttpServletRequest request);
}
