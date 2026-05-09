package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.runtime.AuditObjectLoader;
import com.github.thundax.modules.sys.entity.valueobject.DepartmentIdCodec;
import com.github.thundax.modules.sys.service.DepartmentService;
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
        return departmentService.get(DepartmentIdCodec.toDomain(Long.valueOf(objectId)));
    }
}
