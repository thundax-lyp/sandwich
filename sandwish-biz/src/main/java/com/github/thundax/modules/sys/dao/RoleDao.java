package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.Role;
import java.util.List;

public interface RoleDao {

    Role getById(EntityId id);

    List<Role> batchGetByIds(List<String> idList);

    List<Role> list(String enableFlag);

    Page<Role> page(String enableFlag, int pageNo, int pageSize);

    String insert(Role role);

    int update(Role role);

    int updatePriority(Role role);

    int deleteById(EntityId id);

    /**
     * 启用/禁用
     *
     * @param role 权限
     * @return 影响记录数
     */
    int updateStatus(Role role);

    List<String> listRoleMenus(String roleId);

    void deleteRoleMenu(String roleId);

    void insertRoleMenu(String roleId, List<String> menuIdList);

    List<String> listRoleUsers(String roleId);

    void deleteRoleUser(String roleId);

    void insertRoleUser(String roleId, List<String> userIdList);
}
