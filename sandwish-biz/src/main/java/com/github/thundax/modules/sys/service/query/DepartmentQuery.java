package com.github.thundax.modules.sys.service.query;

import com.github.thundax.common.id.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentQuery {
    private EntityId id;
    private EntityId childId;
    private EntityId ancestorId;
    private EntityId parentId;
    private String name;
    private String remarks;
}
