package com.zincoid.nullbot.core.properties.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class StorageProperties {

    private boolean init;
    private String fileDirectory;
    private String configPath;
    private String resourcePath;
    private String tempPath;
    private String imagePath;
    private String videoPath;
    private String audioPath;

    public String resolve(String relativePath) {
        if (relativePath == null || relativePath.isEmpty() || relativePath.equals("/")) return fileDirectory;
        if (!relativePath.startsWith("/")) relativePath = "/" + relativePath;
        return fileDirectory + relativePath;
    }
}
