package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.security.annotation.HasPermission;
import com.github.thundax.common.web.annotation.WrappedApiController;
import com.github.thundax.common.web.request.RequestListHelper;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.auth.service.PrincipalIdentityService;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.RoleInterfaceAssembler;
import com.github.thundax.modules.sys.controller.request.RoleAssignUserRequest;
import com.github.thundax.modules.sys.controller.request.RoleIdRequest;
import com.github.thundax.modules.sys.controller.request.RoleMenuRequest;
import com.github.thundax.modules.sys.controller.request.RolePriorityRequest;
import com.github.thundax.modules.sys.controller.request.RoleQueryRequest;
import com.github.thundax.modules.sys.controller.request.RoleSaveRequest;
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
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.command.AssignRoleUsersCommand;
import com.github.thundax.modules.sys.service.command.ChangeRolePriorityCommand;
import com.github.thundax.modules.sys.service.command.ChangeRoleStatusCommand;
import com.github.thundax.modules.sys.service.command.DeleteRoleCommand;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "系统/权限")
@SysLogger(module = {"系统", "权限"})
@RequestMapping(value = "/api/sys/role")
@WrappedApiController
public class RoleController {

    private static final String DEPARTMENT_ID_PREFIX = "DEPARTMENT_";
    private static final String MENU_NAME = "Menu";

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
    @RequestMapping(value = "get", method = RequestMethod.POST)
    public RoleResponse get(@Valid @RequestBody RoleIdRequest request) throws ApiException {
        Role bean = roleService.get(roleQuery(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
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
    @RequestMapping(value = "list", method = RequestMethod.POST)
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
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public RoleResponse add(@Valid @RequestBody RoleSaveRequest request) throws ApiException {
        validateMenus(request.getMenuList());

        if (request.getId() != null) {
            Role bean = roleService.get(roleQuery(request.getId()));
            if (bean != null) {
                throw new InsertBeanExistException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
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
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public RoleResponse update(@Valid @RequestBody RoleSaveRequest request) throws ApiException {
        validateMenus(request.getMenuList());

        Role bean = roleService.get(roleQuery(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
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
    @RequestMapping(value = "enable", method = RequestMethod.POST)
    public Boolean updateStatus(@Valid @RequestBody List<RoleStatusRequest> list) throws ApiException {
        List<ChangeRoleStatusCommand> commandList = new ArrayList<>();
        for (RoleStatusRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.get(roleQuery(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
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
    @RequestMapping(value = "priority", method = RequestMethod.POST)
    public Boolean updatePriority(@Valid @RequestBody List<RolePriorityRequest> list) throws ApiException {
        List<ChangeRolePriorityCommand> commandList = new ArrayList<>();
        for (RolePriorityRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.get(roleQuery(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
            }
            commandList.add(new ChangeRolePriorityCommand(
                    bean.getId(), request.getPriority() == null ? 0 : request.getPriority()));
        }
        if (commandList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        commandList.forEach(roleService::changePriority);

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
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public Boolean delete(@Valid @RequestBody List<RoleIdRequest> list) throws ApiException {
        List<DeleteRoleCommand> commandList = new ArrayList<>();
        for (RoleIdRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.get(roleQuery(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
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
    @RequestMapping(value = "menu/tree", method = RequestMethod.POST)
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
    @RequestMapping(value = "user/tree", method = RequestMethod.POST)
    public List<RoleUserTreeNodeResponse> userTree() {
        List<RoleUserTreeNodeResponse> list = new ArrayList<>();

        list.addAll(departmentService.listAll().stream()
                .map(department -> RoleInterfaceAssembler.toDepartmentTreeNode(
                        DEPARTMENT_ID_PREFIX + department.getId(), department))
                .collect(Collectors.toList()));

        list.addAll(userService.list(new UserQuery()).stream()
                .map(user -> RoleInterfaceAssembler.toUserTreeNode(
                        DEPARTMENT_ID_PREFIX,
                        user,
                        getAccountLoginName(user),
                        departmentService.getById(EntityIdCodec.toDomain(user.getDepartmentId())),
                        departmentService::getById))
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
    @RequestMapping(value = "user/list", method = RequestMethod.POST)
    public List<RoleUserResponse> userList(@Valid @RequestBody RoleIdRequest request) throws ApiException {
        Role bean = roleService.get(roleQuery(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getId()));
        }

        return roleService.listRoleUsers(roleQuery(request.getId())).stream()
                .map(user -> toUserResponse(userService.get(userQuery(user.getId()))))
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
    @RequestMapping(value = "user/assign", method = RequestMethod.POST)
    public Boolean assignUser(@Valid @RequestBody RoleAssignUserRequest request) throws ApiException {
        validateAssignUser(request);

        roleService.assignUsers(new AssignRoleUsersCommand(
                EntityIdCodec.toDomain(request.getRoleId()),
                request.getUsers().stream()
                        .map(vo -> EntityIdCodec.toDomain(vo.getId()))
                        .collect(Collectors.toList())));

        return true;
    }

    private RoleResponse toResponse(Role role) {
        return RoleInterfaceAssembler.toResponse(role, roleService.listRoleMenus(roleQuery(role)));
    }

    private RoleUserResponse toUserResponse(User user) {
        return RoleInterfaceAssembler.toUserResponse(
                user,
                getAccountLoginName(user),
                departmentService.getById(EntityIdCodec.toDomain(user.getDepartmentId())),
                departmentService::getById);
    }

    private String getAccountLoginName(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        PrincipalIdentity identity = principalIdentityService.getByPrincipalKeyAndType(
                PrincipalKey.of(PrincipalType.USER, user.getId()), PrincipalIdentityType.USER_ACCOUNT);
        return identity == null ? null : identity.getIdentityValue();
    }

    private void validateAssignUser(RoleAssignUserRequest request) throws ApiException {
        Role roleBean = roleService.get(roleQuery(request.getRoleId()));
        if (roleBean == null) {
            throw new NullBeanException(Role.BEAN_NAME, EntityIdCodec.toDomain(request.getRoleId()));
        }

        if (request.getUsers() == null || request.getUsers().isEmpty()) {
            throw new InvalidParameterException("users");
        }

        for (RoleUserRequest userRequest : request.getUsers()) {
            User userBean = userService.get(userQuery(userRequest.getId()));
            if (userBean == null) {
                throw new NullBeanException(User.BEAN_NAME, EntityIdCodec.toDomain(userRequest.getId()));
            }
        }
    }

    private UserQuery userQuery(EntityId userId) {
        UserQuery query = new UserQuery();
        query.setId(userId);
        return query;
    }

    private UserQuery userQuery(Long userId) {
        return userQuery(EntityIdCodec.toDomain(userId));
    }

    private RoleQuery roleQuery(Role role) {
        return roleQuery(EntityIdCodec.toValue(role.getId()));
    }

    private RoleQuery roleQuery(Long roleId) {
        RoleQuery query = new RoleQuery();
        query.setId(EntityIdCodec.toDomain(roleId));
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
                Menu bean = menuService.get(menuQuery(EntityIdCodec.toDomain(request.getId())));
                if (bean == null) {
                    throw new NullBeanException(MENU_NAME, EntityIdCodec.toDomain(request.getId()));
                }
            }
        }
    }

    private MenuQuery menuQuery(EntityId menuId) {
        MenuQuery query = new MenuQuery();
        query.setId(menuId);
        return query;
    }
}
