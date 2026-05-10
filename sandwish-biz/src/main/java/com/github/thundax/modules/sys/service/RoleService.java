package com.github.thundax.modules.sys.service;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import com.github.thundax.modules.sys.service.command.AssignRoleUsersCommand;
import com.github.thundax.modules.sys.service.command.ChangeRoleInfoCommand;
import com.github.thundax.modules.sys.service.command.ChangeRoleStatusCommand;
import com.github.thundax.modules.sys.service.command.CreateRoleCommand;
import com.github.thundax.modules.sys.service.command.DeleteRoleCommand;
import com.github.thundax.modules.sys.service.query.RoleQuery;
import java.util.List;

public interface RoleService {

    Role get(RoleId id);

    List<Role> list(RoleQuery query);

    PageResult<Role> page(RoleQuery query, PageQuery page);

    RoleId create(CreateRoleCommand command);

    void changeInfo(ChangeRoleInfoCommand command);

    int remove(DeleteRoleCommand command);

    void sort(List<RoleId> orderedIds, SortDirection sortDirection) throws ApiException;

    int changeStatus(ChangeRoleStatusCommand command);

    void assignUsers(AssignRoleUsersCommand command);

    List<User> listRoleUsers(RoleQuery query);

    List<Menu> listRoleMenus(RoleQuery query);
}
