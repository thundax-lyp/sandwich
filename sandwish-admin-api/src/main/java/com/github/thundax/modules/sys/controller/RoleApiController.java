package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.auth.security.annotation.Logical;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.sys.assembler.RoleInterfaceAssembler;
import com.github.thundax.modules.sys.api.RoleServiceApi;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
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
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;
import java.util.List;

/**
 * @author thundax
 */
@RestController
public class RoleApiController extends BaseApiController implements RoleServiceApi {

    private static final String OFFICE_ID_PREFIX = "OFFICE_";

    private final RoleService roleService;
    private final MenuService menuService;
    private final OfficeService officeService;
    private final UserService userService;
    private final RoleInterfaceAssembler roleInterfaceAssembler;

    @Autowired
    public RoleApiController(RoleService roleService,
                             MenuService menuService,
                             OfficeService officeService,
                             UserService userService,
                             Validator validator,
                             RoleInterfaceAssembler roleInterfaceAssembler) {
        super(validator);

        this.roleService = roleService;
        this.menuService = menuService;
        this.officeService = officeService;
        this.userService = userService;
        this.roleInterfaceAssembler = roleInterfaceAssembler;
    }


    @Override
    @RequiresPermissions("sys:role:view")
    public RoleResponse get(@RequestBody RoleIdRequest request) throws ApiException {
        Role bean = roleService.get(request.getId());
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getId());
        }
        return roleInterfaceAssembler.toResponse(bean);
    }

    @Override
    @RequiresPermissions("sys:role:view")
    public List<RoleResponse> list(@RequestBody RoleQueryRequest request) throws ApiException {
        validate(request);

        Role query = new Role();
        if (request.getEnable() != null) {
            query.setQueryProp(Role.Query.PROP_ENABLE_FLAG, request.getEnable() ? Global.ENABLE : Global.DISABLE);
        }

        return ListUtils.map(roleService.findList(query), roleInterfaceAssembler::toResponse);
    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public RoleResponse add(@RequestBody RoleSaveRequest request) throws ApiException {
        validate(request);
        validateMenus(request.getMenuList());

        Role entity = roleInterfaceAssembler.toEntity(new Role(), request);
        if (StringUtils.isNotEmpty(entity.getId())) {
            Role bean = roleService.get(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Role.BEAN_NAME, entity.getId());
            }
            entity.setIsNewRecord(true);
        }

        roleService.save(entity);

        return roleInterfaceAssembler.toResponse(entity);
    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public RoleResponse update(@RequestBody RoleSaveRequest request) throws ApiException {
        validate(request);
        validateMenus(request.getMenuList());

        Role bean = roleService.get(request.getId());
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getId());
        }

        Role entity = roleInterfaceAssembler.toEntity(bean, request);

        roleService.save(entity);

        return roleInterfaceAssembler.toResponse(entity);

    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public Boolean updateEnableFlag(@RequestBody List<RoleStatusRequest> list) throws ApiException {
        List<Role> beanList = validateList(list,
                vo -> roleService.get(vo.getId()),
                null,
                (bean, vo) -> bean.setEnableFlag(Boolean.TRUE.equals(vo.getEnable()) ? Global.ENABLE : Global.DISABLE));

        roleService.updateEnableFlag(beanList);

        return true;
    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public Boolean updatePriority(@RequestBody List<RolePriorityRequest> list) throws ApiException {
        List<Role> beanList = validateList(list,
                vo -> roleService.get(vo.getId()),
                null,
                (bean, vo) -> bean.setPriority(vo.getPriority()));

        roleService.updatePriority(beanList);

        return true;
    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public Boolean delete(@RequestBody List<RoleIdRequest> list) throws ApiException {
        List<Role> beanList = validateList(list, vo -> roleService.get(vo.getId()), null, null);

        roleService.delete(beanList);

        return true;
    }

    @Override
    @RequiresPermissions(value = {"sys:role:view", "sys:role:edit"}, logical = Logical.OR)
    public List<RoleMenuResponse> menuTree() {
        return ListUtils.map(menuService.findList(new Menu()), roleInterfaceAssembler::toMenuResponse);
    }

    @Override
    @RequiresPermissions(value = {"sys:role:view", "sys:role:edit"}, logical = Logical.OR)
    public List<RoleUserTreeNodeResponse> userTree() {
        List<RoleUserTreeNodeResponse> list = ListUtils.newArrayList();

        list.addAll(ListUtils.map(officeService.findList(new Office()), office -> {
            return roleInterfaceAssembler.toOfficeTreeNode(OFFICE_ID_PREFIX + office.getId(), office);
        }));

        list.addAll(ListUtils.map(userService.findList(new User()), user -> {
            return roleInterfaceAssembler.toUserTreeNode(OFFICE_ID_PREFIX, user);
        }));

        return list;
    }

    @Override
    @RequiresPermissions("sys:role:view")
    public List<RoleUserResponse> userList(@RequestBody RoleIdRequest request) throws ApiException {
        Role bean = roleService.get(request.getId());
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getId());
        }

        return ListUtils.map(roleService.findRoleUser(bean),
                user -> roleInterfaceAssembler.toUserResponse(UserServiceHolder.get(user.getId())));
    }

    @Override
    @RequiresPermissions("sys:role:edit")
    public Boolean assignUser(@RequestBody RoleAssignUserRequest request) throws ApiException {
        validateAssignUser(request);

        Role roleBean = RoleServiceHolder.get(request.getRoleId());
        Assert.notNull(roleBean, "role can not be null");

        roleService.updateUserList(roleBean,
                ListUtils.map(request.getUsers(), vo -> new User(vo.getId())));

        return true;
    }


    private void validateAssignUser(RoleAssignUserRequest request) throws ApiException {
        validate(request);

        Role roleBean = RoleServiceHolder.get(request.getRoleId());
        if (roleBean == null) {
            throw new NullBeanException(Role.BEAN_NAME, request.getRoleId());
        }

        if (ListUtils.isEmpty(request.getUsers())) {
            throw new InvalidParameterException("users");
        }

        for (RoleUserRequest userRequest : request.getUsers()) {
            User userBean = UserServiceHolder.get(userRequest.getId());
            if (userBean == null) {
                throw new NullBeanException(User.BEAN_NAME, userRequest.getId());
            }
        }
    }


    private void validateMenus(List<RoleMenuRequest> requestList) throws ApiException {
        if (ListUtils.isEmpty(requestList)) {
            return;
        }
        for (RoleMenuRequest request : requestList) {
            if (request == null || StringUtils.isBlank(request.getId())) {
                throw new InvalidParameterException("menus.id");

            } else {
                Menu bean = MenuServiceHolder.get(request.getId());
                if (bean == null) {
                    throw new NullBeanException(Menu.BEAN_NAME, request.getId());
                }
            }
        }
    }
}
