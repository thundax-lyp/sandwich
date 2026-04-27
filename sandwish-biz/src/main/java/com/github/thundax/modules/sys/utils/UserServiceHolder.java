package com.github.thundax.modules.sys.utils;

import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;
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
                    MenuServiceHolder.getService().findList(new Menu()).stream()
                            .map(menu -> menu.getId())
                            .collect(Collectors.toList());

        } else {
            List<Role> roleList =
                    user.getRoleList().stream()
                            .map(role -> RoleServiceHolder.get(role.getId()))
                            .collect(Collectors.toList());
            boolean isAdmin = user.isAdmin();
            if (!isAdmin) {
                // 如果在admin组里，也认为是admin
                isAdmin = roleList.stream().anyMatch(role -> role.isAdmin());
            }

            if (isAdmin) {
                // 一般管理员可以看到比自己级别低的菜单
                menuIdList =
                        MenuServiceHolder.getService().findList(user.getRanks()).stream()
                                .map(menu -> menu.getId())
                                .collect(Collectors.toList());

            } else {
                // 普通用户只能看到role中指定，且级别比自己低的菜单
                Set<String> menuIds = Sets.newHashSet();
                for (Role role : roleList) {
                    menuIds.addAll(
                            role.getMenuList().stream()
                                    .map(menu -> menu.getId())
                                    .collect(Collectors.toList()));
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
