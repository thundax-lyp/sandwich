package com.github.thundax.modules.sys.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.security.CurrentUserResolver;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.RoleIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.Collections;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserControllerContractTest {

    @Test
    public void shouldWrapUserPageJsonResponseWithApiResponseAdvice() throws Exception {
        UserService userService = mock(UserService.class);
        DepartmentService departmentService = mock(DepartmentService.class);
        PrincipalIdentityService principalIdentityService = mock(PrincipalIdentityService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        User user = user();
        Department department = department();
        Role role = role();
        Role relationRole = new Role();
        relationRole.setId(role.getId());
        RoleService roleService = mock(RoleService.class);
        PrincipalIdentity identity = new PrincipalIdentity();
        identity.setIdentityValue("server.user");

        when(userService.page(any(UserQuery.class), any(PageQuery.class)))
                .thenReturn(PageResult.of(1, 10, 1L, Collections.singletonList(user)));
        when(userService.listUserRoles(any(UserQuery.class))).thenReturn(Collections.singletonList(relationRole));
        when(roleService.get(role.getId())).thenReturn(role);
        when(departmentService.get(any(DepartmentId.class))).thenReturn(department);
        when(principalIdentityService.get(any())).thenReturn(identity);
        when(currentUserService.existsAvatar(any())).thenReturn(false);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserController(
                        userService,
                        departmentService,
                        roleService,
                        principalIdentityService,
                        mock(PrincipalCredentialService.class),
                        mock(PreAuthSessionService.class),
                        mock(CurrentUserResolver.class),
                        currentUserService))
                .setControllerAdvice(new ApiResponseBodyAdvice())
                .build();

        mockMvc.perform(post("/api/sys/user/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNo\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.message").value(ApiResponse.SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value("1001"))
                .andExpect(jsonPath("$.data.records[0].loginName").value("server.user"))
                .andExpect(jsonPath("$.data.records[0].roles[0].name").value("系统管理员"));
    }

    @Test
    public void shouldAllowDottedLoginNameWhenBelongsToSameUser() throws Exception {
        PrincipalIdentityService principalIdentityService = mock(PrincipalIdentityService.class);
        PrincipalIdentity identity = new PrincipalIdentity();
        identity.setIdentityValue("lin.zhiyuan");
        identity.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, 1001L));
        when(principalIdentityService.get(any())).thenReturn(identity);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserController(
                        mock(UserService.class),
                        mock(DepartmentService.class),
                        mock(RoleService.class),
                        principalIdentityService,
                        mock(PrincipalCredentialService.class),
                        mock(PreAuthSessionService.class),
                        mock(CurrentUserResolver.class),
                        mock(CurrentUserService.class)))
                .setControllerAdvice(new ApiResponseBodyAdvice())
                .build();

        mockMvc.perform(post("/api/sys/user/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"1001\",\"loginName\":\"lin.zhiyuan\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ApiResponse.SUCCESS_CODE))
                .andExpect(jsonPath("$.data").value(true));
    }

    private User user() {
        User user = new User();
        user.setId(UserIdCodec.toDomain(1001L));
        user.setDepartmentId(DepartmentIdCodec.toDomain(2001L));
        user.setName("服务端用户A");
        user.setPrivilege(UserPrivilege.ADMIN);
        user.setStatus(UserStatus.ENABLED);
        return user;
    }

    private Department department() {
        Department department = new Department();
        department.setId(DepartmentIdCodec.toDomain(2001L));
        department.setName("平台架构部");
        return department;
    }

    private Role role() {
        Role role = new Role();
        role.setId(RoleIdCodec.toDomain(3001L));
        role.setName("系统管理员");
        role.setStatus(RoleStatus.ENABLED);
        return role;
    }
}
