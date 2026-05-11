package org.bot.nullbot.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataPage<T> {
    private List<T> data;
    private Long current;
    private Long pages;
    private Long total;
    private Long size;
}
