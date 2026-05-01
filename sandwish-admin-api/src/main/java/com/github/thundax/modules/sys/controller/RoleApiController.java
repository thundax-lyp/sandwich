package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.assembler.RoleInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.enums.RoleStatus;
import com.github.thundax.modules.sys.request.RoleAssignUserRequest;
import com.github.thundax.modules.sys.request.RoleIdRequest;
import com.github.thundax.modules.sys.request.RoleMenuRequest;
import com.github.thundax.modules.sys.request.RolePriorityRequest;
import com.github.thundax.modules.sys.request.RoleQueryRequest;
import com.github.thundax.modules.sys.request.RoleSaveRequest;
import com.github.thundax.modules.sys.request.RoleStatusRequest;
import com.github.thundax.modules.sys.request.RoleUserRequest;
import com.github.thundax.modules.sys.response.RoleMenuResponse;
import com.github.thundax.modules.sys.response.RoleResponse;
import com.github.thundax.modules.sys.response.RoleUserResponse;
import com.github.thundax.modules.sys.response.RoleUserTreeNodeResponse;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "02-04.系统-权限")
@SysLogger(module = {"系统", "权限"})
@RequestMapping(value = "/api/sys/role")
@RestController
public class RoleApiController extends BaseApiController {

    private static final String OFFICE_ID_PREFIX = "OFFICE_";

    private final RoleService roleService;
    private final MenuService menuService;
    private final OfficeService officeService;
    private final UserService userService;

    @Autowired
    public RoleApiController(
            RoleService roleService,
            MenuService menuService,
            OfficeService officeService,
            UserService userService,
            Validator validator) {
        super(validator);

        this.roleService = roleService;
        this.menuService = menuService;
        this.officeService = officeService;
        this.userService = userService;
    }

    @ApiOperation(value = "获取对象", notes = "sys:role:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("读取")
    @RequestMapping(value = "get", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:view')")
    public RoleResponse get(@RequestBody RoleIdRequest request) throws ApiException {
        Role bean = roleService.get(EntityIdCodec.toDomain(request.getId()));
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
    @SysLogger("列表")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:view')")
    public List<RoleResponse> list(@RequestBody RoleQueryRequest request) throws ApiException {
        validate(request);

        Role query = new Role();
        Role.Query queryCondition = new Role.Query();
        if (request.getEnable() != null) {
            queryCondition.setStatus(request.getEnable() ? RoleStatus.ENABLED : RoleStatus.DISABLED);
        }
        query.setQuery(queryCondition);

        return roleService.findList(query).stream()
                .map(role -> toResponse(role))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "添加", notes = "sys:role:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("添加")
    @RequestMapping(value = "add", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:edit')")
    public RoleResponse add(@RequestBody RoleSaveRequest request) throws ApiException {
        validate(request);
        validateMenus(request.getMenuList());

        Role entity = RoleInterfaceAssembler.toEntity(new Role(), request);
        if (entity.getId() != null) {
            Role bean = roleService.get(entity.getId());
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
    @SysLogger("更新")
    @RequestMapping(value = "update", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:edit')")
    public RoleResponse update(@RequestBody RoleSaveRequest request) throws ApiException {
        validate(request);
        validateMenus(request.getMenuList());

        Role bean = roleService.get(EntityIdCodec.toDomain(request.getId()));
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
    @SysLogger("启用")
    @RequestMapping(value = "enable", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:edit')")
    public Boolean updateStatus(@RequestBody List<RoleStatusRequest> list) throws ApiException {
        List<Role> beanList = validateList(
                list,
                vo -> roleService.get(EntityIdCodec.toDomain(vo.getId())),
                null,
                (bean, vo) ->
                        bean.setStatus(Boolean.TRUE.equals(vo.getEnable()) ? RoleStatus.ENABLED : RoleStatus.DISABLED));

        roleService.updateStatus(beanList);

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
    @SysLogger("排序")
    @RequestMapping(value = "priority", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:edit')")
    public Boolean updatePriority(@RequestBody List<RolePriorityRequest> list) throws ApiException {
        List<Role> beanList = validateList(
                list,
                vo -> roleService.get(EntityIdCodec.toDomain(vo.getId())),
                null,
                (bean, vo) -> bean.setPriority(vo.getPriority() == null ? 0 : vo.getPriority()));

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
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:edit')")
    public Boolean delete(@RequestBody List<RoleIdRequest> list) throws ApiException {
        List<Role> beanList = validateList(list, vo -> roleService.get(EntityIdCodec.toDomain(vo.getId())), null, null);

        roleService.delete(beanList);

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
    @RequestMapping(value = "menu/tree", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role')")
    public List<RoleMenuResponse> menuTree() {
        return menuService.findList(new Menu()).stream()
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
    @RequestMapping(value = "user/tree", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role')")
    public List<RoleUserTreeNodeResponse> userTree() {
        List<RoleUserTreeNodeResponse> list = new ArrayList<>();

        list.addAll(officeService.findList(new Office()).stream()
                .map(office -> RoleInterfaceAssembler.toOfficeTreeNode(OFFICE_ID_PREFIX + office.getId(), office))
                .collect(Collectors.toList()));

        list.addAll(userService.findList(new User()).stream()
                .map(user -> RoleInterfaceAssembler.toUserTreeNode(
                        OFFICE_ID_PREFIX,
                        user,
                        officeService.get(EntityIdCodec.toDomain(user.getOfficeId())),
                        officeService::get))
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
    @RequestMapping(value = "user/list", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:view')")
    public List<RoleUserResponse> userList(@RequestBody RoleIdRequest request) throws ApiException {
        Role bean = roleService.get(EntityIdCodec.toDomain(request.getId()));
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getId());
        }

        return roleService.findRoleUser(bean).stream()
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
    @SysLogger("授权")
    @RequestMapping(value = "user/assign", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('sys:role:edit')")
    public Boolean assignUser(@RequestBody RoleAssignUserRequest request) throws ApiException {
        validateAssignUser(request);

        Role roleBean = roleService.get(EntityIdCodec.toDomain(request.getRoleId()));
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
        return RoleInterfaceAssembler.toResponse(role, roleService.findRoleMenu(role));
    }

    private RoleUserResponse toUserResponse(User user) {
        return RoleInterfaceAssembler.toUserResponse(
                user, officeService.get(EntityIdCodec.toDomain(user.getOfficeId())), officeService::get);
    }

    private void validateAssignUser(RoleAssignUserRequest request) throws ApiException {
        validate(request);

        Role roleBean = roleService.get(EntityIdCodec.toDomain(request.getRoleId()));
        if (roleBean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getRoleId());
        }

        if (request.getUsers() == null || request.getUsers().isEmpty()) {
            throw new InvalidParameterException("users");
        }

        for (RoleUserRequest userRequest : request.getUsers()) {
            User userBean = userService.get(EntityIdCodec.toDomain(userRequest.getId()));
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
                Menu bean = menuService.get(EntityIdCodec.toDomain(request.getId()));
                if (bean == null) {
                    throw new NullBeanException(Menu.BEAN_NAME, request.getId());
                }
            }
        }
    }
}
