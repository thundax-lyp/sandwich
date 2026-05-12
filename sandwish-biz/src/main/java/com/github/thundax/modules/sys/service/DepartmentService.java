package com.github.thundax.modules.sys.service;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import com.github.thundax.modules.sys.service.command.ChangeDepartmentInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateDepartmentCommand;
import com.github.thundax.modules.sys.service.command.MoveDepartmentCommand;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import java.util.List;

public interface DepartmentService {

    Department get(DepartmentId id);

    List<Department> list(DepartmentQuery query);

    PageResult<Department> page(DepartmentQuery query, PageQuery page);

    DepartmentId create(CreateDepartmentCommand command);

    void changeInfo(ChangeDepartmentInfoCommand command);

    int remove(DepartmentId id);

    void move(MoveDepartmentCommand command);

    boolean existsChildRelation(DepartmentQuery query);
}
