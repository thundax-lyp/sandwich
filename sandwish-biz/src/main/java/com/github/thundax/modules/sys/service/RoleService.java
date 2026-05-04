package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.service.CrudService;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import java.util.List;

public interface RoleService extends CrudService<Role> {

    List<Role> list(RoleQuery query);

    PageDTO<Role> page(RoleQuery query, PageDTO<Role> page);

    List<Role> listEnabled();

    int updateStatus(Role role);

    int updateStatus(List<Role> list);

    void updateUserList(Role role, List<User> userList);

    List<User> listRoleUsers(Role role);

    List<Menu> listRoleMenus(Role role);
}
