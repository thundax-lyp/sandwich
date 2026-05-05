package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.entity.User;
import java.util.List;

public interface UserDao {

    User getById(EntityId id);

    List<User> listByIds(List<String> idList);

    List<User> list(String departmentId, String loginName, String name, String enableFlag, String superFlag);

    Page<User> page(
            String departmentId,
            String loginName,
            String name,
            String enableFlag,
            String superFlag,
            int pageNo,
            int pageSize);

    String insert(User user);

    int update(User user);

    int updatePriority(User user);

    int deleteById(EntityId id);

    int updateStatus(User user);

    List<String> listUserRoles(String userId);

    void deleteUserRole(String userId);

    void insertUserRole(String userId, List<String> roleIdList);
}
