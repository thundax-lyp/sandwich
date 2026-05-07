package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.entity.Department;
import java.util.List;

public interface DepartmentDao {

    Department getById(EntityId id);

    List<Department> listByIds(List<Long> idList);

    List<Department> list(Long parentId, String name, String remarks);

    Page<Department> page(Long parentId, String name, String remarks, int pageNo, int pageSize);

    EntityId insert(Department department);

    int update(Department department);

    int updatePriority(Department department);

    int deleteById(EntityId id);

    void moveTreeNode(Long fromId, Long toId, TreeNodeMoveType moveType);

    boolean isChildOf(Long childId, Long parentId);
}
