package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.exception.PermissionDeniedException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.vo.PageResponse;
import com.github.thundax.common.web.ApiRequestListHelper;
import com.github.thundax.common.web.PageResponseHelper;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.UserInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.enums.UserStatus;
import com.github.thundax.modules.sys.request.UserAvatarRequest;
import com.github.thundax.modules.sys.request.UserCheckRequest;
import com.github.thundax.modules.sys.request.UserIdRequest;
import com.github.thundax.modules.sys.request.UserOfficeRequest;
import com.github.thundax.modules.sys.request.UserQueryRequest;
import com.github.thundax.modules.sys.request.UserRoleRequest;
import com.github.thundax.modules.sys.request.UserSaveRequest;
import com.github.thundax.modules.sys.request.UserStatusRequest;
import com.github.thundax.modules.sys.response.UserOfficeResponse;
import com.github.thundax.modules.sys.response.UserResponse;
import com.github.thundax.modules.sys.response.UserRoleResponse;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import com.github.thundax.modules.sys.service.query.UserQuery;
import com.github.thundax.modules.utils.AvatarUtils;
import com.github.thundax.modules.utils.IPUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "02-05.系统-用户")
@SysLogger(module = {"系统", "用户"})
@RequestMapping(value = "/api/sys/user")
@RestController
public class UserController {

    private static final String AVATAR_URL_FORMAT = "/api/sys/user/avatar?id=%s&token=%s";

    private final UserService userService;
    private final OfficeService officeService;
    private final RoleService roleService;
    private final KeypairService keypairService;
    private final PasswordService passwordService;

    @Autowired
    public UserController(
            UserService userService,
            OfficeService officeService,
            RoleService roleService,
            KeypairService keypairService,
            PasswordService passwordService) {

        this.userService = userService;
        this.officeService = officeService;
        this.roleService = roleService;
        this.keypairService = keypairService;
        this.passwordService = passwordService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:view')")
    public UserResponse get(@Valid @RequestBody UserIdRequest request) throws ApiException {
        User bean = userService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, request.getId());
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
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:view')")
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
    @SysLogger("分页")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:view')")
    public PageResponse<UserResponse> page(@Valid @RequestBody UserQueryRequest request) throws ApiException {
        UserQuery query = readQuery(request);
        Page<User> page = readUserPage(request);

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
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:edit')")
    public UserResponse add(@Valid @RequestBody UserSaveRequest request) throws ApiException {
        // 解密密码（数据需要加密传输）
        String password = Sm2.decrypt(request.getLoginPass(), keypairService.getPrivateKey(request.getToken()));
        request.setLoginPass(password);
        validateOffice(request.getOffice());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw new InvalidParameterException("loginName");
        }

        if (StringUtils.isBlank(request.getLoginPass())) {
            throw new InvalidParameterException("password");
        }

        User entity = UserInterfaceAssembler.toEntity(new User(), request);
        entity.setLoginPass(passwordService.encrypt(request.getLoginPass()));

        if (entity.getId() != null) {
            User bean = userService.getById(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(User.BEAN_NAME, EntityIdCodec.toValue(entity.getId()));
            }
        }

        entity.setRegisterDate(new Date());
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        entity.setRegisterIp(IPUtils.getIpAddr(currentRequest));

        userService.add(entity);

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
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:edit')")
    public UserResponse update(@Valid @RequestBody UserSaveRequest request) throws ApiException {
        // 解密密码（数据需要加密传输）
        if (StringUtils.isNotBlank(request.getLoginPass())) {
            String password = Sm2.decrypt(request.getLoginPass(), keypairService.getPrivateKey(request.getToken()));
            // 先解密，否则密码规则无法校验
            request.setLoginPass(password);
        }
        validateOffice(request.getOffice());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw new InvalidParameterException("loginName");
        }

        if (!isSsoLoginNameAvailable(request.getSsoLoginName(), request.getId())) {
            throw new InvalidParameterException("ssoLoginName");
        }

        User bean = userService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, request.getId());
        }
        User currentUser = UserAccessHolder.currentUser();
        // 非超管用户无权限开启/关闭管理员
        if (!currentUser.isSuper() && Boolean.TRUE.equals(request.getAdmin()) != bean.isAdmin()) {
            throw new PermissionDeniedException();
        }
        // 无权限修改超管/等级高于自身的用户信息
        if (!currentUser.isSuper()) {
            if (bean.isSuper() || (bean.getRanks() >= currentUser.getRanks())) {
                throw new PermissionDeniedException();
            }
        }

        User entity = UserInterfaceAssembler.toEntity(bean, request);

        userService.update(entity);

