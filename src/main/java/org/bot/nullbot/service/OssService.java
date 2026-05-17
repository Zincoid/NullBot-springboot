package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface OssService {

    ResponseEntity<?> getResource(Integer id, HttpServletRequest request);
}
