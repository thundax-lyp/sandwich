package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.List;

public interface UserService {

    User getById(EntityId id);

    List<User> list(User user);

    List<User> list(UserQuery query);

    PageDTO<User> page(UserQuery query, PageDTO<User> page);

    void add(User user);

    void update(User user);

    int batchDeleteById(List<EntityId> ids);

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
