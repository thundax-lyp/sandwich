package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.crypto.Sm2Crypto;
import com.github.thundax.common.exception.AdminResponseExceptions;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.security.context.SandwishContextHolder;
import com.github.thundax.common.security.token.AccessTokenNames;
import com.github.thundax.common.web.annotation.WrappedApiResponse;
import com.github.thundax.common.web.assembler.PageInterfaceAssembler;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.common.web.response.PageResponse;
import com.github.thundax.common.web.response.PageResponseHelper;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionId;
import com.github.thundax.modules.auth.entity.valueobject.PreAuthSessionToken;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalIdentityId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.security.CurrentUserResolver;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.command.PrincipalCredentialCommand;
import com.github.thundax.modules.auth.service.query.PreAuthSessionValueQuery;
import com.github.thundax.modules.auth.service.query.PrincipalCredentialQuery;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.UserInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.UserAvatarRequest;
import com.github.thundax.modules.sys.controller.request.UserCheckRequest;
import com.github.thundax.modules.sys.controller.request.UserDepartmentRequest;
import com.github.thundax.modules.sys.controller.request.UserIdRequest;
import com.github.thundax.modules.sys.controller.request.UserQueryRequest;
import com.github.thundax.modules.sys.controller.request.UserRoleRequest;
import com.github.thundax.modules.sys.controller.request.UserSaveRequest;
import com.github.thundax.modules.sys.controller.request.UserSortRequest;
import com.github.thundax.modules.sys.controller.request.UserStatusRequest;
import com.github.thundax.modules.sys.controller.response.UserDepartmentResponse;
import com.github.thundax.modules.sys.controller.response.UserResponse;
import com.github.thundax.modules.sys.controller.response.UserRoleResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.RoleIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.CurrentUserService;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.ChangeCurrentUserAvatarCommand;
import com.github.thundax.modules.sys.service.command.ChangeUserStatusCommand;
import com.github.thundax.modules.sys.service.command.RemoveCurrentUserAvatarCommand;
import com.github.thundax.modules.sys.service.command.UserSortCommand;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import com.github.thundax.modules.sys.service.query.UserQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "系统/用户")
@SysLogger(module = {"系统", "用户"})
@RequestMapping(value = "/api/sys/user")
@RestController
public class UserController {

    private static final String AVATAR_URL_FORMAT = "/api/sys/user/avatar?id=%s&token=%s";
    private static final int DEFAULT_PASSWORD_FAILED_LIMIT = 0;
    private static final String PRIVATE_KEY_ITEM = "privateKey";

    private final UserService userService;
    private final DepartmentService departmentService;
    private final RoleService roleService;
    private final PrincipalIdentityService principalIdentityService;
    private final PrincipalCredentialService principalCredentialService;
    private final PreAuthSessionService preAuthSessionService;
    private final CurrentUserResolver currentUserResolver;
    private final CurrentUserService currentUserService;

