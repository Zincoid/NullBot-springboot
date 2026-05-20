package com.zincoid.nullbot.core.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface OssService {

    ResponseEntity<?> getResourceById(HttpServletRequest request, Integer id);

    ResponseEntity<?> getResourceByPath(HttpServletRequest request, String path);
}
