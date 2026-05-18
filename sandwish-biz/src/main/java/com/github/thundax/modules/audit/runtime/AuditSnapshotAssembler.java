package com.github.thundax.modules.audit.runtime;

import com.github.thundax.modules.audit.entity.valueobject.AuditField;
import com.github.thundax.modules.audit.entity.valueobject.AuditSnapshot;
import java.util.List;

public interface AuditSnapshotAssembler {

    String objectType();

    String objectTypeLabel();

    List<AuditField> fields();

    AuditSnapshot assemble(Object object);
}
