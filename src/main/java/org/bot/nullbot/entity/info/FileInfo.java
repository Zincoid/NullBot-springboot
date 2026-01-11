package org.bot.nullbot.entity.info;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class FileInfo
{
    private String fileName;
    private Long fileSize;
    LocalDateTime lastModified;
}
