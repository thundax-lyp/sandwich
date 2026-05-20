package com.github.thundax.modules.sys.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.DictService;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.Arrays;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class RoleControllerContractTest {

    @Test
    public void shouldReturnRoleOptionsFromRoleDictionaries() throws Exception {
        DictService dictService = mock(DictService.class);
        ArgumentCaptor<DictQuery> queryCaptor = ArgumentCaptor.forClass(DictQuery.class);
        when(dictService.list(any(DictQuery.class)))
                .thenReturn(
                        Arrays.asList(dict("启用", "ENABLED"), dict("禁用", "DISABLED")),
                        Arrays.asList(dict("普通角色", "NORMAL"), dict("管理员角色", "ADMIN")));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new RoleController(
                        mock(RoleService.class),
                        mock(MenuService.class),
                        mock(DepartmentService.class),
                        dictService,
                        mock(UserService.class),
                        mock(PrincipalIdentityService.class)))
                .setControllerAdvice(new ApiResponseBodyAdvice())
                .build();

        mockMvc.perform(post("/api/sys/role/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.data.statusOptions[0].label").value("启用"))
                .andExpect(jsonPath("$.data.statusOptions[0].value").value("ENABLED"))
                .andExpect(jsonPath("$.data.statusOptions[1].label").value("禁用"))
                .andExpect(jsonPath("$.data.statusOptions[1].value").value("DISABLED"))
                .andExpect(jsonPath("$.data.privilegeOptions[0].label").value("普通角色"))
                .andExpect(jsonPath("$.data.privilegeOptions[0].value").value("NORMAL"))
                .andExpect(jsonPath("$.data.privilegeOptions[1].label").value("管理员角色"))
                .andExpect(jsonPath("$.data.privilegeOptions[1].value").value("ADMIN"));

        verify(dictService, org.mockito.Mockito.times(2)).list(queryCaptor.capture());
        org.junit.Assert.assertEquals(
                "role_status", queryCaptor.getAllValues().get(0).getType());
        org.junit.Assert.assertEquals(
                "role_privilege", queryCaptor.getAllValues().get(1).getType());
    }

    private Dict dict(String label, String value) {
        Dict dict = new Dict();
        dict.setType("role_status");
        dict.setLabel(label);
        dict.setValue(value);
        return dict;
    }
}
