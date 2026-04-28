package com.github.thundax.modules.sys.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import java.util.List;

public interface RoleService extends CrudService<Role> {

    /**
     * 获取可用列表
     *
     * @return 可用列表
     */
    List<Role> findValidList();

    /**
     * 启用/禁用
     *
     * @param role 权限
     * @return 影响记录数
     */
    int updateEnableFlag(Role role);

    /**
     * 启用/禁用
     *
     * @param list 列表
     * @return 影响记录数
     */
    int updateEnableFlag(List<Role> list);

    /**
     * 更新用户列表
     *
     * @param role 权限
     * @param userList 用户列表
     */
    void updateUserList(Role role, List<User> userList);

    /**
     * 获取用户列表
     *
     * @param role 权限
     * @return 用户列表
     */
    List<User> findRoleUser(Role role);

    /**
     * 获取菜单列表
     *
     * @param role 权限
     * @return 菜单列表
     */
    List<Menu> findRoleMenu(Role role);
}
