package com.github.thundax.common.web.response;

import com.github.thundax.common.page.PageDTO;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PageResponseHelper {

    private PageResponseHelper() {}

    public static <T, R> PageResponse<R> fromEntityPage(PageDTO<T> page, Function<T, R> mappingFunction) {
        PageResponse<R> pageResponse = new PageResponse<>();

        pageResponse.setPageNo(page.getPageNo());
        pageResponse.setPageSize(page.getPageSize());
        pageResponse.setTotalPage(page.getTotalPage());
        pageResponse.setCount(page.getCount());

        pageResponse.setRecords(Optional.ofNullable(page.getList())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(mappingFunction)
                .collect(Collectors.toList()));

        return pageResponse;
    }
}
