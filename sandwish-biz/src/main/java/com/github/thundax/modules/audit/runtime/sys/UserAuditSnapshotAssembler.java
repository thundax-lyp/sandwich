package com.github.thundax.modules.audit.runtime.sys;

import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import com.github.thundax.modules.audit.runtime.AuditSnapshotAssembler;
import com.github.thundax.modules.audit.runtime.AuditSnapshots;
import com.github.thundax.modules.sys.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserAuditSnapshotAssembler implements AuditSnapshotAssembler {

    private static final String OBJECT_TYPE = "User";

    @Override
    public String objectType() {
        return OBJECT_TYPE;
    }

    @Override
    public AuditSnapshot assemble(Object object) {
        User user = (User) object;
        if (user == null) {
            return null;
        }
        return AuditSnapshots.of(
                objectType(),
                user.getId(),
                user.getName(),
                AuditSnapshots.field("name", "名称", user.getName()),
                AuditSnapshots.field("status", "状态", user.getStatus()),
                AuditSnapshots.field("privilege", "权限", user.getPrivilege()));
    }
}
