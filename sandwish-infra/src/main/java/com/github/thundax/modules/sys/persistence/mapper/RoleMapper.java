package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.RoleDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 MyBatis Mapper。
 */
@MyBatisDao
public interface RoleMapper {

    RoleDO get(RoleDO role);

    List<RoleDO> getMany(@Param("idList") List<String> idList);

    List<RoleDO> findList(RoleDO role);

    int insert(RoleDO role);

    int update(RoleDO role);

    int updatePriority(RoleDO role);

    int updateStatus(RoleDO role);

    int updateDelFlag(RoleDO role);

    int delete(RoleDO role);

    /**
     * 启用/禁用。
     *
     * @param role 权限
     * @return 影响记录数
     */
    int updateEnableFlag(RoleDO role);

    /**
     * 获取菜单 id 列表。
     *
     * @param role 权限
     * @return 菜单 id 列表
     */
    List<String> findRoleMenu(RoleDO role);

    /**
     * 删除菜单列表。
     *
     * @param role 权限
     */
    void deleteRoleMenu(RoleDO role);

    /**
     * 写入菜单列表。
     *
     * @param role 权限
     */
    void insertRoleMenu(RoleDO role);

    /**
     * 获取用户 id 列表。
     *
     * @param role 权限
     * @return 用户 id 列表
     */
    List<String> findRoleUser(RoleDO role);

    /**
     * 删除用户列表。
     *
     * @param role 权限
     */
    void deleteRoleUser(RoleDO role);

    /**
     * 写入用户列表。
     *
     * @param role       权限
     * @param userIdList 用户 id 列表
     */
    void insertRoleUser(@Param("role") RoleDO role, @Param("userIdList") List<String> userIdList);
}
