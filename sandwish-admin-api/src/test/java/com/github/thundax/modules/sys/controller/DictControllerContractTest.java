package com.github.thundax.modules.sys.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.modules.sys.controller.request.DictIdRequest;
import com.github.thundax.modules.sys.controller.request.DictPageRequest;
import com.github.thundax.modules.sys.controller.response.DictResponse;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.entity.valueobject.DictId;
import com.github.thundax.modules.sys.entity.valueobject.DictIdCodec;
import com.github.thundax.modules.sys.service.DictService;
import com.github.thundax.modules.sys.service.command.DeleteDictCommand;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class DictControllerContractTest {

    @Test
    public void shouldNormalizePageRequestBeforeCallingService() throws Exception {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);
        when(dictService.page(any(DictQuery.class), any(PageQuery.class))).thenAnswer(invocation -> {
            PageQuery page = invocation.getArgument(1);
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, Collections.emptyList());
        });

        DictPageRequest request = new DictPageRequest();
        request.setPageNo(0);
        request.setPageSize(0);
        request.setLabel("Enabled");
        request.setType("status");
        request.setRemarks("visible");

        PageResponse<DictResponse> response = controller.page(request);

        ArgumentCaptor<DictQuery> queryCaptor = ArgumentCaptor.forClass(DictQuery.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);
        verify(dictService).page(queryCaptor.capture(), pageCaptor.capture());
        assertEquals(PageRules.firstPageIndex(), pageCaptor.getValue().getPageNo());
        assertEquals(PageRules.defaultPageSize(), pageCaptor.getValue().getPageSize());
        assertEquals(PageRules.firstPageIndex(), response.getPageNo());
        assertEquals(PageRules.defaultPageSize(), response.getPageSize());
        assertEquals("Enabled", queryCaptor.getValue().getLabel());
        assertEquals("status", queryCaptor.getValue().getType());
        assertEquals("visible", queryCaptor.getValue().getRemarks());
    }

    @Test
    public void shouldWrapPageJsonResponseWithApiResponseAdvice() throws Exception {
        DictService dictService = mock(DictService.class);
        PageResult<Dict> page = PageResult.of(1, 10, 1L, Collections.singletonList(dict(1L)));
        when(dictService.page(any(DictQuery.class), any(PageQuery.class))).thenReturn(page);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DictController(dictService))
                .setControllerAdvice(new ApiResponseBodyAdvice())
                .build();

        mockMvc.perform(post("/api/sys/dict/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.records[0].id").value(1));
    }

    @Test
    public void shouldBatchDeleteExistingDicts() throws Exception {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);
        when(dictService.get(any(DictId.class))).thenAnswer(invocation -> {
            DictId query = invocation.getArgument(0);
            return dict(DictIdCodec.toValue(query));
        });

        Boolean deleted = controller.delete(Arrays.asList(idRequest(1L), idRequest(2L)));

        ArgumentCaptor<DeleteDictCommand> commandCaptor = ArgumentCaptor.forClass(DeleteDictCommand.class);
        verify(dictService, times(2)).remove(commandCaptor.capture());
        assertEquals(Boolean.TRUE, deleted);
        assertEquals(
                Arrays.asList(1L, 2L),
                commandCaptor.getAllValues().stream()
                        .map(DeleteDictCommand::getId)
                        .map(DictIdCodec::toValue)
                        .collect(Collectors.toList()));
    }

    @Test(expected = InvalidParameterException.class)
    public void shouldRejectEmptyDeleteList() throws Exception {
        DictController controller = new DictController(mock(DictService.class));

        controller.delete(Collections.emptyList());
    }

    private DictIdRequest idRequest(Long id) {
        DictIdRequest request = new DictIdRequest();
        request.setId(id);
        return request;
    }

    private Dict dict(Long id) {
        Dict dict = new Dict();
        dict.setId(DictIdCodec.toDomain(id));
        dict.setType("status");
        dict.setLabel(String.valueOf(id));
        dict.setValue(String.valueOf(id));
        return dict;
    }
}
