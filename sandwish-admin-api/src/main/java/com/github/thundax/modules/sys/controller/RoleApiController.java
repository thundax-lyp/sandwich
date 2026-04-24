package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InsertBeanExistException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.NullBeanException;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.vo.UserVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.auth.security.annotation.Logical;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.sys.api.RoleServiceApi;
import com.github.thundax.modules.sys.api.query.AssignUserQueryParam;
import com.github.thundax.modules.sys.api.query.RoleQueryParam;
import com.github.thundax.modules.sys.api.vo.MenuVo;
import com.github.thundax.modules.sys.api.vo.OfficeVo;
import com.github.thundax.modules.sys.api.vo.RoleVo;
import com.github.thundax.modules.sys.api.vo.UserTreeNodeVo;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
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

    @Autowired
    public RoleApiController(RoleService roleService,
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


    @Override
    @RequiresPermissions("sys:role:view")
    public RoleVo get(@RequestBody RoleVo vo) throws ApiException {
        Role bean = roleService.get(vo.getId());
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, vo.getId());
        }
        return entityToVo(bean);
    }

    @Override
    @RequiresPermissions("sys:role:view")
    public List<RoleVo> list(@RequestBody RoleQueryParam queryParam) throws ApiException {
        validate(queryParam);

        Role query = new Role();
        if (queryParam.getEnable() != null) {
            query.setQueryProp(Role.Query.PROP_ENABLE_FLAG, queryParam.getEnable() ? Global.ENABLE : Global.DISABLE);
        }

        return ListUtils.map(roleService.findList(query), this::entityToVo);
    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public RoleVo add(@RequestBody RoleVo vo) throws ApiException {
        validate(vo);
        validateMenus(vo.getMenuList());

        Role entity = voToEntity(new Role(), vo);
        if (StringUtils.isNotEmpty(entity.getId())) {
            Role bean = roleService.get(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(Role.BEAN_NAME, entity.getId());
            }
            entity.setIsNewRecord(true);
        }

        roleService.save(entity);

        return entityToVo(entity);
    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public RoleVo update(@RequestBody RoleVo vo) throws ApiException {
        validate(vo);
        validateMenus(vo.getMenuList());

        Role bean = roleService.get(vo.getId());
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, vo.getId());
        }

        Role entity = voToEntity(bean, vo);

        roleService.save(entity);

        return entityToVo(entity);

    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public Boolean updateEnableFlag(@RequestBody List<RoleVo> list) throws ApiException {
        List<Role> beanList = validateList(list,
                vo -> roleService.get(vo.getId()),
                null,
                (bean, vo) -> bean.setEnableFlag(Boolean.TRUE.equals(vo.getEnable()) ? Global.ENABLE : Global.DISABLE));

        roleService.updateEnableFlag(beanList);

        return true;
    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public Boolean updatePriority(@RequestBody List<RoleVo> list) throws ApiException {
        List<Role> beanList = validateList(list,
                vo -> roleService.get(vo.getId()),
                null,
                (bean, vo) -> bean.setPriority(vo.getPriority()));

        roleService.updatePriority(beanList);

        return true;
    }


    @Override
    @RequiresPermissions("sys:role:edit")
    public Boolean delete(@RequestBody List<RoleVo> list) throws ApiException {
        List<Role> beanList = validateList(list, vo -> roleService.get(vo.getId()), null, null);

        roleService.delete(beanList);

        return true;
    }

    @Override
    @RequiresPermissions(value = {"sys:role:view", "sys:role:edit"}, logical = Logical.OR)
    public List<MenuVo> menuTree() {
        return ListUtils.map(menuService.findList(new Menu()), this::entityToVo);
    }

    @Override
    @RequiresPermissions(value = {"sys:role:view", "sys:role:edit"}, logical = Logical.OR)
    public List<UserTreeNodeVo> userTree() {
        List<UserTreeNodeVo> list = ListUtils.newArrayList();

        list.addAll(ListUtils.map(officeService.findList(new Office()), office -> {
            UserTreeNodeVo vo = new UserTreeNodeVo(OFFICE_ID_PREFIX + office.getId());
            if (StringUtils.isNotBlank(office.getParentId())) {
                vo.setParentId(OFFICE_ID_PREFIX + office.getParentId());
            }
            vo.setName(office.getName());
            return vo;
        }));

        list.addAll(ListUtils.map(userService.findList(new User()), user -> {
            UserTreeNodeVo vo = new UserTreeNodeVo(user.getId());
            vo.setParentId(OFFICE_ID_PREFIX + user.getOfficeId());
            vo.setName(user.getName());
            vo.setUser(entityToVo(user));
            return vo;
        }));

        return list;
    }

    @Override
    @RequiresPermissions("sys:role:view")
    public List<UserVo> userList(@RequestBody RoleVo vo) throws ApiException {
        Role bean = roleService.get(vo.getId());
        if (bean == null) {
            throw new NullBeanException(Role.BEAN_NAME, vo.getId());
        }

        return ListUtils.map(roleService.findRoleUser(bean),
                user -> entityToVo(UserServiceHolder.get(user.getId())));
    }

    @Override
    @RequiresPermissions("sys:role:edit")
    public Boolean assignUser(@RequestBody AssignUserQueryParam queryParam) throws ApiException {
        validateAssignUser(queryParam);

        Role roleBean = RoleServiceHolder.get(queryParam.getRoleId());
        Assert.notNull(roleBean, "role can not be null");

        roleService.updateUserList(roleBean,
                ListUtils.map(queryParam.getUsers(), vo -> new User(vo.getId())));

        return true;
    }


    private void validateAssignUser(AssignUserQueryParam queryParam) throws ApiException {
        validate(queryParam);

        Role roleBean = RoleServiceHolder.get(queryParam.getRoleId());
        if (roleBean == null) {
            throw new NullBeanException(Role.BEAN_NAME, queryParam.getRoleId());
        }

        if (ListUtils.isEmpty(queryParam.getUsers())) {
            throw new InvalidParameterException("users");
        }

        for (UserVo userVo : queryParam.getUsers()) {
            User userBean = UserServiceHolder.get(userVo.getId());
            if (userBean == null) {
                throw new NullBeanException(User.BEAN_NAME, userVo.getId());
            }
        }
    }


    private void validateMenus(List<MenuVo> voList) throws ApiException {
        if (ListUtils.isEmpty(voList)) {
            return;
        }
        for (MenuVo vo : voList) {
            if (vo == null || StringUtils.isBlank(vo.getId())) {
                throw new InvalidParameterException("menus.id");

            } else {
                Menu bean = MenuServiceHolder.get(vo.getId());
                if (bean == null) {
                    throw new NullBeanException(Menu.BEAN_NAME, vo.getId());
                }
            }
        }
    }


    @NonNull
    private RoleVo entityToVo(Role entity) {
        if (entity == null) {
            return new RoleVo();
        }

        RoleVo vo = baseEntityToVo(new RoleVo(), entity);

        vo.setName(entity.getName());
        vo.setAdmin(entity.isAdmin());
        vo.setEnable(entity.isEnable());

        vo.setMenuList(ListUtils.map(entity.getMenuList(), this::entityToVo));

        return vo;
    }

    @NonNull
    private MenuVo entityToVo(Menu entity) {
        if (entity == null) {
            return new MenuVo();
        }

        MenuVo vo = new MenuVo(entity.getId());
        if (StringUtils.isNotBlank(entity.getParentId())) {
            vo.setParentId(entity.getParentId());
        }
        vo.setName(entity.getName());
        vo.setPerms(entity.getPerms());
        return vo;
    }

    @NonNull
    private OfficeVo entityToVo(Office entity) {
        if (entity == null) {
            return new OfficeVo();
        }

        OfficeVo vo = new OfficeVo(entity.getId());
        vo.setName(entity.getName());
        vo.setNamePath(entity.getNamePath());
        return vo;
    }

    @NonNull
    private UserVo entityToVo(User entity) {
        if (entity == null) {
            return new UserVo();
        }

        UserVo vo = new UserVo(entity.getId());
        vo.setName(entity.getName());
        vo.setLoginName(entity.getLoginName());

        vo.setOffice(entityToVo(entity.getOffice()));

        return vo;
    }

    @NonNull
    private Role voToEntity(@NonNull Role entity, @NonNull RoleVo vo) {
        baseVoToEntity(entity, vo);

        entity.setName(vo.getName());
        entity.setAdminFlag(Boolean.TRUE.equals(vo.getAdmin()) ? Global.YES : Global.NO);
        entity.setEnableFlag(Boolean.TRUE.equals(vo.getEnable()) ? Global.ENABLE : Global.DISABLE);

        entity.setMenuIdList(ListUtils.map(vo.getMenuList(), MenuVo::getId));

        return entity;
    }


}
