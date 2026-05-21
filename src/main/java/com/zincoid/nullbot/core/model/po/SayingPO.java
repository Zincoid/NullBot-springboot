package com.zincoid.nullbot.core.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`saying`")
public class SayingPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private LocalDateTime time;
    private Long userId;
    private String userName;
    private String text;

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = time != null ? time.format(formatter) : "";
        return """
            [%s][No.%d]
            %s
            \t—— %s(%d)""".formatted(formattedTime, id, text, userName, userId);
    }
}