    @Autowired
    public UserController(
            UserService userService,
            DepartmentService departmentService,
            RoleService roleService,
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService,
            PreAuthSessionService preAuthSessionService,
            CurrentUserResolver currentUserResolver,
            CurrentUserService currentUserService) {

        this.userService = userService;
        this.departmentService = departmentService;
        this.roleService = roleService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.preAuthSessionService = preAuthSessionService;
        this.currentUserResolver = currentUserResolver;
        this.currentUserService = currentUserService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @SysLogger("读取")
    @PostMapping(value = "get")
    @WrappedApiResponse
    public UserResponse get(@Valid @RequestBody UserIdRequest request) {
        User bean = userService.get(UserIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        return toResponse(bean);
    }

    @ApiOperation(value = "获取列表", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @SysLogger("列表")
    @PostMapping(value = "list")
    @WrappedApiResponse
    public List<UserResponse> list(@Valid @RequestBody UserQueryRequest request) {
        UserQuery query = readQuery(request);

        return userService.list(query).stream().map(user -> toResponse(user)).collect(Collectors.toList());
    }

    @ApiOperation(value = "获取分页列表", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @SysLogger("分页")
    @PostMapping(value = "page")
    @WrappedApiResponse
    public PageResponse<UserResponse> page(@Valid @RequestBody UserQueryRequest request) {
        UserQuery query = readQuery(request);

        return PageResponseHelper.fromPageResult(
                userService.page(query, PageInterfaceAssembler.toPageQuery(request)), this::toResponse);
    }

    @ApiOperation(value = "添加", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("添加")
    @PostMapping(value = "create")
    @WrappedApiResponse
    public UserResponse add(@Valid @RequestBody UserSaveRequest request) {
        // 解密密码（数据需要加密传输）
        String password = Sm2Crypto.decrypt(request.getLoginPass(), getPrivateKey(request.getToken()));
        request.setLoginPass(password);
        validateDepartment(request.getDepartment());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw AdminResponseExceptions.invalidParameter("loginName");
        }

        if (StringUtils.isBlank(request.getLoginPass())) {
            throw AdminResponseExceptions.invalidParameter("password");
        }

        User entity = UserInterfaceAssembler.toEntity(new User(), request);
        String encryptedPassword = PasswordHelper.encrypt(request.getLoginPass());

        if (entity.getId() != null) {
            User bean = userService.get(entity.getId());
            if (bean != null) {
                throw AdminResponseExceptions.objectExists();
            }
        }

        entity.setId(userService.create(UserInterfaceAssembler.toCreateCommand(request, encryptedPassword)));

        return toResponse(entity);
    }

    @ApiOperation(value = "更新", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("更新")
    @PostMapping(value = "update")
    @WrappedApiResponse
    public UserResponse update(@Valid @RequestBody UserSaveRequest request) {
        // 解密密码（数据需要加密传输）
        if (StringUtils.isNotBlank(request.getLoginPass())) {
            String password = Sm2Crypto.decrypt(request.getLoginPass(), getPrivateKey(request.getToken()));
            // 先解密，否则密码规则无法校验
            request.setLoginPass(password);
        }
        validateDepartment(request.getDepartment());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw AdminResponseExceptions.invalidParameter("loginName");
        }

        User bean = userService.get(UserIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        User currentUser = currentUserResolver.currentUser();
        // 非超管用户无权限开启/关闭管理员
        if (!currentUser.isSuper() && Boolean.TRUE.equals(request.getAdmin()) != bean.isAdmin()) {
            throw AdminResponseExceptions.permissionDenied();
        }
        // 无权限修改超管/等级高于自身的用户信息
        if (!currentUser.isSuper()) {
            if (bean.isSuper()
                    || bean.getRank().value() >= currentUser.getRank().value()) {
                throw AdminResponseExceptions.permissionDenied();
            }
        }

        User entity = UserInterfaceAssembler.toEntity(bean, request);

        userService.changeInfo(UserInterfaceAssembler.toChangeInfoCommand(request));

        if (StringUtils.isNotBlank(request.getLoginPass())) {
            upsertPassword(entity, PasswordHelper.encrypt(request.getLoginPass()));
        }

        return toResponse(entity);
    }

    @ApiOperation(value = "上传头像", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
        @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("上传头像")
    @PostMapping(value = "avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @WrappedApiResponse
    public Boolean uploadAvatar(@RequestParam(value = "id") String id, MultipartFile avatar) {
        try {
            currentUserService.changeAvatar(new ChangeCurrentUserAvatarCommand(
                    UserIdCodec.toDomain(Long.valueOf(id)), avatar.getInputStream(), avatar.getOriginalFilename()));
        } catch (IOException e) {
            throw AdminResponseExceptions.system(e.getMessage());
        }
        return true;
    }

    @ApiOperation(value = "删除头像", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("删除头像")
    @PostMapping(value = "avatar/delete")
    @WrappedApiResponse
    public Boolean deleteAvatar(@Valid @RequestBody UserAvatarRequest request) {
        currentUserService.removeAvatar(new RemoveCurrentUserAvatarCommand(UserIdCodec.toDomain(request.getId())));
        return true;
    }

    @ApiOperation(value = "获取头像相对路径", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @PostMapping(value = "avatar")
    public String avatar(@Valid @RequestBody UserAvatarRequest request) {
        return readAvatarUrl(UserIdCodec.toDomain(request.getId()));
    }

    @ApiOperation(value = "启用/禁用", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("启用")
    @PostMapping(value = "enable")
    @WrappedApiResponse
    public Boolean updateStatus(@Valid @RequestBody List<UserStatusRequest> list) {
        User currentUser = currentUserResolver.currentUser();

        List<ChangeUserStatusCommand> commandList = new ArrayList<>();
        for (UserStatusRequest request : RequestListHelper.present(list)) {
            User bean = userService.get(UserIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            if (bean.isSuper()
                    || bean.getRank().value() >= currentUser.getRank().value()) {
                throw AdminResponseExceptions.permissionDenied();
            }
            commandList.add(new ChangeUserStatusCommand(
                    bean.getId(), Boolean.TRUE.equals(request.getEnable()) ? UserStatus.ENABLED : UserStatus.DISABLED));
        }
        if (commandList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        commandList.forEach(userService::changeStatus);

        return true;
    }

    @ApiOperation(value = "排序", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("排序")
    @PostMapping(value = "sort")
    @WrappedApiResponse
    public Boolean sort(@Valid @RequestBody UserSortRequest request) {
        userService.sort(new UserSortCommand(
                RequestListHelper.map(
                        readOrderedIds(request == null ? null : request.getOrderedIds()), UserIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    private List<Long> readOrderedIds(List<String> sourceList) {
        List<String> orderedIdValues = RequestListHelper.present(sourceList);
        if (sourceList == null || orderedIdValues.size() != sourceList.size() || orderedIdValues.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("orderedIds");
        }
        List<Long> orderedIds = orderedIdValues.stream()
                .map(value -> Long.valueOf(value.trim()))
                .collect(Collectors.toList());
        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw AdminResponseExceptions.invalidParameter("orderedIds");
        }
        return orderedIds;
    }

    @ApiOperation(value = "删除", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("删除")
    @PostMapping(value = "delete")
    @WrappedApiResponse
    public Boolean delete(@Valid @RequestBody List<UserIdRequest> list) {
        User currentUser = currentUserResolver.currentUser();

        List<UserId> idList = new ArrayList<>();
        for (UserIdRequest request : RequestListHelper.present(list)) {
            User bean = userService.get(UserIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            if (bean.isSuper()
                    || bean.getRank().value() >= currentUser.getRank().value()) {
                throw AdminResponseExceptions.permissionDenied();
            }
            idList.add(bean.getId());
        }
        if (idList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }

        idList.forEach(userService::remove);

        return true;
    }

    @ApiOperation(value = "检查 [loginName]是否存在", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @PostMapping(value = "check")
    @WrappedApiResponse
    public Boolean check(@Valid @RequestBody UserCheckRequest request) {
        return isLoginNameAvailable(request.getLoginName(), request.getId());
    }

    @ApiOperation(value = "获取部门树", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @PostMapping(value = "department/tree")
    @WrappedApiResponse
    public List<UserDepartmentResponse> departmentTree() {
        return departmentService.list(new DepartmentQuery()).stream()
                .map(department -> UserInterfaceAssembler.toDepartmentResponse(department, departmentService::get))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "获取权限列表", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @PostMapping(value = "role/list")
    @WrappedApiResponse
    public List<UserRoleResponse> roleList() {
        RoleQuery query = new RoleQuery();
        query.setStatus(RoleStatus.ENABLED);
        return roleService.list(query).stream()
                .map(role -> UserInterfaceAssembler.toRoleResponse(role))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "用户头像", notes = "user")
    @HasPermission("user")
    @GetMapping(value = "avatar")
    public void avatarImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = request.getParameter("id");
        if (StringUtils.isBlank(userId)) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        StoredObject avatar = currentUserService.getAvatar(UserIdCodec.toDomain(Long.valueOf(userId)));
        InputStream inputStream = currentUserService.getAvatarInputStream(UserIdCodec.toDomain(Long.valueOf(userId)));
        if (avatar == null || inputStream == null) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(avatar.getMimeType());

        try (InputStream avatarInputStream = inputStream;
                OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = avatarInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, readBytes);
            }
        }
    }

    private UserQuery readQuery(UserQueryRequest request) {
        UserQuery query = UserInterfaceAssembler.toQuery(request);

        if (request.getDepartmentId() != null) {
            Department department = departmentService.get(DepartmentIdCodec.toDomain(request.getDepartmentId()));
            if (department == null) {
                throw AdminResponseExceptions.objectNotFound();
            }

            query.setDepartmentId(department.getId());
        }

        return query;
    }

    private void validateDepartment(UserDepartmentRequest request) {
        if (request == null || request.getId() == null) {
            throw AdminResponseExceptions.invalidParameter("department.id");

        } else {
            Department bean = departmentService.get(DepartmentIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
        }
    }

    private void validateRoles(List<UserRoleRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return;
        }
        for (UserRoleRequest request : requestList) {
            if (request == null || request.getId() == null) {
                throw AdminResponseExceptions.invalidParameter("roles.id");

            } else {
                Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
                if (bean == null) {
                    throw AdminResponseExceptions.objectNotFound();
                }
            }
        }
    }

    private boolean isLoginNameAvailable(String loginName, String id) {
        if (StringUtils.isBlank(loginName)) {
            return true;
        }
        PrincipalIdentity identity =
                principalIdentityService.get(identityQuery(PrincipalIdentityType.USER_ACCOUNT, loginName));
        if (identity == null) {
            return true;
        }

        return identity.getPrincipalKey() != null
                && Objects.equals(identity.getPrincipalKey().getPrincipalId(), id);
    }

    private UserResponse toResponse(User user) {
        Department department = departmentService.get(user.getDepartmentId());
        return UserInterfaceAssembler.toResponse(
                user,
                getAccountLoginName(user.getId()),
                department,
                loadUserRoles(user),
                readAvatarUrl(user.getId()),
                departmentService::get);
    }

    private List<Role> loadUserRoles(User user) {
        List<Role> userRoles = userService.listUserRoles(userQuery(user.getId()));
        if (userRoles == null) {
            return new ArrayList<>();
        }
        return userRoles.stream()
                .map(role -> role == null ? null : roleService.get(role.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private UserQuery userQuery(UserId userId) {
        UserQuery query = new UserQuery();
        query.setId(userId);
        return query;
    }

    private String getAccountLoginName(UserId userId) {
        PrincipalIdentity identity = getAccountIdentity(userId);
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentity getAccountIdentity(UserId userId) {
        if (userId == null) {
            return null;
        }
        return principalIdentityService.get(identityQuery(
                PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(userId)), PrincipalIdentityType.USER_ACCOUNT));
    }

    private void upsertPassword(User user, String encryptedPassword) {
        PrincipalIdentity accountIdentity = getAccountIdentity(user.getId());
        if (accountIdentity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        PrincipalCredential credential = principalCredentialService.get(
                credentialQuery(accountIdentity.getId(), PrincipalCredentialType.USER_PASSWORD));
        if (credential == null) {
            credential = new PrincipalCredential();
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(user.getId())));
            credential.setIdentityId(accountIdentity.getId());
            credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            principalCredentialService.create(new PrincipalCredentialCommand(credential));
            return;
        }
        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        principalCredentialService.change(new PrincipalCredentialCommand(credential));
    }

    private PrincipalIdentityQuery identityQuery(PrincipalIdentityType identityType, String identityValue) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setIdentityType(identityType);
        query.setIdentityValue(identityValue);
        return query;
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private PrincipalCredentialQuery credentialQuery(
            PrincipalIdentityId identityId, PrincipalCredentialType credentialType) {
        PrincipalCredentialQuery query = new PrincipalCredentialQuery();
        query.setIdentityId(identityId);
        query.setCredentialType(credentialType);
        return query;
    }

    private String getPrivateKey(String token) {
        PreAuthSessionId sessionId = preAuthSessionService.getIdByToken(PreAuthSessionToken.of(token));
        if (sessionId == null) {
            throw AdminResponseExceptions.invalidToken();
        }
        String privateKey = preAuthSessionService.getValue(new PreAuthSessionValueQuery(sessionId, PRIVATE_KEY_ITEM));
        if (StringUtils.isBlank(privateKey)) {
            throw AdminResponseExceptions.invalidToken();
        }
        return privateKey;
    }

    public static String getAvatarUrl(String userId, String token) {
        return String.format(AVATAR_URL_FORMAT, userId, token);
    }

    private String readAvatarUrl(UserId userId) {
        if (!currentUserService.existsAvatar(userId)) {
            return null;
        }
        return getAvatarUrl(UserIdCodec.toStringValue(userId), SandwishContextHolder.currentToken());
    }
}
