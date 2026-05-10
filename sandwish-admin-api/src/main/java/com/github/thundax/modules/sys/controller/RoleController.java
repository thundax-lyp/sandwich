package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.auth.service.query.PrincipalIdentityQuery;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.RoleInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.RoleAssignUserRequest;
import com.github.thundax.modules.sys.controller.request.RoleIdRequest;
import com.github.thundax.modules.sys.controller.request.RoleMenuRequest;
import com.github.thundax.modules.sys.controller.request.RoleQueryRequest;
import com.github.thundax.modules.sys.controller.request.RoleSaveRequest;
import com.github.thundax.modules.sys.controller.request.RoleSortRequest;
import com.github.thundax.modules.sys.controller.request.RoleStatusRequest;
import com.github.thundax.modules.sys.controller.request.RoleUserRequest;
import com.github.thundax.modules.sys.controller.response.RoleMenuResponse;
import com.github.thundax.modules.sys.controller.response.RoleResponse;
import com.github.thundax.modules.sys.controller.response.RoleUserResponse;
import com.github.thundax.modules.sys.controller.response.RoleUserTreeNodeResponse;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.MenuIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.RoleIdCodec;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.entity.valueobject.UserIdCodec;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.AssignRoleUsersCommand;
import com.github.thundax.modules.sys.service.command.ChangeRoleStatusCommand;
import com.github.thundax.modules.sys.service.command.DeleteRoleCommand;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import com.github.thundax.modules.sys.service.query.UserQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = "系统/权限")
@SysLogger(module = {"系统", "权限"})
@RequestMapping(value = "/api/sys/role")
@WrappedApiController
public class RoleController {

    private static final String DEPARTMENT_ID_PREFIX = "DEPARTMENT_";
    private static final String DEPARTMENT_NAME = "department";
    private static final String MENU_NAME = "Menu";
    private static final String ROLE_NAME = "Role";
    private static final String USER_NAME = "User";

    private final RoleService roleService;
    private final MenuService menuService;
    private final DepartmentService departmentService;
    private final UserService userService;
    private final PrincipalIdentityService principalIdentityService;

    @Autowired
    public RoleController(
            RoleService roleService,
            MenuService menuService,
            DepartmentService departmentService,
            UserService userService,
            PrincipalIdentityService principalIdentityService) {

        this.roleService = roleService;
        this.menuService = menuService;
        this.departmentService = departmentService;
        this.userService = userService;
        this.principalIdentityService = principalIdentityService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:role:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:role:view")
    @SysLogger("读取")
    @PostMapping(value = "get")
    public RoleResponse get(@Valid @RequestBody RoleIdRequest request) throws ApiException {
        Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(ROLE_NAME, RoleIdCodec.toDomain(request.getId()));
        }
        return toResponse(bean);
    }

    @ApiOperation(value = "获取列表", notes = "sys:role:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:role:view")
    @SysLogger("列表")
    @PostMapping(value = "list")
    public List<RoleResponse> list(@Valid @RequestBody RoleQueryRequest request) throws ApiException {
        RoleQuery query = RoleInterfaceAssembler.toQuery(request);

        return roleService.list(query).stream().map(role -> toResponse(role)).collect(Collectors.toList());
    }

