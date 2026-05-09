package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import java.util.List;

public interface DepartmentService {

    Department getById(EntityId id);

    List<Department> listAll();

    List<Department> list(DepartmentQuery query);

    PageResult<Department> page(DepartmentQuery query, PageQuery page);

    EntityId add(Department department);

    void update(Department department);

    int batchDeleteById(List<EntityId> ids);

    void moveTreeNode(Department fromBean, Department toBean, TreeNodeMoveType moveType);

    boolean isChildOf(Department child, Department parent);
}
