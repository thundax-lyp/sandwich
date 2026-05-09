package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.service.DepartmentService;
import com.github.thundax.modules.sys.service.query.DepartmentQuery;
import org.springframework.stereotype.Component;

@Component
public class DepartmentAuditObjectLoader implements AuditObjectLoader {

    private final DepartmentService departmentService;

    public DepartmentAuditObjectLoader(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Override
    public String objectType() {
        return "Department";
    }

    @Override
    public Object load(String objectId) {
        DepartmentQuery query = new DepartmentQuery();
        query.setId(EntityIdCodec.toDomain(Long.valueOf(objectId)));
        return departmentService.get(query);
    }
}
