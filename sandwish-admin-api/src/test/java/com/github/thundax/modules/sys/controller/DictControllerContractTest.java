package com.github.thundax.modules.sys.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.modules.sys.controller.request.DictIdRequest;
import com.github.thundax.modules.sys.controller.request.DictPageRequest;
import com.github.thundax.modules.sys.controller.response.DictResponse;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DictControllerContractTest {

    @Test
    public void shouldNormalizePageRequestBeforeCallingService() throws Exception {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);
        when(dictService.page(any(DictQuery.class), any(PageDTO.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        DictPageRequest request = new DictPageRequest();
        request.setPageNo(0);
        request.setPageSize(0);
        request.setLabel("Enabled");
        request.setType("status");
        request.setRemarks("visible");

        PageResponse<DictResponse> response = controller.page(request);

        ArgumentCaptor<DictQuery> queryCaptor = ArgumentCaptor.forClass(DictQuery.class);
        ArgumentCaptor<PageDTO> pageCaptor = ArgumentCaptor.forClass(PageDTO.class);
        verify(dictService).page(queryCaptor.capture(), pageCaptor.capture());
        assertEquals(PageRules.firstPageIndex(), pageCaptor.getValue().getPageNo());
        assertEquals(PageRules.defaultPageSize(), pageCaptor.getValue().getPageSize());
        assertEquals(Integer.valueOf(PageRules.firstPageIndex()), response.getPageNo());
        assertEquals(Integer.valueOf(PageRules.defaultPageSize()), response.getPageSize());
        assertEquals("Enabled", queryCaptor.getValue().getLabel());
        assertEquals("status", queryCaptor.getValue().getType());
        assertEquals("visible", queryCaptor.getValue().getRemarks());
    }

    @Test
    public void shouldBatchDeleteExistingDicts() throws Exception {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);
        when(dictService.getById(EntityIdCodec.toDomain("d1"))).thenReturn(dict("d1"));
        when(dictService.getById(EntityIdCodec.toDomain("d2"))).thenReturn(dict("d2"));

        Boolean deleted = controller.delete(Arrays.asList(idRequest("d1"), idRequest("d2")));

        ArgumentCaptor<List> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(dictService).batchDeleteById(idsCaptor.capture());
        assertEquals(Boolean.TRUE, deleted);
        assertEquals(Arrays.asList(EntityIdCodec.toDomain("d1"), EntityIdCodec.toDomain("d2")), idsCaptor.getValue());
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldRejectEmptyDeleteList() throws Exception {
        DictController controller = new DictController(mock(DictService.class));

        controller.delete(Collections.emptyList());
    }

    private DictIdRequest idRequest(String id) {
        DictIdRequest request = new DictIdRequest();
        request.setId(id);
        return request;
    }

    private Dict dict(String id) {
        Dict dict = new Dict();
        dict.setId(EntityId.of(id));
        dict.setType("status");
        dict.setLabel(id);
        dict.setValue(id);
        return dict;
    }
}
