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
import com.github.thundax.modules.sys.service.UserIdentityService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.service.query.MenuQuery;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
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
    private final UserIdentityService userIdentityService;

    @Autowired
    public RoleController(
            RoleService roleService,
            MenuService menuService,
            DepartmentService departmentService,
            UserService userService,
            UserIdentityService userIdentityService) {

        this.roleService = roleService;
        this.menuService = menuService;
        this.departmentService = departmentService;
        this.userService = userService;
        this.userIdentityService = userIdentityService;
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
        Role bean = roleService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getId());
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

        Role entity = RoleInterfaceAssembler.toEntity(new Role(), request);
        if (entity.getId() != null) {
            Role bean = roleService.getById(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Role.BEAN_NAME, EntityIdCodec.toValue(entity.getId()));
            }
        }

        roleService.add(entity);

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

        Role bean = roleService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getId());
        }

        Role entity = RoleInterfaceAssembler.toEntity(bean, request);

        roleService.update(entity);

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
        List<Role> beanList = new ArrayList<>();
        for (RoleStatusRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Role.BEAN_NAME, request.getId());
            }
            bean.setStatus(Boolean.TRUE.equals(request.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED);
            beanList.add(bean);
        }
        if (beanList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        roleService.batchUpdateStatus(beanList);

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
        List<Role> beanList = new ArrayList<>();
        for (RolePriorityRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Role.BEAN_NAME, request.getId());
            }
            bean.setPriority(request.getPriority() == null ? 0 : request.getPriority());
            beanList.add(bean);
        }
        if (beanList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        roleService.updatePriority(beanList);

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
        List<Role> beanList = new ArrayList<>();
        for (RoleIdRequest request : RequestListHelper.present(list)) {
            Role bean = roleService.getById(EntityIdCodec.toDomain(request.getId()));
            if (bean == null) {
                throw new NullBeanException(Role.BEAN_NAME, request.getId());
            }
            beanList.add(bean);
        }
        if (beanList.isEmpty()) {
            throw new InvalidParameterException("list");
        }

        roleService.batchDeleteById(beanList.stream().map(Role::getId).collect(Collectors.toList()));

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

        list.addAll(userService.listAll().stream()
                .map(user -> RoleInterfaceAssembler.toUserTreeNode(
                        DEPARTMENT_ID_PREFIX,
                        user,
                        userIdentityService.getAccountLoginName(user.getId()),
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
        Role bean = roleService.getById(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getId());
        }

        return roleService.listRoleUsers(bean).stream()
                .map(user -> toUserResponse(userService.getById(user.getId())))
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

        Role roleBean = roleService.getById(EntityIdCodec.toDomain(request.getRoleId()));
        Assert.notNull(roleBean, "role can not be null");

        roleService.updateUserList(
                roleBean,
                request.getUsers().stream().map(vo -> newUser(vo.getId())).collect(Collectors.toList()));

        return true;
    }

    private User newUser(String id) {
        User user = new User();
        user.setId(EntityIdCodec.toDomain(id));
        return user;
    }

    private RoleResponse toResponse(Role role) {
        return RoleInterfaceAssembler.toResponse(role, roleService.listRoleMenus(role));
    }

    private RoleUserResponse toUserResponse(User user) {
        return RoleInterfaceAssembler.toUserResponse(
                user,
                userIdentityService.getAccountLoginName(user.getId()),
                departmentService.getById(EntityIdCodec.toDomain(user.getDepartmentId())),
                departmentService::getById);
    }

    private void validateAssignUser(RoleAssignUserRequest request) throws ApiException {
        Role roleBean = roleService.getById(EntityIdCodec.toDomain(request.getRoleId()));
        if (roleBean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getRoleId());
        }

        if (request.getUsers() == null || request.getUsers().isEmpty()) {
            throw new InvalidParameterException("users");
        }

        for (RoleUserRequest userRequest : request.getUsers()) {
            User userBean = userService.getById(EntityIdCodec.toDomain(userRequest.getId()));
            if (userBean == null) {
                throw new NullBeanException(User.BEAN_NAME, userRequest.getId());
            }
        }
    }

    private void validateMenus(List<RoleMenuRequest> requestList) throws ApiException {
        if (requestList == null || requestList.isEmpty()) {
            return;
        }
        for (RoleMenuRequest request : requestList) {
            if (request == null || StringUtils.isBlank(request.getId())) {
                throw new InvalidParameterException("menus.id");

            } else {
                Menu bean = menuService.getById(EntityIdCodec.toDomain(request.getId()));
                if (bean == null) {
                    throw new NullBeanException(MENU_NAME, request.getId());
                }
            }
        }
    }
}
