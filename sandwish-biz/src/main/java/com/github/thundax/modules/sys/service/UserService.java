package com.github.thundax.modules.sys.service;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.List;

public interface UserService extends CrudService<User> {

    List<User> list(UserQuery query);

    Page<User> page(UserQuery query, Page<User> page);

    User getByLoginName(String loginName);

    User getBySsoLoginName(String ssoLoginName);

    void updatePassword(User user);

    /**
     * 更新登录信息：lastLoginIp, lastLoginDate, loginCount
     */
    void updateLoginInfo(User user);

    int updateStatus(User user);

    int updateStatus(List<User> list);

    List<Role> listUserRoles(User user);
}
