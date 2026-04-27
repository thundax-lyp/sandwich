package com.github.thundax.modules.sys.service;

import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import java.util.List;

/** @author wdit */
public interface UserService extends CrudService<User> {

    /**
     * 根据loginName获取
     *
     * @param loginName 登录名
     * @return 用户
     */
    User getByLoginName(String loginName);

    /**
     * 根据ssoLoginName获取
     *
     * @param ssoLoginName 登录名
     * @return 用户
     */
    User getBySsoLoginName(String ssoLoginName);

    /**
     * 更新密码
     *
     * @param user 用户
     */
    void updatePassword(User user);

    /**
     * 更新登录信息：lastLoginIp, lastLoginDate, loginCount
     *
     * @param user 用户
     */
    void updateLoginInfo(User user);

    /**
     * 启用/禁用
     *
     * @param user 用户
     * @return 影响记录数
     */
    int updateEnableFlag(User user);

    /**
     * 启用/禁用
     *
     * @param list 列表
     * @return 影响记录数
     */
    int updateEnableFlag(List<User> list);

    /**
     * 获取用户权限列表
     *
     * @param user 用户
     * @return 权限列表
     */
    List<Role> findUserRole(User user);
}
