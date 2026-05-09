package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssembler;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.sys.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentAuditSnapshotAssembler implements AuditSnapshotAssembler {

    @Override
    public String objectType() {
        return "Department";
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        Department department = (Department) object;
        if (department == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                department.getId(),
                department.getName(),
                AuditSnapshots.field("name", "名称", department.getName()),
                AuditSnapshots.field("shortName", "简称", department.getShortName()),
                AuditSnapshots.field("parentId", "父级", department.getParentId()));
    }
}
