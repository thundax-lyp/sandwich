package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.entity.Role;
import java.util.List;

public interface RoleDao {

    Role get(String id);

    List<Role> getMany(List<String> idList);

    List<Role> findList(String enableFlag);

    Page<Role> findPage(String enableFlag, int pageNo, int pageSize);

    int insert(Role role);

    int update(Role role);

    int updatePriority(Role role);

    int updateDelFlag(Role role);

    int delete(String id);

    /**
     * 启用/禁用
     *
     * @param role 权限
     * @return 影响记录数
     */
    int updateEnableFlag(Role role);

    List<String> findRoleMenu(String roleId);

    void deleteRoleMenu(String roleId);

    void insertRoleMenu(String roleId, List<String> menuIdList);

    List<String> findRoleUser(String roleId);

    void deleteRoleUser(String roleId);

    void insertRoleUser(String roleId, List<String> userIdList);
}
