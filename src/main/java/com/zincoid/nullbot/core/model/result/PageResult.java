package com.zincoid.nullbot.core.model.result;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    private List<T> data;

    private Long current;
    private Long pages;
    private Long total;
    private Long size;

    public static <T, R> PageResult<R> of(Page<T> page, Function<T, R> mapper) {
        List<R> records = page.getRecords().stream().map(mapper).toList();
        return new PageResult<>(records, page.getCurrent(), page.getPages(), page.getTotal(), page.getSize());
    }

    public static <T> PageResult<T> of(Page<T> page) {
        return of(page, Function.identity());
    }
}
