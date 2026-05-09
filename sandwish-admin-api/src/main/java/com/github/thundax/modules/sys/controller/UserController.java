package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.utils.encrypt.Sm2Helper;
import com.github.thundax.common.web.annotation.WrappedApiResponse;
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
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.PreAuthSessionService;
import com.github.thundax.modules.auth.service.PrincipalCredentialService;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.utils.PasswordHelper;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.UserInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.UserAvatarRequest;
import com.github.thundax.modules.sys.controller.request.UserCheckRequest;
import com.github.thundax.modules.sys.controller.request.UserDepartmentRequest;
import com.github.thundax.modules.sys.controller.request.UserIdRequest;
import com.github.thundax.modules.sys.controller.request.UserQueryRequest;
import com.github.thundax.modules.sys.controller.request.UserRoleRequest;
import com.github.thundax.modules.sys.controller.request.UserSaveRequest;
import com.github.thundax.modules.sys.controller.request.UserStatusRequest;
import com.github.thundax.modules.sys.controller.response.UserDepartmentResponse;
import com.github.thundax.modules.sys.controller.response.UserResponse;
import com.github.thundax.modules.sys.controller.response.UserRoleResponse;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.ChangeUserStatusCommand;
import com.github.thundax.modules.sys.service.command.DeleteUserCommand;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import com.github.thundax.modules.sys.service.query.UserQuery;
import com.github.thundax.modules.utils.AvatarUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @Autowired
    public UserController(
            UserService userService,
            DepartmentService departmentService,
            RoleService roleService,
            PrincipalIdentityService principalIdentityService,
            PrincipalCredentialService principalCredentialService,
            PreAuthSessionService preAuthSessionService) {

        this.userService = userService;
        this.departmentService = departmentService;
        this.roleService = roleService;
        this.principalIdentityService = principalIdentityService;
        this.principalCredentialService = principalCredentialService;
        this.preAuthSessionService = preAuthSessionService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    @WrappedApiResponse
    public UserResponse get(@Valid @RequestBody UserIdRequest request) throws ApiException {
        User bean = userService.get(userQuery(request.getId()));
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
        }
        return toResponse(bean);
    }

    @ApiOperation(value = "获取列表", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    @WrappedApiResponse
    public List<UserResponse> list(@Valid @RequestBody UserQueryRequest request) throws ApiException {
        UserQuery query = readQuery(request);

        return userService.list(query).stream().map(user -> toResponse(user)).collect(Collectors.toList());
    }

    @ApiOperation(value = "获取分页列表", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @SysLogger("分页")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    public PageResponse<UserResponse> page(@Valid @RequestBody UserQueryRequest request) throws ApiException {
        UserQuery query = readQuery(request);
        PageQuery page = readUserPage(request);

        return PageResponseHelper.fromEntityPage(userService.page(query, page), this::toResponse);
    }

    @ApiOperation(value = "添加", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    @WrappedApiResponse
    public UserResponse add(@Valid @RequestBody UserSaveRequest request) throws ApiException {
        // 解密密码（数据需要加密传输）
        String password = Sm2Helper.decrypt(request.getLoginPass(), getPrivateKey(request.getToken()));
        request.setLoginPass(password);
        validateDepartment(request.getDepartment());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw new InvalidParameterException("loginName");
        }

        if (StringUtils.isBlank(request.getLoginPass())) {
            throw new InvalidParameterException("password");
        }

        User entity = UserInterfaceAssembler.toEntity(new User(), request);
        String encryptedPassword = PasswordHelper.encrypt(request.getLoginPass());

        if (entity.getId() != null) {
            User bean = userService.get(userQuery(entity.getId()));
            if (bean != null) {
                throw new InsertBeanExistException(User.BEAN_NAME, entity.getId());
            }
        }

        entity.setId(userService.create(UserInterfaceAssembler.toCreateCommand(request, encryptedPassword)));

        return toResponse(entity);
    }

    @ApiOperation(value = "更新", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    @WrappedApiResponse
    public UserResponse update(@Valid @RequestBody UserSaveRequest request) throws ApiException {
        // 解密密码（数据需要加密传输）
        if (StringUtils.isNotBlank(request.getLoginPass())) {
            String password = Sm2Helper.decrypt(request.getLoginPass(), getPrivateKey(request.getToken()));
            // 先解密，否则密码规则无法校验
            request.setLoginPass(password);
        }
        validateDepartment(request.getDepartment());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw new InvalidParameterException("loginName");
        }

        User bean = userService.get(userQuery(request.getId()));
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
        }
        User currentUser = UserAccessHolder.currentUser();
        // 非超管用户无权限开启/关闭管理员
        if (!currentUser.isSuper() && Boolean.TRUE.equals(request.getAdmin()) != bean.isAdmin()) {
            throw new PermissionDeniedException();
        }
        // 无权限修改超管/等级高于自身的用户信息
        if (!currentUser.isSuper()) {
            if (bean.isSuper()
                    || bean.getRank().value() >= currentUser.getRank().value()) {
                throw new PermissionDeniedException();
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
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
        @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("上传头像")
    @RequestMapping(
            value = "avatar/upload",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @WrappedApiResponse
    public Boolean uploadAvatar(@RequestParam(value = "id") String id, MultipartFile avatar) throws ApiException {
        return true;
    }

    @ApiOperation(value = "删除头像", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("删除头像")
    @RequestMapping(value = "avatar/delete", method = RequestMethod.POST)
    @WrappedApiResponse
    public Boolean deleteAvatar(@Valid @RequestBody UserAvatarRequest request) throws ApiException {
        return true;
    }

    @ApiOperation(value = "获取头像相对路径", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @RequestMapping(value = "avatar", method = RequestMethod.POST)
    public String avatar(@Valid @RequestBody UserAvatarRequest request) throws ApiException {
        return "";
    }

    @ApiOperation(value = "启用/禁用", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("启用")
    @RequestMapping(value = "enable", method = RequestMethod.POST)
    @WrappedApiResponse
    public Boolean updateStatus(@Valid @RequestBody List<UserStatusRequest> list) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        List<ChangeUserStatusCommand> commandList = new ArrayList<>();
        for (UserStatusRequest request : RequestListHelper.present(list)) {
            User bean = userService.get(userQuery(request.getId()));
            if (bean == null) {
                throw new NullBeanException(User.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
            }
            if (bean.isSuper()
                    || bean.getRank().value() >= currentUser.getRank().value()) {
                throw new PermissionDeniedException();
            }
            commandList.add(new ChangeUserStatusCommand(
                    bean.getId(), Boolean.TRUE.equals(request.getEnable()) ? UserStatus.ENABLED : UserStatus.DISABLED));
        }
        if (commandList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        commandList.forEach(userService::changeStatus);

        return true;
    }

    @ApiOperation(value = "删除", notes = "sys:user:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:edit")
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @WrappedApiResponse
    public Boolean delete(@Valid @RequestBody List<UserIdRequest> list) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        List<DeleteUserCommand> commandList = new ArrayList<>();
        for (UserIdRequest request : RequestListHelper.present(list)) {
            User bean = userService.get(userQuery(request.getId()));
            if (bean == null) {
                throw new NullBeanException(User.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
            }
            if (bean.isSuper()
                    || bean.getRank().value() >= currentUser.getRank().value()) {
                throw new PermissionDeniedException();
            }
            commandList.add(new DeleteUserCommand(bean.getId()));
        }
        if (commandList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        commandList.forEach(userService::remove);

        return true;
    }

    @ApiOperation(value = "检查 [loginName]是否存在", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @RequestMapping(value = "check", method = RequestMethod.POST)
    @WrappedApiResponse
    public Boolean check(@Valid @RequestBody UserCheckRequest request) {
        return isLoginNameAvailable(request.getLoginName(), request.getId());
    }

    @ApiOperation(value = "获取部门树", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @RequestMapping(value = "department/tree", method = RequestMethod.POST)
    @WrappedApiResponse
    public List<UserDepartmentResponse> departmentTree() {
        return departmentService.list(new DepartmentQuery()).stream()
                .map(department -> UserInterfaceAssembler.toDepartmentResponse(department, this::getDepartment))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "获取权限列表", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:user:view")
    @RequestMapping(value = "role/list", method = RequestMethod.POST)
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

        File avatarFile = AvatarUtils.getAvatarFile(userId);
        if (!avatarFile.exists()) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);

        IOUtils.write(FileUtils.readFileToByteArray(avatarFile), response.getOutputStream());
    }

    private UserQuery readQuery(UserQueryRequest request) throws ApiException {
        UserQuery query = UserInterfaceAssembler.toQuery(request);

        if (request.getDepartmentId() != null) {
            Department department = getDepartment(EntityIdCodec.toDomain(request.getDepartmentId()));
            if (department == null) {
                throw new NullBeanException(Department.BEAN_NAME, EntityIdCodec.toDomain(request.getDepartmentId()));
            }

            query.setDepartmentId(EntityIdCodec.toValue(department.getId()));
        }

        return query;
    }

    private void validateDepartment(UserDepartmentRequest request) throws ApiException {
        if (request == null || request.getId() == null) {
            throw new InvalidParameterException("department.id");

        } else {
            Department bean = getDepartment(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Department.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
            }
        }
    }

    private void validateRoles(List<UserRoleRequest> requestList) throws ApiException {
        if (requestList == null || requestList.isEmpty()) {
            return;
        }
        for (UserRoleRequest request : requestList) {
            if (request == null || request.getId() == null) {
                throw new InvalidParameterException("roles.id");

            } else {
                RoleQuery query = new RoleQuery();
                query.setId(EntityIdCodec.toDomain(request.getId()));
                Role bean = roleService.get(query);
                if (bean == null) {
                    throw new NullBeanException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
                }
            }
        }
    }

    private PageQuery readUserPage(UserQueryRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < PageRules.firstPageIndex()) {
            pageNo = PageRules.firstPageIndex();
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }

        PageQuery page = new PageQuery();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

    private boolean isLoginNameAvailable(String loginName, Long id) {
        if (StringUtils.isBlank(loginName)) {
            return true;
        }
        PrincipalIdentity identity =
                principalIdentityService.getByIdentity(PrincipalIdentityType.USER_ACCOUNT, loginName);
        if (identity == null) {
            return true;
        }

        return identity.getPrincipalKey() != null
                && Objects.equals(
                        EntityIdCodec.toValue(identity.getPrincipalKey().getPrincipalId()), id);
    }

    private UserResponse toResponse(User user) {
        Department department = getDepartment(EntityIdCodec.toDomain(user.getDepartmentId()));
        List<Role> roleList = userService.listUserRoles(userQuery(user.getId()));
        return UserInterfaceAssembler.toResponse(
                user, getAccountLoginName(user.getId()), department, roleList, this::getDepartment);
    }

    private Department getDepartment(EntityId departmentId) {
        DepartmentQuery query = new DepartmentQuery();
        query.setId(departmentId);
        return departmentService.get(query);
    }

    private UserQuery userQuery(Long userId) {
        return userQuery(EntityIdCodec.toDomain(userId));
    }

    private UserQuery userQuery(EntityId userId) {
        UserQuery query = new UserQuery();
        query.setId(userId);
        return query;
    }

    private String getAccountLoginName(EntityId userId) {
        PrincipalIdentity identity = getAccountIdentity(userId);
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentity getAccountIdentity(EntityId userId) {
        if (userId == null) {
            return null;
        }
        return principalIdentityService.getByPrincipalKeyAndType(
                PrincipalKey.of(PrincipalType.USER, userId), PrincipalIdentityType.USER_ACCOUNT);
    }

    private void upsertPassword(User user, String encryptedPassword) {
        PrincipalIdentity accountIdentity = getAccountIdentity(user.getId());
        if (accountIdentity == null || StringUtils.isBlank(encryptedPassword)) {
            return;
        }
        PrincipalCredential credential = principalCredentialService.getByIdentityIdAndType(
                accountIdentity.getId(), PrincipalCredentialType.USER_PASSWORD);
        if (credential == null) {
            credential = new PrincipalCredential();
            credential.setPrincipalKey(PrincipalKey.of(PrincipalType.USER, user.getId()));
            credential.setIdentityId(accountIdentity.getId());
            credential.setCredentialType(PrincipalCredentialType.USER_PASSWORD);
            credential.setCredentialValue(encryptedPassword);
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            credential.setNeedChangePassword(false);
            credential.setFailedCount(0);
            credential.setFailedLimit(DEFAULT_PASSWORD_FAILED_LIMIT);
            principalCredentialService.add(credential);
            return;
        }
        credential.setCredentialValue(encryptedPassword);
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        credential.setNeedChangePassword(false);
        credential.setFailedCount(0);
        credential.setLockedUntil(null);
        credential.setLastVerifiedAt(null);
        principalCredentialService.update(credential);
    }

    private String getPrivateKey(String token) throws InvalidTokenException {
        PreAuthSessionId sessionId = preAuthSessionService.findIdByToken(PreAuthSessionToken.of(token));
        if (sessionId == null) {
            throw new InvalidTokenException();
        }
        String privateKey = preAuthSessionService.findValue(sessionId, PRIVATE_KEY_ITEM);
        if (StringUtils.isBlank(privateKey)) {
            throw new InvalidTokenException();
        }
        return privateKey;
    }

    public static String getAvatarUrl(String userId, String token) {
        return String.format(AVATAR_URL_FORMAT, userId, token);
    }
}
