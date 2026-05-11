package org.bot.nullbot.entity.po;

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
}
