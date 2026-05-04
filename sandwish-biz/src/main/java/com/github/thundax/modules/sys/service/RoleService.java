package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import java.util.List;

public interface RoleService {

    Role getById(EntityId id);

    List<Role> list(RoleQuery query);

    PageDTO<Role> page(RoleQuery query, PageDTO<Role> page);

    void add(Role role);

    void update(Role role);

    int deleteById(EntityId id);

    int batchDeleteById(List<EntityId> ids);

    int updatePriority(List<Role> list);

    List<Role> listEnabled();

    int updateStatus(Role role);

    int updateStatus(List<Role> list);

    void updateUserList(Role role, List<User> userList);

    List<User> listRoleUsers(Role role);

    List<Menu> listRoleMenus(Role role);
}
