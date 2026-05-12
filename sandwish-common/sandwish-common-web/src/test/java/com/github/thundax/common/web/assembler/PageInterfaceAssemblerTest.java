package com.github.thundax.common.web.assembler;

import static org.junit.Assert.assertEquals;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.web.request.PageRequest;
import org.junit.Test;

public class PageInterfaceAssemblerTest {

    @Test
    public void shouldUseDefaultPageWhenRequestIsNull() {
        PageQuery page = PageInterfaceAssembler.toPageQuery(null);

        assertEquals(PageRules.firstPageIndex(), page.getPageNo());
        assertEquals(PageRules.defaultPageSize(), page.getPageSize());
    }

    @Test
    public void shouldNormalizeInvalidPageValues() {
        TestPageRequest request = new TestPageRequest();
        request.setPageNo(0);
        request.setPageSize(0);

        PageQuery page = PageInterfaceAssembler.toPageQuery(request);

        assertEquals(PageRules.firstPageIndex(), page.getPageNo());
        assertEquals(PageRules.defaultPageSize(), page.getPageSize());
    }

    @Test
    public void shouldKeepValidPageValues() {
        PageQuery page = PageInterfaceAssembler.toPageQuery(3, 20);

        assertEquals(3, page.getPageNo());
        assertEquals(20, page.getPageSize());
    }

    private static class TestPageRequest extends PageRequest {}
}
