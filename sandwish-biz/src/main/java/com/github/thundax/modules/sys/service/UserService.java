package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserCredential;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.List;

public interface UserService {

    User getById(EntityId id);

    List<User> list(User user);

    List<User> list(UserQuery query);

    PageDTO<User> page(UserQuery query, PageDTO<User> page);

    void add(User user, String loginName, String encryptedPassword, List<String> roleIdList);

    default void update(User user, String loginName) {
        update(user, loginName, null);
    }

    void update(User user, String loginName, List<String> roleIdList);

    int batchDeleteById(List<EntityId> ids);

    User getByLoginName(String loginName);

    String getAccountLoginName(EntityId userId);

    UserCredential getPasswordCredential(EntityId userId);

    void updatePassword(EntityId userId, String encryptedPassword, String updateUserId);

    /**
     * 更新登录信息：lastLoginIp, lastLoginDate, loginCount
     */
    void updateLoginInfo(User user);

    int updateStatus(User user);

    int updateStatus(List<User> list);

    List<Role> listUserRoles(User user);
}