    @ApiOperation(value = "添加", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:role:edit")
    @SysLogger("添加")
    @PostMapping(value = "create")
    public RoleResponse add(@Valid @RequestBody RoleSaveRequest request) throws ApiException {
        validateMenus(request.getMenuList());

        if (request.getId() != null) {
            Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
            if (bean != null) {
                throw new InsertBeanExistException(ROLE_NAME, RoleIdCodec.toDomain(request.getId()));
            }
        }

        Role entity = RoleInterfaceAssembler.toEntity(new Role(), request);
        entity.setId(roleService.create(RoleInterfaceAssembler.toCreateCommand(request)));

        return toResponse(entity);
    }

    @ApiOperation(value = "更新", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:role:edit")
    @SysLogger("更新")
    @PostMapping(value = "update")
    public RoleResponse update(@Valid @RequestBody RoleSaveRequest request) throws ApiException {
        validateMenus(request.getMenuList());

        Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(ROLE_NAME, RoleIdCodec.toDomain(request.getId()));
        }

        Role entity = RoleInterfaceAssembler.toEntity(bean, request);

        roleService.changeInfo(RoleInterfaceAssembler.toChangeInfoCommand(request));

        return toResponse(entity);
    }

    @ApiOperation(value = "启用/禁用", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:role:edit")
    @SysLogger("启用")
    @PostMapping(value = "enable")
    public Boolean updateStatus(@Valid @RequestBody List<RoleStatusRequest> list) throws ApiException {
        List<ChangeRoleStatusCommand> commandList = new ArrayList<>();
        for (RoleStatusRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(ROLE_NAME, RoleIdCodec.toDomain(request.getId()));
            }
            commandList.add(new ChangeRoleStatusCommand(
                    bean.getId(), Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED));
        }
        if (commandList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        commandList.forEach(roleService::changeStatus);

        return true;
    }

    @ApiOperation(value = "排序", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:role:edit")
    @SysLogger("排序")
    @PostMapping(value = "sort")
    public Boolean updatePriority(@Valid @RequestBody RoleSortRequest request) throws ApiException {
        roleService.sort(
                RequestListHelper.map(request == null ? null : request.getOrderedIds(), RoleIdCodec::toDomain),
                request == null ? SortDirection.ASC : request.getSortDirection());
        return true;
    }

    @ApiOperation(value = "删除", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:role:edit")
    @SysLogger("删除")
    @PostMapping(value = "delete")
    public Boolean delete(@Valid @RequestBody List<RoleIdRequest> list) throws ApiException {
        List<DeleteRoleCommand> commandList = new ArrayList<>();
        for (RoleIdRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(ROLE_NAME, RoleIdCodec.toDomain(request.getId()));
            }
            commandList.add(new DeleteRoleCommand(bean.getId()));
        }
        if (commandList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        commandList.forEach(roleService::remove);

        return true;
    }

    @ApiOperation(value = "获取菜单树", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission({"sys:role:view", "sys:role:edit"})
    @PostMapping(value = "menu/tree")
    public List<RoleMenuResponse> menuTree() {
        return menuService.list(new MenuQuery()).stream()
                .map(menu -> RoleInterfaceAssembler.toMenuResponse(menu))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "获取用户树", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission({"sys:role:view", "sys:role:edit"})
    @PostMapping(value = "user/tree")
    public List<RoleUserTreeNodeResponse> userTree() {
        List<RoleUserTreeNodeResponse> list = new ArrayList<>();

        list.addAll(departmentService.list(new DepartmentQuery()).stream()
                .map(department -> RoleInterfaceAssembler.toDepartmentTreeNode(
                        DEPARTMENT_ID_PREFIX + DepartmentIdCodec.toValue(department.getId()), department))
                .collect(Collectors.toList()));

        list.addAll(userService.list(new UserQuery()).stream()
                .map(user -> RoleInterfaceAssembler.toUserTreeNode(
                        DEPARTMENT_ID_PREFIX,
                        user,
                        getAccountLoginName(user),
                        departmentService.get(user.getDepartmentId()),
                        departmentService::get))
                .collect(Collectors.toList()));

        return list;
    }

    @ApiOperation(value = "获取权限用户列表", notes = "sys:role:view, sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission({"sys:role:view", "sys:role:edit"})
    @PostMapping(value = "user/list")
    public List<RoleUserResponse> userList(@Valid @RequestBody RoleIdRequest request) throws ApiException {
        Role bean = roleService.get(RoleIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(ROLE_NAME, RoleIdCodec.toDomain(request.getId()));
        }

        return roleService.listRoleUsers(roleQuery(request.getId())).stream()
                .map(user -> toUserResponse(userService.get(user.getId())))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "更新权限用户列表", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("sys:role:edit")
    @SysLogger("授权")
    @PostMapping(value = "user/assign")
    public Boolean assignUser(@Valid @RequestBody RoleAssignUserRequest request) throws ApiException {
        validateAssignUser(request);

        roleService.assignUsers(new AssignRoleUsersCommand(
                RoleIdCodec.toDomain(request.getRoleId()),
                request.getUsers().stream()
                        .map(vo -> UserIdCodec.toDomain(vo.getId()))
                        .collect(Collectors.toList())));

        return true;
    }

    private RoleResponse toResponse(Role role) {
        return RoleInterfaceAssembler.toResponse(role, roleService.listRoleMenus(roleQuery(role)));
    }

    private RoleUserResponse toUserResponse(User user) {
        return RoleInterfaceAssembler.toUserResponse(
                user, getAccountLoginName(user), departmentService.get(user.getDepartmentId()), departmentService::get);
    }

    private String getAccountLoginName(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.get(identityQuery(
                PrincipalKey.of(PrincipalType.USER, UserIdCodec.toValue(user.getId())),
                PrincipalIdentityType.USER_ACCOUNT));
        return identity == null ? null : identity.getIdentityValue();
    }

    private PrincipalIdentityQuery identityQuery(PrincipalKey principalKey, PrincipalIdentityType identityType) {
        PrincipalIdentityQuery query = new PrincipalIdentityQuery();
        query.setPrincipalKey(principalKey);
        query.setIdentityType(identityType);
        return query;
    }

    private void validateAssignUser(RoleAssignUserRequest request) throws ApiException {
        Role roleBean = roleService.get(RoleIdCodec.toDomain(request.getRoleId()));
        if (roleBean == null) {
            throw new NullBeanException(ROLE_NAME, RoleIdCodec.toDomain(request.getRoleId()));
        }

        if (request.getUsers() == null || request.getUsers().isEmpty()) {
            throw new InvalidParameterException("users");
        }

        for (RoleUserRequest userRequest : request.getUsers()) {
            User userBean = userService.get(UserIdCodec.toDomain(userRequest.getId()));
            if (userBean == null) {
                throw new NullBeanException(USER_NAME, UserIdCodec.toDomain(userRequest.getId()));
            }
        }
    }

    private UserQuery userQuery(UserId userId) {
        UserQuery query = new UserQuery();
        query.setId(userId);
        return query;
    }

    private UserQuery userQuery(Long userId) {
        return userQuery(UserIdCodec.toDomain(userId));
    }

    private RoleQuery roleQuery(Role role) {
        return roleQuery(RoleIdCodec.toValue(role.getId()));
    }

    private RoleQuery roleQuery(Long roleId) {
        RoleQuery query = new RoleQuery();
        query.setId(RoleIdCodec.toDomain(roleId));
        return query;
    }

    private void validateMenus(List<RoleMenuRequest> requestList) throws ApiException {
        if (requestList == null || requestList.isEmpty()) {
            return;
        }
        for (RoleMenuRequest request : requestList) {
            if (request == null || request.getId() == null) {
                throw new InvalidParameterException("menus.id");

            } else {
                Menu bean = menuService.get(MenuIdCodec.toDomain(request.getId()));
                if (bean == null) {
                    throw new NullBeanException(MENU_NAME, MenuIdCodec.toDomain(request.getId()));
                }
            }
        }
    }
}
