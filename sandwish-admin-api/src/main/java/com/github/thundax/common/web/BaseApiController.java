package com.github.thundax.common.web;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.vo.query.MoveTreeNodeQueryParam;
import com.github.thundax.common.vo.query.PageQueryParam;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseApiController extends BaseController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected TreeService.MoveTreeNodeType readMoveTreeNodeType(MoveTreeNodeQueryParam queryParam) {
        switch (queryParam.getType()) {
            case MoveTreeNodeQueryParam.TYPE_BEFORE:
                return TreeService.MoveTreeNodeType.BEFORE;

            case MoveTreeNodeQueryParam.TYPE_INSIDE:
                return TreeService.MoveTreeNodeType.INSIDE;

            case MoveTreeNodeQueryParam.TYPE_INSIDE_LAST:
                return TreeService.MoveTreeNodeType.INSIDE_LAST;

            default:
                return TreeService.MoveTreeNodeType.AFTER;
        }
    }

    public static <T> Page<T> readPage(PageQueryParam queryParam) {
        Integer pageNo = queryParam.getPageNo();
        Integer pageSize = queryParam.getPageSize();

        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<T> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

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
