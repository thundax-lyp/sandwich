package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.entity.Department;
import java.util.List;

public interface DepartmentDao {

    Department getById(EntityId id);

    List<Department> listByIds(List<String> idList);

    List<Department> list(String parentId, String name, String remarks);

    Page<Department> page(String parentId, String name, String remarks, int pageNo, int pageSize);

    String insert(Department department);

    int update(Department department);

    int updatePriority(Department department);

    int deleteById(EntityId id);

    void moveTreeNode(String fromId, String toId, TreeNodeMoveType moveType);

    boolean isChildOf(String childId, String parentId);
}
