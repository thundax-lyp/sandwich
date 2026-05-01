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
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.service.KeypairService;
import com.github.thundax.modules.auth.service.PasswordService;
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
import javax.validation.Validator;
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
public class UserApiController extends BaseApiController {

    private static final String AVATAR_URL_FORMAT = "/api/sys/user/avatar?id=%s&token=%s";

    private final UserService userService;
    private final OfficeService officeService;
    private final RoleService roleService;
    private final KeypairService keypairService;
    private final PasswordService passwordService;
    private final UserInterfaceAssembler userInterfaceAssembler;

    @Autowired
    public UserApiController(
            UserService userService,
            OfficeService officeService,
            RoleService roleService,
            Validator validator,
            KeypairService keypairService,
            PasswordService passwordService) {
        super(validator);

        this.userService = userService;
        this.officeService = officeService;
        this.roleService = roleService;
        this.keypairService = keypairService;
        this.passwordService = passwordService;
        this.userInterfaceAssembler = new UserInterfaceAssembler();
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
    public UserResponse get(@RequestBody UserIdRequest request) throws ApiException {
        User bean = userService.get(userInterfaceAssembler.toEntityId(request.getId()));
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
    public List<UserResponse> list(@RequestBody UserQueryRequest request) throws ApiException {
        validate(request);

        User query = readQuery(request);

        return userService.findList(query).stream()
                .map(user -> toResponse(user))
                .collect(Collectors.toList());
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
    public PageVo<UserResponse> page(@RequestBody UserQueryRequest request) throws ApiException {
        validate(request);

        User query = readQuery(request);
        Page<User> page = readUserPage(request);

        return entityPageToVo(userService.findPage(query, page), this::toResponse);
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
    public UserResponse add(@RequestBody UserSaveRequest request) throws ApiException {
        // 解密密码（数据需要加密传输）
        String password = Sm2.decrypt(request.getLoginPass(), keypairService.getPrivateKey(request.getToken()));
        request.setLoginPass(password);
        validate(request);
        validateOffice(request.getOffice());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw new InvalidParameterException("loginName");
        }

        if (StringUtils.isBlank(request.getLoginPass())) {
            throw new InvalidParameterException("password");
        }

        User entity = userInterfaceAssembler.toEntity(new User(), request);
        entity.setLoginPass(passwordService.encrypt(request.getLoginPass()));

        if (entity.getId() != null) {
            User bean = userService.get(userInterfaceAssembler.toEntityId(EntityIdCodec.toValue(entity.getId())));
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
    public UserResponse update(@RequestBody UserSaveRequest request) throws ApiException {
        // 解密密码（数据需要加密传输）
        if (StringUtils.isNotBlank(request.getLoginPass())) {
            String password = Sm2.decrypt(request.getLoginPass(), keypairService.getPrivateKey(request.getToken()));
            // 先解密，否则密码规则无法校验
            request.setLoginPass(password);
        }
        validate(request);
        validateOffice(request.getOffice());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw new InvalidParameterException("loginName");
        }

        if (!isSsoLoginNameAvailable(request.getSsoLoginName(), request.getId())) {
            throw new InvalidParameterException("ssoLoginName");
        }

        User bean = userService.get(userInterfaceAssembler.toEntityId(request.getId()));
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, request.getId());
        }
        // 非超管用户无权限开启/关闭管理员
        if (!currentUser().isSuper() && Boolean.TRUE.equals(request.getAdmin()) != bean.isAdmin()) {
            throw new PermissionDeniedException();
        }
        // 无权限修改超管/等级高于自身的用户信息
        if (!currentUser().isSuper()) {
            if (bean.isSuper() || (bean.getRanks() >= currentUser().getRanks())) {
                throw new PermissionDeniedException();
            }
        }

        User entity = userInterfaceAssembler.toEntity(bean, request);

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
    public Boolean deleteAvatar(@RequestBody UserAvatarRequest request) throws ApiException {
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
    public String avatar(@RequestBody UserAvatarRequest request) throws ApiException {
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
        User currentUser = currentUser();

        List<User> beanList = validateList(
                list,
                vo -> userService.get(userInterfaceAssembler.toEntityId(vo.getId())),
                (bean, vo) -> {
                    if (bean.isSuper() || bean.getRanks() >= currentUser.getRanks()) {
                        throw new PermissionDeniedException();
                    }
                    return true;
                },
                (bean, vo) ->
                        bean.setStatus(Boolean.TRUE.equals(vo.getEnable()) ? UserStatus.ENABLED : UserStatus.DISABLED));

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
        User currentUser = currentUser();

        List<User> beanList = validateList(
                list,
                vo -> userService.get(userInterfaceAssembler.toEntityId(vo.getId())),
                (bean, vo) -> {
                    if (bean.isSuper() || bean.getRanks() >= currentUser.getRanks()) {
                        throw new PermissionDeniedException();
                    }
                    return true;
                },
                null);

        userService.delete(beanList);

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
    public Boolean check(@RequestBody UserCheckRequest request) {
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
    public Boolean checkSsoLoginName(@RequestBody UserCheckRequest request) {
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
        return officeService.findList(new Office()).stream()
                .map(office -> userInterfaceAssembler.toOfficeResponse(office, officeService::get))
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
        Role query = new Role();
        Role.Query queryCondition = new Role.Query();
        queryCondition.setStatus(RoleStatus.ENABLED);
        query.setQuery(queryCondition);

        return roleService.findList(query).stream()
                .map(role -> userInterfaceAssembler.toRoleResponse(role))
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

    private User readQuery(UserQueryRequest request) throws ApiException {
        User query = new User();
        User.Query queryCondition = new User.Query();

        queryCondition.setLoginName(request.getLoginName());
        queryCondition.setName(request.getName());

        if (request.getEnable() != null) {
            queryCondition.setStatus(request.getEnable() ? UserStatus.ENABLED : UserStatus.DISABLED);
        }

        if (StringUtils.isNotBlank(request.getOfficeId())) {
            Office office = officeService.get(userInterfaceAssembler.toEntityId(request.getOfficeId()));
            if (office == null) {
                throw new NullBeanException(Office.BEAN_NAME, request.getOfficeId());
            }

            queryCondition.setOfficeId(EntityIdCodec.toValue(office.getId()));
        }

        queryCondition.setOrderBy(request.getOrderBy());
        query.setQuery(queryCondition);

        return query;
    }

    private void validateOffice(UserOfficeRequest request) throws ApiException {
        if (request == null || StringUtils.isBlank(request.getId())) {
            throw new InvalidParameterException("office.id");

        } else {
            Office bean = officeService.get(userInterfaceAssembler.toEntityId(request.getId()));
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
                Role bean = roleService.get(userInterfaceAssembler.toEntityId(request.getId()));
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
        Office office = officeService.get(userInterfaceAssembler.toEntityId(user.getOfficeId()));
        List<Role> roleList = userService.findUserRole(user);
        return userInterfaceAssembler.toResponse(user, office, roleList, officeService::get);
    }

    public static String getAvatarUrl(String userId, String token) {
        return String.format(AVATAR_URL_FORMAT, userId, token);
    }
}