        if (StringUtils.isNotBlank(request.getLoginPass())) {
            entity.setLoginPass(passwordService.encrypt(request.getLoginPass()));
            userService.updatePassword(entity);
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
    @SysLogger("上传头像")
    @RequestMapping(
            value = "avatar/upload",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:edit')")
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
    @SysLogger("删除头像")
    @RequestMapping(value = "avatar/delete", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:edit')")
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
    @RequestMapping(value = "avatar", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:view')")
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
    @SysLogger("启用")
    @RequestMapping(value = "enable", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:edit')")
    public Boolean updateStatus(@RequestBody List<UserStatusRequest> list) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        List<User> beanList = ApiRequestListHelper.mapNotEmpty(list, request -> {
            User bean = userService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(User.BEAN_NAME, request.getId());
            }
            if (bean.isSuper() || bean.getRanks() >= currentUser.getRanks()) {
                throw new PermissionDeniedException();
            }
            bean.setStatus(Boolean.TRUE.equals(request.getEnable()) ? UserStatus.ENABLED : UserStatus.DISABLED);
            return bean;
        });

        userService.updateStatus(beanList);

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
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:edit')")
    public Boolean delete(@RequestBody List<UserIdRequest> list) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        List<User> beanList = ApiRequestListHelper.mapNotEmpty(list, request -> {
            User bean = userService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(User.BEAN_NAME, request.getId());
            }
            if (bean.isSuper() || bean.getRanks() >= currentUser.getRanks()) {
                throw new PermissionDeniedException();
            }
            return bean;
        });

        userService.batchDeleteById(beanList.stream().map(User::getId).collect(Collectors.toList()));

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
    @RequestMapping(value = "check", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:view')")
    public Boolean check(@Valid @RequestBody UserCheckRequest request) {
        return isLoginNameAvailable(request.getLoginName(), request.getId());
    }

    @ApiOperation(value = "检查 [ssoLoginName]是否存在", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "check-sso-loginName", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:view')")
    public Boolean checkSsoLoginName(@Valid @RequestBody UserCheckRequest request) {
        return isSsoLoginNameAvailable(request.getSsoLoginName(), request.getId());
    }

    @ApiOperation(value = "获取部门树", notes = "sys:user:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @RequestMapping(value = "office/tree", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:view')")
    public List<UserOfficeResponse> officeTree() {
        return officeService.list(new Office()).stream()
                .map(office -> UserInterfaceAssembler.toOfficeResponse(office, officeService::getById))
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
    @RequestMapping(value = "role/list", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:user:view')")
    public List<UserRoleResponse> roleList() {
        RoleQuery query = new RoleQuery();
        query.setStatus(RoleStatus.ENABLED);

        return roleService.list(query).stream()
                .map(role -> UserInterfaceAssembler.toRoleResponse(role))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "用户头像", notes = "user")
    @GetMapping(value = "avatar")
    @PreAuthorize("@permissionAuthorizationService.isPermitted('user')")
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

        if (StringUtils.isNotBlank(request.getOfficeId())) {
            Office office = officeService.getById(EntityIdCodec.toDomain(request.getOfficeId()));
            if (office == null) {
                throw new NullBeanException(Office.BEAN_NAME, request.getOfficeId());
            }

            query.setOfficeId(EntityIdCodec.toValue(office.getId()));
        }

        return query;
    }

    private void validateOffice(UserOfficeRequest request) throws ApiException {
        if (request == null || StringUtils.isBlank(request.getId())) {
            throw new InvalidParameterException("office.id");

        } else {
            Office bean = officeService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Office.BEAN_NAME, request.getId());
            }
        }
    }

    private void validateRoles(List<UserRoleRequest> requestList) throws ApiException {
        if (requestList == null || requestList.isEmpty()) {
            return;
        }
        for (UserRoleRequest request : requestList) {
            if (request == null || StringUtils.isBlank(request.getId())) {
                throw new InvalidParameterException("roles.id");

            } else {
                Role bean = roleService.getById(EntityIdCodec.toDomain(request.getId()));
                if (bean == null) {
                    throw new NullBeanException(Role.BEAN_NAME, request.getId());
                }
            }
        }
    }

    private Page<User> readUserPage(UserQueryRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<User> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

    private boolean isLoginNameAvailable(String loginName, String id) {
        if (StringUtils.isBlank(loginName)) {
            return true;
        }
        User bean = userService.getByLoginName(loginName);
        if (bean == null) {
            return true;
        }

        return StringUtils.equals(EntityIdCodec.toValue(bean.getId()), id);
    }

    private boolean isSsoLoginNameAvailable(String ssoLoginName, String id) {
        if (StringUtils.isBlank(ssoLoginName)) {
            return true;
        }
        User bean = userService.getBySsoLoginName(ssoLoginName);
        if (bean == null) {
            return true;
        }

        return StringUtils.equals(EntityIdCodec.toValue(bean.getId()), id);
    }

    private UserResponse toResponse(User user) {
        Office office = officeService.getById(EntityIdCodec.toDomain(user.getOfficeId()));
        List<Role> roleList = userService.listUserRoles(user);
        return UserInterfaceAssembler.toResponse(user, office, roleList, officeService::getById);
    }

    public static String getAvatarUrl(String userId, String token) {
        return String.format(AVATAR_URL_FORMAT, userId, token);
    }
}
