package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.Role;
import java.util.List;

public interface RoleDao {

    Role getById(EntityId id);

    List<Role> listByIds(List<Long> idList);

    List<Role> list(String enableFlag);

    Page<Role> page(String enableFlag, int pageNo, int pageSize);

    Long insert(Role role);

    int update(Role role);

    int updatePriority(Role role);

    int deleteById(EntityId id);

    int updateStatus(Role role);

    List<Long> listRoleMenus(Long roleId);

    void deleteRoleMenu(Long roleId);

    void insertRoleMenu(Long roleId, List<Long> menuIdList);

    List<Long> listRoleUsers(Long roleId);

    void deleteRoleUser(Long roleId);

    void insertRoleUser(Long roleId, List<Long> userIdList);
}
