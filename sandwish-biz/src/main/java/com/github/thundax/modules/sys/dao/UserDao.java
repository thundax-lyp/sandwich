package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.User;
import java.util.List;

public interface UserDao {

    User getById(EntityId id);

    List<User> batchGetByIds(List<String> idList);

    List<User> list(String officeId, String loginName, String name, String enableFlag, String superFlag);

    Page<User> page(
            String officeId,
            String loginName,
            String name,
            String enableFlag,
            String superFlag,
            int pageNo,
            int pageSize);

    String insert(User user);

    int update(User user);

    int updatePriority(User user);

    int deleteById(EntityId id);

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
     * 更新状态
     *
     * @param user 用户
     * @return 影响记录数
     */
    int updateStatus(User user);

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
    List<String> listUserRoles(String userId);

    /**
     * 删除用户权限列表
     *
     * @param user 用户
     */
    void deleteUserRole(String userId);

    /**
     * 写入用户权限列表
     *
     * @param user 用户
     */
    void insertUserRole(String userId, List<String> roleIdList);
}
