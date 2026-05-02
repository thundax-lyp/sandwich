package com.github.thundax.common.web;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.vo.PageVo;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseApiController extends BaseController {

    public static <T, R> PageVo<R> entityPageToVo(Page<T> page, Function<T, R> mappingFunction) {
        PageVo<R> pageVo = new PageVo<>();

        pageVo.setPageNo(page.getPageNo());
        pageVo.setPageSize(page.getPageSize());
        pageVo.setTotalPage(page.getTotalPage());
        pageVo.setCount(page.getCount());

        pageVo.setRecords(
                page.getList() == null
                        ? new ArrayList<>()
                        : page.getList().stream()
                                .map(item -> mappingFunction.apply(item))
                                .collect(Collectors.toList()));

        return pageVo;
    }
}
