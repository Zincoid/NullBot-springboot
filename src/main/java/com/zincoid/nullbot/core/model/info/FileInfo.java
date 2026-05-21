package com.zincoid.nullbot.core.model.info;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileInfo {
    private String fileName;
    private Long fileSize;
    LocalDateTime lastModified;
}
