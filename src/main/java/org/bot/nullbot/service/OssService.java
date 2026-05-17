package org.bot.nullbot.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface OssService {

    ResponseEntity<@NonNull Resource> getResource(Integer id, HttpServletRequest request);
}
