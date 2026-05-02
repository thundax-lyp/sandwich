package com.github.thundax.common.web;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.vo.PageResponse;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PageResponseHelper {

    private PageResponseHelper() {}

    public static <T, R> PageResponse<R> fromEntityPage(Page<T> page, Function<T, R> mappingFunction) {
        PageResponse<R> pageResponse = new PageResponse<>();

        pageResponse.setPageNo(page.getPageNo());
        pageResponse.setPageSize(page.getPageSize());
        pageResponse.setTotalPage(page.getTotalPage());
        pageResponse.setCount(page.getCount());

        pageResponse.setRecords(
                page.getList() == null
                        ? new ArrayList<>()
                        : page.getList().stream()
                                .map(item -> mappingFunction.apply(item))
                                .collect(Collectors.toList()));

        return pageResponse;
    }
}
