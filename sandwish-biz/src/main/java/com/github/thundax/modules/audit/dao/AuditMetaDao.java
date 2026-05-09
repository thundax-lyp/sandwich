package com.github.thundax.modules.audit.dao;

import com.github.thundax.modules.audit.entity.AuditMeta;
import com.github.thundax.modules.audit.entity.valueobject.AuditMetaId;
import com.github.thundax.modules.audit.entity.valueobject.AuditObjectRef;

public interface AuditMetaDao {

    AuditMeta getByObjectRef(AuditObjectRef objectRef);

    AuditMetaId insert(AuditMeta meta);

    int update(AuditMeta meta);
}
