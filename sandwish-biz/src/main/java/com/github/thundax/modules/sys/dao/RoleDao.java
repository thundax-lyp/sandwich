package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.valueobject.RoleId;
import java.util.List;

public interface RoleDao {

    Role getById(RoleId id);

    List<Role> listByIds(List<Long> idList);

    List<Role> list(String status);

    int maxPriorityByScope(String status);

    List<Role> listByScope(String status, SortDirection sortDirection);

    Page<Role> page(String status, int pageNo, int pageSize);

    RoleId insert(Role role);

    int update(Role role);

    int updatePriority(Role role);

    int deleteById(RoleId id);

    int updateStatus(Role role);

    List<Long> listRoleMenus(Long roleId);

    void deleteRoleMenu(Long roleId);

    void insertRoleMenu(Long roleId, List<Long> menuIdList);

    List<Long> listRoleUsers(Long roleId);

    void deleteRoleUser(Long roleId);

    void insertRoleUser(Long roleId, List<Long> userIdList);
}
