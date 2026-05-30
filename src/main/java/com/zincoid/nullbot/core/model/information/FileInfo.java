package com.zincoid.nullbot.core.model.information;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileInfo {

    private String directory;
    private String name;
    private Long size;
    LocalDateTime lastModified;

    public String getPath() {
        return directory + "/" + name;
    }
}
