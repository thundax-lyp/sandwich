package com.github.thundax.modules.sys.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;

import java.util.List;

/**
 * 用户DAO接口
 *
 * @author wdit
 */
public interface UserDao extends CrudDao<User> {

    /**
     * 根据loginName获取
     *
     * @param loginName 登录名
     * @return 用户
     */
    User getByLoginName(String loginName);

    /**
     * 根据loginName获取
     *
     * @param ssoLoginName 登录名
     * @return 用户
     */
    User getBySsoLoginName(String ssoLoginName);

    /**
     * 更新登录信息：lastLoginIp, lastLoginDate, loginCount
     *
     * @param user 用户
     */
    void updateLoginInfo(User user);

    /**
     * 启用/禁用：enableFlag, updateDate, updateBy
     *
     * @param user 用户
     * @return 影响记录数
     */
    int updateEnableFlag(User user);

    /**
     * 更新密码, loginPass, updateDate, updateBy
     *
     * @param user 用户
     */
    void updateLoginPass(User user);

    /**
     * 获取用户权限列表
     *
     * @param user 用户
     * @return 权限列表
     */
    List<Role> findUserRole(User user);

    /**
     * 删除用户权限列表
     *
     * @param user 用户
     */
    void deleteUserRole(User user);

    /**
     * 写入用户权限列表
     *
     * @param user 用户
     */
    void insertUserRole(User user);

}
