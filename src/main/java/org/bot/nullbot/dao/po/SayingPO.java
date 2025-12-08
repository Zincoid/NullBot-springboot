package org.bot.nullbot.dao.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SayingPO {
    private Integer id;
    private LocalDateTime time;
    private Long userId;
    private String userName;
    private String text;
}
