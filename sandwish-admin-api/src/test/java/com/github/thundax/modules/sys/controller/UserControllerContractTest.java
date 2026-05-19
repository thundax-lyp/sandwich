package com.github.thundax.modules.sys.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.common.crypto.Sm2Crypto;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.common.web.exception.SandwishException;
import com.github.thundax.common.web.exception.WebErrorCode;
import com.github.thundax.common.web.response.ApiResponse;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.security.CurrentUserResolver;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.query.PreAuthSessionValueQuery;
import com.github.thundax.modules.sys.controller.request.UserDepartmentRequest;
import com.github.thundax.modules.sys.controller.request.UserSaveRequest;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.enums.UserPrivilege;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.RoleIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.ChangeUserInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateUserCommand;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.Collections;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    @Test
    public void shouldRejectCreatingUserWhenRankExceedsCurrentUserMaxRank() {
        Sm2Crypto.StringKeyPair keyPair = Sm2Crypto.generateKeyPair();
        UserService userService = mock(UserService.class);
        DepartmentService departmentService = mock(DepartmentService.class);
        PreAuthSessionService preAuthSessionService = mock(PreAuthSessionService.class);
        CurrentUserResolver currentUserResolver = mock(CurrentUserResolver.class);
        PreAuthSessionId sessionId = PreAuthSessionId.of("session-1");
        User currentUser = user();
        UserDepartmentRequest departmentRequest = new UserDepartmentRequest();
        UserSaveRequest request = new UserSaveRequest();

        currentUser.setRank(AccessRank.of(3));
        departmentRequest.setId("2001");
        request.setLoginName("new.user");
        request.setLoginPass(Sm2Crypto.encrypt("Plain@123", keyPair.getPublicKey()));
        request.setToken("login-token-1");
        request.setName("新用户");
        request.setRanks(3);
        request.setDepartment(departmentRequest);

        when(preAuthSessionService.getIdByToken(any(PreAuthSessionToken.class))).thenReturn(sessionId);
        when(preAuthSessionService.getValue(any(PreAuthSessionValueQuery.class)))
                .thenReturn(keyPair.getPrivateKey());
        when(departmentService.get(any(DepartmentId.class))).thenReturn(department());
        when(currentUserResolver.currentUser()).thenReturn(currentUser);

        try {
            new UserController(
                            userService,
                            departmentService,
                            mock(RoleService.class),
                            mock(PrincipalIdentityService.class),
                            mock(PrincipalCredentialService.class),
                            preAuthSessionService,
                            currentUserResolver,
                            mock(CurrentUserService.class))
                    .add(request);
            org.junit.Assert.fail("creating user with too high rank should be rejected");
        } catch (SandwishException e) {
            org.junit.Assert.assertEquals(WebErrorCode.FORBIDDEN, e.getErrorCode());
        }
        verify(userService, never()).create(any(CreateUserCommand.class));
    }

    @Test
    public void shouldRejectUpdatingUserWhenEmailExistsOnAnotherUser() {
        UserService userService = mock(UserService.class);
        DepartmentService departmentService = mock(DepartmentService.class);
        CurrentUserResolver currentUserResolver = mock(CurrentUserResolver.class);
        User currentUser = user();
        User targetUser = user();
        UserSaveRequest request = editableUserRequest();

        currentUser.setRank(AccessRank.of(3));
        targetUser.setRank(AccessRank.of(2));
        request.setRanks(2);
        request.setEmail("exists@example.com");

        when(userService.get(UserIdCodec.toDomain("1001"))).thenReturn(targetUser);
        when(userService.existsEmail(any(UserQuery.class))).thenReturn(true);
        when(departmentService.get(any(DepartmentId.class))).thenReturn(department());
        when(currentUserResolver.currentUser()).thenReturn(currentUser);

        try {
            new UserController(
                            userService,
                            departmentService,
                            mock(RoleService.class),
                            mock(PrincipalIdentityService.class),
                            mock(PrincipalCredentialService.class),
                            mock(PreAuthSessionService.class),
                            currentUserResolver,
                            mock(CurrentUserService.class))
                    .update(request);
            org.junit.Assert.fail("duplicate email should be rejected");
        } catch (SandwishException e) {
            org.junit.Assert.assertEquals(WebErrorCode.BAD_REQUEST, e.getErrorCode());
        }
        verify(userService, never()).changeInfo(any(ChangeUserInfoCommand.class));
    }

    @Test
    public void shouldRejectUpdatingUserWhenNewRankIsNotLowerThanCurrentUser() {
        UserService userService = mock(UserService.class);
        DepartmentService departmentService = mock(DepartmentService.class);
        CurrentUserResolver currentUserResolver = mock(CurrentUserResolver.class);
        User currentUser = user();
        User targetUser = user();
        UserSaveRequest request = editableUserRequest();

        currentUser.setRank(AccessRank.of(3));
        targetUser.setRank(AccessRank.of(2));
        request.setRanks(3);

        when(userService.get(UserIdCodec.toDomain("1001"))).thenReturn(targetUser);
        when(departmentService.get(any(DepartmentId.class))).thenReturn(department());
        when(currentUserResolver.currentUser()).thenReturn(currentUser);

        try {
            new UserController(
                            userService,
                            departmentService,
                            mock(RoleService.class),
                            mock(PrincipalIdentityService.class),
                            mock(PrincipalCredentialService.class),
                            mock(PreAuthSessionService.class),
                            currentUserResolver,
                            mock(CurrentUserService.class))
                    .update(request);
            org.junit.Assert.fail("updating user to same rank as current user should be rejected");
        } catch (SandwishException e) {
            org.junit.Assert.assertEquals(WebErrorCode.FORBIDDEN, e.getErrorCode());
        }
        verify(userService, never()).changeInfo(any(ChangeUserInfoCommand.class));
    }

    @Test
    public void shouldBuildAvatarUrlWithContextPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/admin-api");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            org.junit.Assert.assertEquals(
                    "/admin-api/api/sys/user/avatar?id=1001", UserController.getAvatarUrl("1001"));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
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

    private UserSaveRequest editableUserRequest() {
        UserDepartmentRequest departmentRequest = new UserDepartmentRequest();
        departmentRequest.setId("2001");

        UserSaveRequest request = new UserSaveRequest();
        request.setId("1001");
        request.setLoginName("server.user");
        request.setName("服务端用户A");
        request.setEmail("server.user@example.com");
        request.setMobile("13800000000");
        request.setAdmin(false);
        request.setEnable(true);
        request.setDepartment(departmentRequest);
        return request;
    }
}
