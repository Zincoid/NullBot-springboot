package com.zincoid.nullbot.core.model.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`file`")
public class FilePO {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String fileName;
    private Long fileSize;
    private String directory;
    private Integer isDir;
    private Boolean visible;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime lastModified;

    public FilePO(String fileName, Long fileSize, String directory, Integer isDir,
                  Boolean visible, Long ownerId, String ownerName, LocalDateTime lastModified) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.directory = directory;
        this.isDir = isDir;
        this.visible = visible;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.lastModified = lastModified;
    }

    public String getPath() {
        return directory + "/" + fileName;
    }

    public String getName() {
        if (fileName.contains("."))
            return fileName.substring(0, fileName.lastIndexOf("."));
        return fileName;
    }

    public String getDirName() {
        if (directory.contains("/"))
            return directory.substring(directory.lastIndexOf("/") + 1);
        return directory;
    }
}
