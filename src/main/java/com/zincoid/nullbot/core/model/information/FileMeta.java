package com.zincoid.nullbot.core.model.information;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileMeta {

    private String dir;
    private String name;
    private Long size;
    LocalDateTime lastModified;

    public String getPath() {
        return dir + "/" + name;
    }
}
