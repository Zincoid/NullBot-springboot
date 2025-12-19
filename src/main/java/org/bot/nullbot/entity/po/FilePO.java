package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("file")
public class FilePO {
    private Integer id;
    private String fileName;
    private Long fileSize;
    private String directory;
    private String location;
    private Integer isDir;
}
