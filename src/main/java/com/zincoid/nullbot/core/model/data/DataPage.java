package com.zincoid.nullbot.core.model.data;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataPage<T> {

    private List<T> data;

    private Long current;
    private Long pages;
    private Long total;
    private Long size;

    public static <T, R> DataPage<R> of(Page<T> page, Function<T, R> mapper) {
        List<R> records = page.getRecords().stream().map(mapper).toList();
        return new DataPage<>(records, page.getCurrent(), page.getPages(), page.getTotal(), page.getSize());
    }

    public static <T> DataPage<T> of(Page<T> page) {
        return of(page, Function.identity());
    }
}
