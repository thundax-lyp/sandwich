package com.github.thundax.modules.sys.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.service.command.ChangeDepartmentInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateDepartmentCommand;
import com.github.thundax.modules.sys.service.command.DeleteDepartmentCommand;
import com.github.thundax.modules.sys.service.command.MoveDepartmentCommand;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import java.util.List;

public interface DepartmentService {

    Department get(DepartmentQuery query);

    List<Department> list(DepartmentQuery query);

    PageResult<Department> page(DepartmentQuery query, PageQuery page);

    EntityId create(CreateDepartmentCommand command);

    void changeInfo(ChangeDepartmentInfoCommand command);

    int remove(DeleteDepartmentCommand command);

    void move(MoveDepartmentCommand command);

    boolean existsChildRelation(DepartmentQuery query);
}
