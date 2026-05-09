package com.github.thundax.modules.sys.service.query;

import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentQuery {
    private DepartmentId childId;
    private DepartmentId ancestorId;
    private DepartmentId parentId;
    private String name;
    private String remarks;
}
