package com.github.thundax.common.web.assembler;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.web.request.PageRequest;

public final class PageInterfaceAssembler {

    private PageInterfaceAssembler() {}

    public static PageQuery toPageQuery(PageRequest request) {
        return request == null ? toPageQuery(null, null) : toPageQuery(request.getPageNo(), request.getPageSize());
    }

    public static PageQuery toPageQuery(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < PageRules.firstPageIndex()) {
            pageNo = PageRules.firstPageIndex();
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }
        return new PageQuery(pageNo, pageSize);
    }
}
