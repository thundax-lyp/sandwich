package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** @author wdit */
public interface RoleDao extends CrudDao<Role> {

    /**
     * 启用/禁用
     *
     * @param role 权限
     * @return 影响记录数
     */
    int updateEnableFlag(Role role);

    /**
     * 获取菜单列表
     *
     * @param role 权限
     * @return 菜单列表
     */
    List<Menu> findRoleMenu(Role role);

    /**
     * 删除菜单列表
     *
     * @param role 权限
     */
    void deleteRoleMenu(Role role);

    /**
     * 写入菜单列表
     *
     * @param role 权限
     */
    void insertRoleMenu(Role role);

    /**
     * 获取用户列表
     *
     * @param role 权限
     * @return 用户列表
     */
    List<User> findRoleUser(Role role);

    /**
     * 删除用户列表
     *
     * @param role 权限
     */
    void deleteRoleUser(Role role);

    /**
     * 写入用户列表
     *
     * @param role 权限
     * @param userList 用户列表
     */
    void insertRoleUser(@Param("role") Role role, @Param("userList") List<User> userList);
}
