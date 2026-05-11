package org.bot.nullbot.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.file.storage")
public class FileStorageProperties {
    private String fileDirectory;
    private String configPath;
    private String resourcePath;
    private String tempPath;
    private String imagePath;
    private String videoPath;
    private String audioPath;
}
