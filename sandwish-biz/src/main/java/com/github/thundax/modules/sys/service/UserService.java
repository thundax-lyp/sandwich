package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.query.UserQuery;
import java.util.List;

public interface UserService {

    User getById(EntityId id);

    List<User> listAll();

    List<User> list(UserQuery query);

    PageDTO<User> page(UserQuery query, PageDTO<User> page);

    EntityId add(User user, String loginName, String encryptedPassword, List<Long> roleIdList);

    void update(User user, String loginName, List<Long> roleIdList);

    int batchDeleteById(List<EntityId> ids);

    int updateStatus(User user);

    int batchUpdateStatus(List<User> list);

    List<Role> listUserRoles(User user);
}
