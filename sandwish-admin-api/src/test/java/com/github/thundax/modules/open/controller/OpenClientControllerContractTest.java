package com.github.thundax.modules.open.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.modules.open.controller.request.OpenClientPageRequest;
import com.github.thundax.modules.open.controller.request.OpenClientSaveRequest;
import com.github.thundax.modules.open.controller.request.OpenClientSecretResetRequest;
import com.github.thundax.modules.open.controller.request.OpenClientStatusRequest;
import com.github.thundax.modules.open.controller.response.OpenClientResponse;
import com.github.thundax.modules.open.controller.response.OpenClientSecretResponse;
import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.service.OpenClientService;
import com.github.thundax.modules.open.service.command.ChangeOpenClientStatusCommand;
import com.github.thundax.modules.open.service.command.CreateOpenClientCommand;
import com.github.thundax.modules.open.service.command.ResetOpenClientSecretCommand;
import com.github.thundax.modules.open.service.command.UpdateOpenClientCommand;
import com.github.thundax.modules.open.service.dto.OpenClientDTO;
import com.github.thundax.modules.open.service.query.OpenClientQuery;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class OpenClientControllerContractTest {

    @Test
    public void shouldNormalizePageRequestBeforeCallingService() {
        OpenClientService openClientService = mock(OpenClientService.class);
        OpenClientController controller = new OpenClientController(openClientService);
        when(openClientService.page(any(OpenClientQuery.class), any(PageQuery.class)))
                .thenAnswer(invocation -> {
                    PageQuery page = invocation.getArgument(1);
                    return PageResult.of(page.getPageNo(), page.getPageSize(), 0, Collections.emptyList());
                });

        OpenClientPageRequest request = new OpenClientPageRequest();
        request.setPageNo(0);
        request.setPageSize(0);
        request.setName("partner");
        request.setStatus("ENABLED");

        PageResponse<OpenClientResponse> response = controller.page(request);

        ArgumentCaptor<OpenClientQuery> queryCaptor = ArgumentCaptor.forClass(OpenClientQuery.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);
        verify(openClientService).page(queryCaptor.capture(), pageCaptor.capture());
        assertEquals(PageRules.firstPageIndex(), pageCaptor.getValue().getPageNo());
        assertEquals(PageRules.defaultPageSize(), pageCaptor.getValue().getPageSize());
        assertEquals(PageRules.firstPageIndex(), response.getPageNo());
        assertEquals(PageRules.defaultPageSize(), response.getPageSize());
        assertEquals("partner", queryCaptor.getValue().getName());
        assertEquals(OpenClientStatus.ENABLED, queryCaptor.getValue().getStatus());
    }

    @Test
    public void shouldWrapPageJsonWithoutApiSecret() throws Exception {
        OpenClientService openClientService = mock(OpenClientService.class);
        when(openClientService.page(any(OpenClientQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 10, 1L, Collections.singletonList(dtoWithSecret())));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OpenClientController(openClientService))
                .setControllerAdvice(new ApiResponseBodyAdvice())
                .build();

        mockMvc.perform(post("/api/open/client/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value("9001"))
                .andExpect(jsonPath("$.data.records[0].apiKey").value("swak_demo"))
                .andExpect(jsonPath("$.data.records[0].apiSecret").doesNotExist());
    }

    @Test
    public void shouldConvertCreateRequestToCommandAndReturnSecretOnce() {
        OpenClientService openClientService = mock(OpenClientService.class);
        when(openClientService.create(any(CreateOpenClientCommand.class))).thenReturn(dtoWithSecret());
        OpenClientController controller = new OpenClientController(openClientService);
        OpenClientSaveRequest request = saveRequest();

        OpenClientSecretResponse response = controller.create(request);

        ArgumentCaptor<CreateOpenClientCommand> captor = ArgumentCaptor.forClass(CreateOpenClientCommand.class);
        verify(openClientService).create(captor.capture());
        assertEquals("Sandbox", captor.getValue().getName());
        assertEquals("[\"127.0.0.1\"]", captor.getValue().getIpWhitelist());
        assertEquals(
                Arrays.asList("submission:submission:create"), captor.getValue().getPermissions());
        assertEquals("9001", response.getId());
        assertEquals("swak_demo", response.getApiKey());
        assertEquals("swas_demo", response.getApiSecret());
    }

    @Test
    public void shouldConvertUpdateRequestToCommandWithoutReturningSecret() {
        OpenClientService openClientService = mock(OpenClientService.class);
        when(openClientService.change(any(UpdateOpenClientCommand.class))).thenReturn(dtoWithoutSecret());
        OpenClientController controller = new OpenClientController(openClientService);
        OpenClientSaveRequest request = saveRequest();
        request.setId("9001");

        OpenClientResponse response = controller.update(request);

        ArgumentCaptor<UpdateOpenClientCommand> captor = ArgumentCaptor.forClass(UpdateOpenClientCommand.class);
        verify(openClientService).change(captor.capture());
        assertEquals(Long.valueOf(9001L), captor.getValue().getId().value());
        assertEquals("Sandbox", captor.getValue().getName());
        assertEquals("swak_demo", response.getApiKey());
        assertNoApiSecretField(response);
    }

    @Test
    public void shouldConvertStatusRequestToCommand() {
        OpenClientService openClientService = mock(OpenClientService.class);
        OpenClientController controller = new OpenClientController(openClientService);
        OpenClientStatusRequest request = new OpenClientStatusRequest();
        request.setId("9001");
        request.setStatus("DISABLED");

        assertTrue(controller.changeStatus(request));

        ArgumentCaptor<ChangeOpenClientStatusCommand> captor =
                ArgumentCaptor.forClass(ChangeOpenClientStatusCommand.class);
        verify(openClientService).changeStatus(captor.capture());
        assertEquals(Long.valueOf(9001L), captor.getValue().getId().value());
        assertEquals(OpenClientStatus.DISABLED, captor.getValue().getStatus());
    }

    @Test
    public void shouldResetSecretAndReturnPlaintextOnlyInSecretResponse() {
        OpenClientService openClientService = mock(OpenClientService.class);
        when(openClientService.resetSecret(any(ResetOpenClientSecretCommand.class)))
                .thenReturn(dtoWithSecret());
        OpenClientController controller = new OpenClientController(openClientService);
        OpenClientSecretResetRequest request = new OpenClientSecretResetRequest();
        request.setId("9001");

        OpenClientSecretResponse response = controller.resetSecret(request);

        ArgumentCaptor<ResetOpenClientSecretCommand> captor =
                ArgumentCaptor.forClass(ResetOpenClientSecretCommand.class);
        verify(openClientService).resetSecret(captor.capture());
        assertEquals(Long.valueOf(9001L), captor.getValue().getId().value());
        assertEquals("swak_demo", response.getApiKey());
        assertEquals("swas_demo", response.getApiSecret());
    }

    private OpenClientSaveRequest saveRequest() {
        OpenClientSaveRequest request = new OpenClientSaveRequest();
        request.setName("Sandbox");
        request.setIpWhitelist("[\"127.0.0.1\"]");
        request.setRemarks("remark");
        request.setPermissions(Arrays.asList("submission:submission:create"));
        return request;
    }

    private OpenClientDTO dtoWithSecret() {
        OpenClientDTO dto = dtoWithoutSecret();
        dto.setApiSecret("swas_demo");
        return dto;
    }

    private OpenClientDTO dtoWithoutSecret() {
        OpenClientDTO dto = new OpenClientDTO();
        dto.setId(OpenClientId.of(9001L));
        dto.setName("Sandbox");
        dto.setStatus(OpenClientStatus.ENABLED);
        dto.setApiKey("swak_demo");
        dto.setIpWhitelist("[\"127.0.0.1\"]");
        dto.setRemarks("remark");
        dto.setPermissions(Arrays.asList("submission:submission:create"));
        return dto;
    }

    private void assertNoApiSecretField(OpenClientResponse response) {
        for (Field field : response.getClass().getDeclaredFields()) {
            assertFalse("apiSecret must not be exposed by OpenClientResponse", "apiSecret".equals(field.getName()));
        }
    }
}
