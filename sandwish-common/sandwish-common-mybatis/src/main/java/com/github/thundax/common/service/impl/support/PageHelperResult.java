package com.github.thundax.common.service.impl.support;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import java.util.ArrayList;
import java.util.List;

public class PageHelperResult<T> {

    private final Page<T> page;

    private PageHelperResult(Page<T> page) {
        this.page = page;
    }

    public static <T> PageHelperResult<T> startPage(int pageNo, int pageSize) {
        return startPage(pageNo, pageSize, true);
    }

    public static <T> PageHelperResult<T> startPage(int pageNo, int pageSize, boolean count) {
        return new PageHelperResult<T>(PageHelper.startPage(pageNo, pageSize, count));
    }

    public T getFirst() {
        return page.size() > 0 ? page.get(0) : null;
    }

    public int getPageNo() {
        return page.getPageNum();
    }

    public int getPageSize() {
        return page.getPageSize();
    }

    public long getTotal() {
        return page.getTotal();
    }

    public List<T> getList() {
        return new ArrayList<T>(page);
    }
}
