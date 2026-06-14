package com.zincoid.nullbot.core.properties.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Component
@ConfigurationProperties(prefix = "file.storage")
public class StorageProperties {

    @Getter
    private boolean init;
    @Getter
    private String fileDirectory;
    private String configPath;
    private String resourcePath;
    private String tempPath;
    private String imagePath;
    private String videoPath;
    private String audioPath;

    public String getConfigPath() {
        return fileDirectory + configPath;
    }
    public String getResourcePath() {
        return fileDirectory + resourcePath;
    }
    public String getTempPath() {
        return fileDirectory + tempPath;
    }
    public String getImagePath() {
        return fileDirectory + imagePath;
    }
    public String getVideoPath() {
        return fileDirectory + videoPath;
    }
    public String getAudioPath() {
        return fileDirectory + audioPath;
    }
}
