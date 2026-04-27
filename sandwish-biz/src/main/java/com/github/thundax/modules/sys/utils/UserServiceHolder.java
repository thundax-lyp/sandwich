package com.github.thundax.modules.sys.utils;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/** @author wdit */
@Service
@Lazy(false)
public class UserServiceHolder {

    private static UserService service;

    private static final PooledThreadLocal<Map<String, User>> ID_OBJECT_HOLDER =
            new PooledThreadLocal<>();

    @Autowired
    public UserServiceHolder(UserService targetService) {
        service = targetService;
    }

    public static UserService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(UserService.class);
        }
        return service;
    }

    public static User get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return ID_OBJECT_HOLDER
                .computeIfAbsent(HashMap::new)
                .computeIfAbsent(id, (key) -> getService().get(id));
    }

    /**
     * 获取用户菜单
     *
     * @param user 用户
     * @return List<Menu>
     */
    public static List<Menu> findMenuList(User user) {
        List<String> menuIdList;

        if (user.isSuper()) {
            // 超级管理员可以看到全部菜单
            menuIdList =
                    ListUtils.map(MenuServiceHolder.getService().findList(new Menu()), Menu::getId);

        } else {
            List<Role> roleList =
                    ListUtils.map(user.getRoleList(), role -> RoleServiceHolder.get(role.getId()));
            boolean isAdmin = user.isAdmin();
            if (!isAdmin) {
                // 如果在admin组里，也认为是admin
                isAdmin = ListUtils.contains(roleList, Role::isAdmin);
            }

            if (isAdmin) {
                // 一般管理员可以看到比自己级别低的菜单
                menuIdList =
                        ListUtils.map(
                                MenuServiceHolder.getService().findList(user.getRanks()),
                                Menu::getId);

            } else {
                // 普通用户只能看到role中指定，且级别比自己低的菜单
                Set<String> menuIds = Sets.newHashSet();
                for (Role role : roleList) {
                    menuIds.addAll(ListUtils.map(role.getMenuList(), Menu::getId));
                }

                menuIds.removeIf(
                        menuId -> {
                            Menu menuBean = MenuServiceHolder.get(menuId);
                            return menuBean == null || menuBean.getRanks() > user.getRanks();
                        });

                menuIdList = new ArrayList<>(menuIds);
            }
        }

        List<Menu> menuList = MenuServiceHolder.getService().getMany(menuIdList);

        menuList.sort(Menu::compareTo);

        return menuList;
    }
}
