package com.github.thundax.modules.audit.runtime;

import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;

public interface AuditSnapshotAssembler {

    String objectType();

    AuditSnapshot assemble(Object object);
}
